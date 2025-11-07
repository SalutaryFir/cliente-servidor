package com.universidad.chat.servidor.network;

import com.universidad.chat.comun.dto.ActionType;
import com.universidad.chat.comun.dto.FederatedMessageDTO;
import com.universidad.chat.comun.dto.InvitationDTO;
import com.universidad.chat.comun.dto.Packet;
import com.universidad.chat.comun.dto.ServerTopologyDTO;
import com.universidad.chat.comun.dto.ServerUserListDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Maneja la recepción de paquetes desde un servidor al que nos conectamos proactivamente.
 * Este handler es necesario para que las conexiones salientes también puedan recibir
 * listas de usuarios, topología y otros mensajes del servidor remoto.
 */
public class OutgoingConnectionHandler implements Runnable {
    private final Socket socket;
    private final ServerRegistry serverRegistry;
    private final TCPServer tcpServer;
    private final String serverKey;
    private boolean running = true;

    public OutgoingConnectionHandler(Socket socket, ServerRegistry serverRegistry, 
                                    TCPServer tcpServer, String serverKey) {
        this.socket = socket;
        this.serverRegistry = serverRegistry;
        this.tcpServer = tcpServer;
        this.serverKey = serverKey;
    }

    @Override
    public void run() {
        System.out.println("🔄 OutgoingConnectionHandler iniciado para servidor: " + serverKey);
        
        try {
            ServerRegistry.FederatedServer fs = serverRegistry.getFederatedServer(serverKey);
            if (fs == null || fs.inputStream == null) {
                System.err.println("❌ No se pudo obtener inputStream para " + serverKey);
                return;
            }
            
            // Usar SIEMPRE el inputStream ya creado en FederatedServer
            ObjectInputStream in = fs.inputStream;
            
            while (running && fs.socket != null && !fs.socket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Packet) {
                        Packet packet = (Packet) obj;
                        handlePacket(packet, serverKey);
                    } else {
                        System.out.println("⚠️ Objeto no reconocido recibido: " + (obj != null ? obj.getClass() : "null"));
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("❌ Error de deserialización desde " + serverKey + ": " + e.getMessage());
                } catch (java.io.EOFException e) {
                    // Conexión cerrada limpiamente por el otro lado
                    System.out.println("🔌 Servidor remoto cerró la conexión: " + serverKey);
                    break;
                } catch (java.net.SocketException e) {
                    // Socket cerrado (timeout, reset, etc.)
                    if (running) {
                        System.out.println("🔌 Socket cerrado para servidor: " + serverKey);
                    }
                    break;
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Error de I/O con servidor " + serverKey + ": " + e.getMessage());
                    }
                    break;
                }
            }
            
        } catch (Exception e) {
            if (running) {
                System.err.println("❌ Error fatal en OutgoingConnectionHandler para " + serverKey + ": " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            if (running) {
                serverRegistry.unregisterServer(serverKey);
            }
            running = false;
            System.out.println("🔌 OutgoingConnectionHandler finalizado para " + serverKey);
        }
    }

    private void handlePacket(Packet packet, String fromServerKey) {
        ActionType action = packet.getAction();
        Object data = packet.getPayload();
        
        System.out.println("📩 Paquete recibido en OutgoingConnectionHandler desde " + fromServerKey + ": " + action);

        switch (action) {
            case SERVER_USER_LIST_SYNC:
                handleUserListSync((ServerUserListDTO) data, fromServerKey);
                break;
                
            case SERVER_TOPOLOGY_SYNC:
                handleTopologySync((ServerTopologyDTO) data);
                break;
                
            case FEDERATED_MESSAGE:
                handleFederatedMessage((FederatedMessageDTO) data);
                break;
                
            case FEDERATED_AUDIO:
                handleFederatedAudio((FederatedMessageDTO) data);
                break;
                
            case FEDERATED_CHANNEL_INVITE:
                handleFederatedChannelInvite((InvitationDTO) data);
                break;
                
            case FEDERATED_INVITATION_RESPONSE:
                handleFederatedInvitationResponse((InvitationDTO) data);
                break;
                
            case SERVER_HEARTBEAT:
                // Actualizar timestamp del servidor
                serverRegistry.updateServerTimestamp(fromServerKey);
                break;
                
            default:
                System.out.println("⚠️ Acción no manejada en OutgoingConnectionHandler: " + action);
                break;
        }
    }

    private void handleUserListSync(ServerUserListDTO userListDTO, String fromServerKey) {
        System.out.println("👥 Lista de usuarios recibida desde " + fromServerKey + ": " + userListDTO.getUsernames());
        
        // Actualizar cache de usuarios remotos
        serverRegistry.updateRemoteUsers(fromServerKey, userListDTO.getUsernames());
        
        // Notificar a todos los clientes locales con la lista actualizada
        if (tcpServer != null) {
            tcpServer.broadcastUserListToClients();
        }
    }

    private void handleTopologySync(ServerTopologyDTO topology) {
        System.out.println("🌐 Topología recibida con " + topology.getServers().size() + " servidores");
        
        // Conectar automáticamente a los servidores de la topología
        serverRegistry.connectToTopology(topology);
    }

    private void handleFederatedMessage(FederatedMessageDTO fedMsg) {
        System.out.println("💬 Mensaje federado recibido en OutgoingConnectionHandler de: " + fedMsg.getOriginServerName());
        
        if (tcpServer != null) {
            com.universidad.chat.comun.dto.MessageDTO message = fedMsg.getMessage();
            
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
                // Es un mensaje de canal - broadcast a todos los clientes locales
                Packet forwardPacket = new Packet(ActionType.NEW_MESSAGE, message);
                tcpServer.broadcastPacket(forwardPacket);
                System.out.println("📨 Mensaje federado de canal " + message.getRecipient() + " broadcast a clientes locales");
            } else {
                System.err.println("⚠️ Destinatario " + message.getRecipient() + " no encontrado localmente");
            }
        }
    }

    private void handleFederatedAudio(FederatedMessageDTO fedMsg) {
        System.out.println("🔊 Audio federado recibido en OutgoingConnectionHandler de: " + fedMsg.getOriginServerName());
        
        // Reutilizar la misma lógica que handleFederatedMessage
        handleFederatedMessage(fedMsg);
    }
    
    private void handleFederatedChannelInvite(InvitationDTO invitation) {
        System.out.println("👥 Invitación federada recibida en OutgoingConnectionHandler: " + 
                         invitation.getInviterUsername() + " invita a " + invitation.getInvitedUsername());
        
        // Buscar al usuario invitado localmente
        if (tcpServer != null) {
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
    }
    
    private void handleFederatedInvitationResponse(InvitationDTO response) {
        System.out.println("✅ Respuesta de invitación federada recibida en OutgoingConnectionHandler: " + 
                         response.getInvitedUsername() + " " + (response.isAccepted() ? "aceptó" : "rechazó"));
        
        // Delegar el manejo al TCPServer que tiene acceso a los repositorios
        if (tcpServer != null) {
            tcpServer.handleRemoteInvitationResponse(response);
        }
    }

    public void stop() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando socket en OutgoingConnectionHandler: " + e.getMessage());
        }
    }
}
