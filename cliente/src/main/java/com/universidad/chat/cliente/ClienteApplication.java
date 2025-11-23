package com.universidad.chat.cliente;

import com.universidad.chat.cliente.repository.MensajeCacheRepository;
import com.universidad.chat.cliente.service.NetworkService;
import com.universidad.chat.cliente.vista.LoginVista;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class ClienteApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(ClienteApplication.class);
		ConfigurableApplicationContext context = builder.headless(false).run(args);

		SwingUtilities.invokeLater(() -> {
			// Obtenemos los servicios que necesitamos del contexto de Spring
			NetworkService networkService = context.getBean(NetworkService.class);
			MensajeCacheRepository cacheRepository = context.getBean(MensajeCacheRepository.class);

			// Mostrar diálogo de conexión al servidor
			boolean connected = showServerConnectionDialog(networkService);
			
			if (connected) {
				// Si la conexión fue exitosa, mostramos la ventana de login
				LoginVista loginVista = new LoginVista(networkService, cacheRepository);
				loginVista.setVisible(true);
			} else {
				// Si no se pudo conectar, cerramos la aplicación
				JOptionPane.showMessageDialog(null, 
					"No se pudo establecer conexión con el servidor.\nLa aplicación se cerrará.",
					"Error de Conexión",
					JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		});
	}
	
	/**
	 * Muestra un diálogo para que el usuario ingrese la IP y puerto del servidor
	 * @param networkService Servicio de red para establecer la conexión
	 * @return true si la conexión fue exitosa, false en caso contrario
	 */
	private static boolean showServerConnectionDialog(NetworkService networkService) {
		JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JLabel ipLabel = new JLabel("IP del Servidor:");
		JTextField ipField = new JTextField("localhost", 20);
		
		JLabel portLabel = new JLabel("Puerto:");
		JTextField portField = new JTextField("5000", 10);
		
		panel.add(ipLabel);
		panel.add(ipField);
		panel.add(portLabel);
		panel.add(portField);
		
		while (true) {
			int result = JOptionPane.showConfirmDialog(
				null,
				panel,
				"Conectar al Servidor de Chat",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
			);
			
			if (result == JOptionPane.OK_OPTION) {
				String serverIp = ipField.getText().trim();
				String portText = portField.getText().trim();
				
				// Validaciones
				if (serverIp.isEmpty()) {
					JOptionPane.showMessageDialog(null,
						"Por favor ingrese la IP del servidor.",
						"Campo Requerido",
						JOptionPane.WARNING_MESSAGE);
					continue;
				}
				
				if (portText.isEmpty()) {
					JOptionPane.showMessageDialog(null,
						"Por favor ingrese el puerto del servidor.",
						"Campo Requerido",
						JOptionPane.WARNING_MESSAGE);
					continue;
				}
				
				try {
					int serverPort = Integer.parseInt(portText);
					
					if (serverPort < 1 || serverPort > 65535) {
						JOptionPane.showMessageDialog(null,
							"El puerto debe estar entre 1 y 65535.",
							"Puerto Inválido",
							JOptionPane.ERROR_MESSAGE);
						continue;
					}
					
					// Intentar conectar
					System.out.println("Conectando a " + serverIp + ":" + serverPort + "...");
					boolean connected = networkService.connect(serverIp, serverPort);
					
					if (connected) {
						JOptionPane.showMessageDialog(null,
							"Conexión exitosa al servidor " + serverIp + ":" + serverPort,
							"Conectado",
							JOptionPane.INFORMATION_MESSAGE);
						return true;
					} else {
						int retry = JOptionPane.showConfirmDialog(null,
							"No se pudo conectar al servidor " + serverIp + ":" + serverPort + 
							"\n¿Desea intentar con otra dirección?",
							"Error de Conexión",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE);
						
						if (retry == JOptionPane.NO_OPTION) {
							return false;
						}
						// Si elige YES, continúa el loop
					}
					
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
						"El puerto debe ser un número válido.",
						"Formato Incorrecto",
						JOptionPane.ERROR_MESSAGE);
					continue;
				}
			} else {
				// Usuario canceló
				return false;
			}
		}
	}
}