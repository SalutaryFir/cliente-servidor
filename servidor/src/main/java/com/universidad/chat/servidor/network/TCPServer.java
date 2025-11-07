package com.universidad.chat.servidor.network;

import com.universidad.chat.comun.dto.ActionType;
import com.universidad.chat.comun.dto.Packet;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.service.CanalService;
import com.universidad.chat.servidor.service.MensajeService;
import com.universidad.chat.servidor.service.TranscriptionService;
import com.universidad.chat.servidor.service.UsuarioService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.PropertyChangeListener; // Import for Observer
import java.beans.PropertyChangeSupport; // Import for Observer
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors; // Import for stream operations

@Component
public class TCPServer {

    @Value("${chat.server.client-port:5000}")
    private int clientPort;
    
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    @Value("${chat.server.max-connections}")
    private int maxConnections;

    @Value("${chat.server.name:Servidor-Principal}")
    private String serverName;

    @Value("${chat.server.federation-port:5001}")
    private int federationPort;
    
    @Value("${server.address:}")
    private String configuredAddress;

    // --- Injected dependencies ---
    @Autowired private UsuarioService usuarioService;
    @Autowired private CanalService canalService;
    @Autowired private CanalRepository canalRepository;
    @Autowired private com.universidad.chat.servidor.repository.UsuarioRepository usuarioRepository;
    @Autowired private MensajeService mensajeService;
    @Autowired private TranscriptionService transcriptionService;
    @Autowired private ServerRegistry serverRegistry;

    // --- Observer Pattern Implementation ---
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    // --- End Observer Pattern ---

