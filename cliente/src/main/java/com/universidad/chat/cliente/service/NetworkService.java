package com.universidad.chat.cliente.service;

import com.universidad.chat.comun.dto.Packet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Service
public class NetworkService {

    @Value("${chat.client.server-address:localhost}")
    private String serverAddress;

    @Value("${chat.client.server-port:5000}")
    private int serverPort;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;


    // --- IMPLEMENTACIÓN DEL PATRÓN OBSERVER ---
    // PropertyChangeSupport es una clase de ayuda de Java para implementar el patrón Observer fácilmente.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
    // --- FIN DE LA IMPLEMENTACIÓN ---

    @PostConstruct
    public void connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Conectado al servidor en " + serverAddress + ":" + serverPort);

            // ¡Iniciamos un hilo para escuchar al servidor!
            startListening();

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    // El hilo se bloquea aquí hasta que recibe un paquete.
                    Packet receivedPacket = (Packet) inputStream.readObject();

                    // Cuando recibe algo, notifica a todos los observadores (la UI).
                    // El primer argumento es el "nombre" del evento.
                    // El segundo es el valor antiguo (no lo usamos).
                    // El tercero es el nuevo valor (el paquete que llegó).
                    support.firePropertyChange("newPacket", null, receivedPacket);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Desconectado del servidor: " + e.getMessage());
            }
        }).start();
    }

    // El método sendPacket() no cambia.
    public void sendPacket(Packet packet) {
        // ... (código sin cambios) ...
        if (outputStream != null) {
            try {
                outputStream.writeObject(packet);
                outputStream.flush();
            } catch (IOException e) {
                System.err.println("Error al enviar paquete: " + e.getMessage());
            }
        }
    }
}