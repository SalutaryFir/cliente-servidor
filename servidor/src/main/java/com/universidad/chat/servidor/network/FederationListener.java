package com.universidad.chat.servidor.network;

import com.universidad.chat.comun.dto.*;
import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.repository.UsuarioRepository;
import com.universidad.chat.servidor.service.CanalService;
import com.universidad.chat.servidor.service.MensajeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listener que acepta conexiones de otros servidores en el puerto de federación
 */
@Component
public class FederationListener {

    @Value("${chat.server.federation-port:5001}")
    private int federationPort;

    @Autowired
    private ServerRegistry serverRegistry;

    @Autowired
    private TCPServer tcpServer;

    @Autowired
    private CanalRepository canalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Eliminado: inyección no utilizada de CanalService

    @Autowired
    private MensajeService mensajeService;

    @PostConstruct
    public void startListener() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(federationPort)) {
                System.out.println("🌐 Listener de Federación iniciado en puerto " + federationPort);

                while (true) {
                    Socket federatedSocket = serverSocket.accept();
                    System.out.println("🔗 Conexión entrante de servidor: " + federatedSocket.getInetAddress());

                    // Crear un handler para este servidor federado
                    new Thread(new FederationHandler(federatedSocket, serverRegistry, tcpServer, canalRepository, usuarioRepository, mensajeService)).start();
                }
            } catch (IOException e) {
                System.err.println("❌ Error en FederationListener: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handler para procesar paquetes de un servidor federado
     */
    private static class FederationHandler implements Runnable {
        private final Socket socket;
        private final ServerRegistry serverRegistry;
        private final TCPServer tcpServer;
        private final CanalRepository canalRepository;
        private final UsuarioRepository usuarioRepository;
        private final MensajeService mensajeService;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        private String remoteServerKey;

        public FederationHandler(Socket socket, ServerRegistry registry, TCPServer tcpServer, 
                               CanalRepository canalRepository, UsuarioRepository usuarioRepository, 
                               MensajeService mensajeService) {
            this.socket = socket;
            this.serverRegistry = registry;
            this.tcpServer = tcpServer;
            this.canalRepository = canalRepository;
            this.usuarioRepository = usuarioRepository;
            this.mensajeService = mensajeService;
        }

        @Override
        public void run() {
            try {
                // Importante: crear primero ObjectOutputStream y hacer flush del header,
                // luego crear ObjectInputStream para evitar deadlocks y desalineación
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Packet receivedPacket = (Packet) inputStream.readObject();

                    switch (receivedPacket.getAction()) {
                        case SERVER_REGISTER:
                            handleServerRegister(receivedPacket);
                            break;

                        case SERVER_HEARTBEAT:
                            handleHeartbeat(receivedPacket);
                            break;

                        case SERVER_USER_LIST_SYNC:
                            handleUserListSync(receivedPacket);
                            break;
                        
                        case SERVER_TOPOLOGY_SYNC:
                            handleTopologySync(receivedPacket);
                            break;

                        case FEDERATED_MESSAGE:
                            handleFederatedMessage(receivedPacket);
                            break;

                        case FEDERATED_AUDIO:
                            handleFederatedAudio(receivedPacket);
                            break;
                            
                        case FEDERATED_CHANNEL_INVITE:
                            handleFederatedChannelInvite(receivedPacket);
                            break;
                            
                        case FEDERATED_INVITATION_RESPONSE:
                            handleFederatedInvitationResponse(receivedPacket);
                            break;

                        case SERVER_UNREGISTER:
                            handleServerUnregister(receivedPacket);
                            return; // Salir del loop

                        default:
                            System.out.println("⚠️ Acción S2S desconocida: " + receivedPacket.getAction());
                    }
                }
            } catch (java.io.EOFException e) {
                System.out.println("🔌 Conexión cerrada limpiamente: " + socket.getInetAddress());
            } catch (java.net.SocketException e) {
                System.out.println("🔌 Socket cerrado: " + socket.getInetAddress());
            } catch (IOException e) {
                System.err.println("❌ Error I/O con servidor federado " + socket.getInetAddress() + ": " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Error de deserialización con servidor federado " + socket.getInetAddress() + ": " + e.getMessage());
            } finally {
                if (remoteServerKey != null) {
                    serverRegistry.unregisterServer(remoteServerKey);
                }
                try {
                    socket.close();
                } catch (IOException e) { /* ignore */ }
            }
        }

        private void handleServerRegister(Packet packet) {
            ServerInfoDTO serverInfo = (ServerInfoDTO) packet.getPayload();
            remoteServerKey = serverInfo.getIpAddress() + ":" + serverInfo.getFederationPort();
            // Registrar usando los streams ya creados en este handler para evitar duplicación
            serverRegistry.registerServer(serverInfo, socket, outputStream, inputStream);

            // Responder con nuestra info
            ServerInfoDTO myInfo = new ServerInfoDTO();
            myInfo.setServerName(serverRegistry.getLocalServerName());
            myInfo.setIpAddress(serverRegistry.getLocalServerIP());
            // Enviar nuestro puerto real de federación, no el remoto
            myInfo.setFederationPort(serverRegistry.getLocalFederationPort());
            myInfo.setTimestamp(System.currentTimeMillis());

            try {
                Packet response = new Packet(ActionType.SERVER_REGISTER, myInfo);
                outputStream.writeObject(response);
                outputStream.flush();
                
                // Enviar inmediatamente nuestra lista de usuarios
                serverRegistry.sendUserListToServer(remoteServerKey);
                
                // NUEVO: Enviar la topología completa de servidores conocidos
                serverRegistry.sendTopologyToServer(remoteServerKey);
            } catch (IOException e) {
                System.err.println("Error respondiendo SERVER_REGISTER: " + e.getMessage());
            }
        }

        private void handleHeartbeat(Packet packet) {
            // Actualizar timestamp del servidor
            System.out.println("💓 Heartbeat recibido de " + remoteServerKey);
        }

        private void handleUserListSync(Packet packet) {
            ServerUserListDTO userList = (ServerUserListDTO) packet.getPayload();
            System.out.println("📋 Lista de usuarios recibida de " + userList.getServerName() + ": " + userList.getUsernames());
            
            if (remoteServerKey != null) {
                System.out.println("🔄 Actualizando caché para " + remoteServerKey);
                serverRegistry.updateRemoteUsers(remoteServerKey, userList.getUsernames());
                // Asegurar que conocemos el nombre del servidor remoto para anotaciones en la UI
                serverRegistry.updateServerName(remoteServerKey, userList.getServerName());
            } else {
                System.err.println("⚠️ remoteServerKey es null, no se puede actualizar caché");
            }
            
            // Notificar a la UI del servidor para que actualice la lista de usuarios federados
            tcpServer.notifyRemoteServerUserList(userList);
            
            // IMPORTANTE: Notificar a TODOS los clientes locales sobre la actualización
            System.out.println("📢 Broadcasting lista completa a clientes locales...");
            tcpServer.broadcastUserListToClients();
        }
        
        private void handleTopologySync(Packet packet) {
            ServerTopologyDTO topology = (ServerTopologyDTO) packet.getPayload();
            System.out.println("🌐 Topología recibida con " + topology.getServers().size() + " servidores");
            
            // Conectar automáticamente a todos los servidores de la topología
            serverRegistry.connectToTopology(topology);
        }

        private void handleFederatedMessage(Packet packet) {
            FederatedMessageDTO fedMsg = (FederatedMessageDTO) packet.getPayload();
            MessageDTO message = fedMsg.getMessage();

            // Si es un mensaje de audio Y tiene datos Base64, guardar el archivo localmente
            if (message.isAudioMessage() && message.getAudioDataBase64() != null) {
                try {
                    byte[] audioData = java.util.Base64.getDecoder().decode(message.getAudioDataBase64());
                    java.io.File audioDir = new java.io.File("audio_files");
                    if (!audioDir.exists()) audioDir.mkdir();
                    
                    // Usar el mismo nombre de archivo que tiene el mensaje
                    java.io.File audioFile = new java.io.File(audioDir, message.getAudioFileName());
                    
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(audioFile)) {
                        fos.write(audioData);
                    }
                    System.out.println("🔊 Audio federado guardado localmente: " + message.getAudioFileName());
                    
                    // Limpiar los datos Base64 para no enviarlos al cliente
                    message.setAudioDataBase64(null);
                } catch (Exception e) {
                    System.err.println("❌ Error guardando audio federado: " + e.getMessage());
                }
            }

            // Buscar destinatario local
            ClientHandler recipient = tcpServer.findClientByUsername(message.getRecipient());

            if (recipient != null) {
                // El usuario está aquí, reenviar el mensaje
                Packet forwardPacket = new Packet(ActionType.NEW_MESSAGE, message);
                recipient.sendPacket(forwardPacket);
                System.out.println("📨 Mensaje federado entregado a " + message.getRecipient());
            } else if (message.getRecipient().startsWith("#")) {
                // Es un mensaje de canal
                // CASO 1: El canal existe localmente (este servidor tiene el canal en su BD)
                java.util.Optional<Canal> canalOpt = canalRepository.findByNombreCanal(message.getRecipient());
                
                if (canalOpt.isPresent()) {
                    // El canal existe aquí, broadcast a todos los miembros locales
                    Canal canal = canalOpt.get();
                    java.util.Set<String> miembrosLocales = canal.getMiembros().stream()
                            .map(u -> u.getNombreUsuario())
                            .collect(java.util.stream.Collectors.toSet());
                    
                    Packet forwardPacket = new Packet(ActionType.NEW_MESSAGE, message);
                    tcpServer.broadcastToUserList(forwardPacket, miembrosLocales);
                    System.out.println("📨 Mensaje federado de canal entregado a " + miembrosLocales.size() + " miembros locales del canal " + message.getRecipient());
                } else {
                    // CASO 2: El canal NO existe localmente (canal está en servidor remoto)
                    // Broadcast a TODOS los usuarios conectados localmente
                    // (asumimos que si recibimos el mensaje federado, hay usuarios locales en ese canal remoto)
                    Packet forwardPacket = new Packet(ActionType.NEW_MESSAGE, message);
                    tcpServer.broadcastPacket(forwardPacket);  // ✅ Usar el Packet correcto
                    System.out.println("📨 Mensaje federado de canal remoto " + message.getRecipient() + " broadcast a todos los clientes locales");
                }
            } else {
                System.err.println("⚠️ Destinatario " + message.getRecipient() + " no encontrado localmente");
            }

            // NO guardar mensajes federados en BD local para evitar error de "emisor no existe"
            // El mensaje ya está guardado en el servidor de origen
            System.out.println("📝 Mensaje federado procesado (no guardado en BD local)");
        }

        private void handleFederatedAudio(Packet packet) {
            // Similar a handleFederatedMessage pero para audio
            handleFederatedMessage(packet); // Por ahora reutilizamos la lógica
        }
        
        private void handleFederatedChannelInvite(Packet packet) {
            InvitationDTO invitation = (InvitationDTO) packet.getPayload();
            System.out.println("👥 Invitación federada recibida: " + invitation.getInviterUsername() + 
                             " invita a " + invitation.getInvitedUsername() + " al canal " + invitation.getChannelName());
            
            // Buscar al usuario invitado localmente
            ClientHandler invitedHandler = tcpServer.findClientByUsername(invitation.getInvitedUsername());
            
            if (invitedHandler != null) {
                // El usuario está conectado a este servidor, enviarle la invitación
                Packet invitationPacket = new Packet(ActionType.CHANNEL_INVITATION, invitation);
                invitedHandler.sendPacket(invitationPacket);
                System.out.println("✅ Invitación federada entregada a " + invitation.getInvitedUsername());
            } else {
                System.err.println("⚠️ Usuario invitado " + invitation.getInvitedUsername() + " no encontrado localmente");
            }
        }
        
        private void handleFederatedInvitationResponse(Packet packet) {
            InvitationDTO response = (InvitationDTO) packet.getPayload();
            System.out.println("✅ Respuesta de invitación federada recibida: " + response.getInvitedUsername() + 
                             " " + (response.isAccepted() ? "aceptó" : "rechazó") + " unirse a " + response.getChannelName());
            
            if (!response.isAccepted()) {
                return; // No hacer nada si rechazó
            }
            
            // Verificar si el canal existe en este servidor
            canalRepository.findByNombreCanal(response.getChannelName()).ifPresent(canal -> {
                // El canal está en ESTE servidor, agregar al usuario remoto
                // Primero verificar/crear el usuario en la BD local si no existe
                usuarioRepository.findByNombreUsuario(response.getInvitedUsername()).ifPresentOrElse(
                    usuario -> {
                        // Usuario ya existe, agregar al canal
                        try {
                            if (!canal.getMiembros().contains(usuario)) {
                                canal.getMiembros().add(usuario);
                                canalRepository.save(canal);
                                System.out.println("✅ Usuario remoto " + response.getInvitedUsername() + " agregado al canal " + response.getChannelName());
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error agregando usuario remoto al canal: " + e.getMessage());
                        }
                    },
                    () -> {
                        // Usuario no existe en BD local, crear registro temporal
                        System.out.println("ℹ️ Creando registro temporal para usuario remoto: " + response.getInvitedUsername());
                        Usuario nuevoUsuarioRemoto = new Usuario();
                        nuevoUsuarioRemoto.setNombreUsuario(response.getInvitedUsername());
                        nuevoUsuarioRemoto.setEmail(response.getInvitedUsername() + "@remote.server"); // Email placeholder
                        nuevoUsuarioRemoto.setPassword("REMOTE_USER"); // Password placeholder
                        usuarioRepository.save(nuevoUsuarioRemoto);
                        
                        // Ahora agregar al canal
                        canal.getMiembros().add(nuevoUsuarioRemoto);
                        canalRepository.save(canal);
                        System.out.println("✅ Usuario remoto " + response.getInvitedUsername() + " creado y agregado al canal " + response.getChannelName());
                    }
                );
            });
        }

        private void handleServerUnregister(Packet packet) {
            System.out.println("👋 Servidor federado se desconecta: " + remoteServerKey);
        }
    }
}