    @PostConstruct
    public void startServer() {
        // Configurar información local del servidor
        String localIP = getLocalIPAddress();
        serverRegistry.setLocalServerInfo(localIP, serverName, federationPort);
        serverRegistry.setTCPServer(this); // Establecer referencia al TCPServer

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(clientPort)) {
                // Use log method to send messages to console AND GUI
                log("======================================================");
                log("      SERVIDOR TCP INICIADO EN EL PUERTO " + clientPort);
                log("      NOMBRE: " + serverName);
                log("      IP LOCAL: " + localIP);
                log("      PUERTO FEDERACIÓN: " + federationPort);
                log("      LÍMITE DE CONEXIONES ESTABLECIDO EN: " + maxConnections);
                log("======================================================");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    synchronized (clients) { // Lock before checking size
                        if (clients.size() >= maxConnections) {
                            log("Conexión rechazada desde " + clientSocket.getInetAddress() + ". Límite alcanzado.");
                            try { clientSocket.close(); } catch (IOException ioex) { /* ignore */ }
                            continue;
                        }
                    } // Unlock
                    // Log connection attempt
                    log("Nuevo cliente intentando conectar desde: " + clientSocket.getInetAddress());

                    // Create handler and start its thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket, usuarioService, this, canalService, canalRepository, mensajeService, transcriptionService);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                log("!!! ERROR FATAL al iniciar el servidor TCP: " + e.getMessage());
                e.printStackTrace(); // Also print stack trace for detailed errors
            }
        }).start();
    }

    // --- Modified Methods to Fire Events ---

    /**
     * Adds an authenticated client handler to the list and notifies observers.
     * Called by ClientHandler after successful login.
     */
    public void addClient(ClientHandler clientHandler) {
        if (clientHandler.getAuthenticatedUser() == null) {
            log("Advertencia: Intento de añadir cliente no autenticado.");
            return; // Only add authenticated clients
        }
        List<String> oldUsernames = getConnectedUsernames();
        synchronized(clients) {
            clients.add(clientHandler);
        }
        List<String> newUsernames = getConnectedUsernames();
        log("Cliente autenticado y añadido: " + clientHandler.getAuthenticatedUser().getUsername() + " (" + clients.size() + "/" + maxConnections + ")");
        // Notify GUI about the change in the client list
        support.firePropertyChange("clientListChanged", oldUsernames, newUsernames);
    }

    /**
     * Removes a client handler from the list (on disconnect) and notifies observers.
     * Called by ClientHandler in its finally block.
     */
    public void removeClient(ClientHandler clientHandler) {
        List<String> oldUsernames = getConnectedUsernames();
        boolean removed;
        synchronized(clients) {
            removed = clients.remove(clientHandler);
        }
        String username = (clientHandler.getAuthenticatedUser() != null) ? clientHandler.getAuthenticatedUser().getUsername() : "No autenticado";

        if (removed) {
            List<String> newUsernames = getConnectedUsernames();
            log("Cliente desconectado: " + username + " (" + clients.size() + "/" + maxConnections + ")");
            support.firePropertyChange("clientListChanged", oldUsernames, newUsernames);
        }
    }

    // --- Helper method for logging (sends to console and GUI) ---
    public void log(String message) {
        System.out.println(message); // Keep console log
        support.firePropertyChange("logMessage", null, message); // Send log message to GUI
    }

    // --- Methods to provide info to GUI ---
    public int getMaxConnections() {
        return maxConnections;
    }

    public int getConnectedClientCount() {
        // Return the current size safely
        synchronized(clients) {
            return clients.size();
        }
    }

    // --- Existing methods needed by ClientHandler or GUI ---

    /**
     * Sends a packet to all currently connected clients.
     */
    public void broadcastPacket(Packet packet) {
        synchronized (clients) {
            // Use a copy of the list to avoid ConcurrentModificationException if a client disconnects during broadcast
            List<ClientHandler> clientsCopy = new ArrayList<>(clients);
            System.out.println("📡 Broadcasting packet tipo " + packet.getAction() + " a " + clientsCopy.size() + " clientes");
            for (ClientHandler client : clientsCopy) {
                if (client.getAuthenticatedUser() != null) {
                    System.out.println("  → Enviando a: " + client.getAuthenticatedUser().getUsername());
                }
                client.sendPacket(packet);
            }
        }
    }

    /**
     * Sends a packet only to clients whose usernames are in the provided set.
     */
    public void broadcastToUserList(Packet packet, Set<String> usernames) {
        synchronized (clients) {
            List<ClientHandler> clientsCopy = new ArrayList<>(clients);
            for (ClientHandler client : clientsCopy) {
                if (client.getAuthenticatedUser() != null && usernames.contains(client.getAuthenticatedUser().getUsername())) {
                    client.sendPacket(packet);
                }
            }
        }
    }

    /**
     * Finds an active ClientHandler by username. Returns null if not found.
     */
    public ClientHandler findClientByUsername(String username) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getAuthenticatedUser() != null && client.getAuthenticatedUser().getUsername().equals(username)) {
                    return client;
                }
            }
        }
        return null;
    }

    /**
     * Gets a list of usernames of all authenticated clients currently connected.
     */
    public List<String> getConnectedUsernames() {
        List<String> usernames = new ArrayList<>();
        synchronized(clients) {
            usernames = clients.stream()
                    .filter(c -> c.getAuthenticatedUser() != null)
                    .map(c -> c.getAuthenticatedUser().getUsername())
                    .collect(Collectors.toList());
        }
        return usernames;
    }
    
    /**
     * Obtiene TODOS los usuarios disponibles (locales + remotos de servidores federados)
     */
    public List<String> getAllAvailableUsernames() {
        List<String> localUsers = getConnectedUsernames();
        List<String> remoteUsers = serverRegistry.getAllRemoteUsers();
        List<String> allUsers = new ArrayList<>(localUsers);
        allUsers.addAll(remoteUsers);
        
        System.out.println("👥 Usuarios locales: " + localUsers);
        System.out.println("🌐 Usuarios remotos: " + remoteUsers);
        System.out.println("📋 Total disponibles: " + allUsers);
        
        return allUsers;
    }
    
    /**
     * Obtiene TODOS los usuarios disponibles anotados con el nombre del servidor
     * Ej: "alice (Servidor-Principal)"
     */
    public List<String> getAllAvailableUsernamesAnnotated() {
        List<String> annotated = new ArrayList<>();
        // Locales con nombre de este servidor
        String myName = getServerName();
        for (String u : getConnectedUsernames()) {
            annotated.add(u + " (" + myName + ")");
        }
        // Remotos con el nombre de su servidor
        annotated.addAll(serverRegistry.getAllRemoteUsersAnnotated());
        return annotated;
    }
    
    /**
     * Envía actualización de lista de usuarios a TODOS los clientes
     * Incluye usuarios locales Y remotos
     */
    public void broadcastUserListToClients() {
        List<String> allUsers = getAllAvailableUsernames();
        Packet userListPacket = new Packet(ActionType.USER_LIST_UPDATE, allUsers);
        
        System.out.println("📢 ===== BROADCAST USER LIST =====");
        System.out.println("📢 Lista a enviar: " + allUsers);
        
        broadcastPacket(userListPacket);
        
        System.out.println("📢 ===== FIN BROADCAST =====");
    }

    /**
     * Obtiene la IP local del servidor
     */
    private String getLocalIPAddress() {
        // Si está configurado server.address, úsalo (para desarrollo local)
        if (configuredAddress != null && !configuredAddress.trim().isEmpty()) {
            return configuredAddress;
        }
        
        // Si no, detecta la IP LAN automáticamente
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1"; // Fallback
        }
    }

    public ServerRegistry getServerRegistry() {
        return serverRegistry;
    }

    public String getServerName() {
        return serverName;
    }
    
    /**
     * Notifica a la UI cuando se recibe una actualización de lista de usuarios de un servidor federado
     */
    public void notifyRemoteServerUserList(com.universidad.chat.comun.dto.ServerUserListDTO userList) {
        // Construir un mensaje descriptivo de los usuarios remotos
        String remoteUsersInfo = userList.getServerName() + " (" + userList.getServerIP() + "): " + 
                                 String.join(", ", userList.getUsernames());
        
        // Notificar a través del PropertyChangeSupport para que la UI lo muestre
        support.firePropertyChange("remoteUsers", null, remoteUsersInfo);
        
        log("📡 Usuarios remotos actualizados - " + remoteUsersInfo);
    }
    
    /**
     * Transmite un mensaje federado a todos los clientes locales
     */
    public void broadcastMessageToLocalClients(com.universidad.chat.comun.dto.FederatedMessageDTO fedMsg) {
        Packet messagePacket = new Packet(ActionType.NEW_MESSAGE, fedMsg.getMessage());
        broadcastPacket(messagePacket);
        log("💬 Mensaje federado transmitido a clientes locales desde: " + fedMsg.getOriginServerName());
    }
    
    /**
     * Transmite un audio federado a todos los clientes locales
     */
    public void broadcastAudioToLocalClients(com.universidad.chat.comun.dto.FederatedMessageDTO fedMsg) {
        Packet audioPacket = new Packet(ActionType.UPLOAD_AUDIO, fedMsg.getMessage());
        broadcastPacket(audioPacket);
        log("🔊 Audio federado transmitido a clientes locales desde: " + fedMsg.getOriginServerName());
    }
    
    /**
     * Maneja la respuesta de invitación de un usuario remoto
     */
    public void handleRemoteInvitationResponse(com.universidad.chat.comun.dto.InvitationDTO response) {
        if (!response.isAccepted()) {
            return; // No hacer nada si rechazó
        }
        
        // Verificar si el canal existe en este servidor
        canalRepository.findByNombreCanal(response.getChannelName()).ifPresent(canal -> {
            // El canal está en ESTE servidor, agregar al usuario remoto
            // Primero verificar/crear el usuario en la BD local si no existe
            usuarioRepository.findByNombreUsuario(response.getInvitedUsername()).ifPresentOrElse(
                usuario -> {
                    // Usuario ya existe, agregar al canal si no está
                    try {
                        if (!canal.getMiembros().contains(usuario)) {
                            canal.getMiembros().add(usuario);
                            canalRepository.save(canal);
                            log("✅ Usuario remoto " + response.getInvitedUsername() + " agregado al canal " + response.getChannelName());
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error agregando usuario remoto al canal: " + e.getMessage());
                    }
                },
                () -> {
                    // Usuario no existe en BD local, crear registro temporal
                    log("ℹ️ Creando registro temporal para usuario remoto: " + response.getInvitedUsername());
                    com.universidad.chat.servidor.model.Usuario nuevoUsuarioRemoto = new com.universidad.chat.servidor.model.Usuario();
                    nuevoUsuarioRemoto.setNombreUsuario(response.getInvitedUsername());
                    nuevoUsuarioRemoto.setEmail(response.getInvitedUsername() + "@remote.server");
                    nuevoUsuarioRemoto.setPassword("REMOTE_USER");
                    usuarioRepository.save(nuevoUsuarioRemoto);
                    
                    // Ahora agregar al canal
                    canal.getMiembros().add(nuevoUsuarioRemoto);
                    canalRepository.save(canal);
                    log("✅ Usuario remoto " + response.getInvitedUsername() + " creado y agregado al canal " + response.getChannelName());
                }
            );
        });
    }
}