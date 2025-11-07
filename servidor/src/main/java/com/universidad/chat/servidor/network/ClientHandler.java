package com.universidad.chat.servidor.network;

import com.universidad.chat.comun.dto.*;
import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.service.CanalService;
import com.universidad.chat.servidor.service.MensajeService;
import com.universidad.chat.servidor.service.TranscriptionService;
import com.universidad.chat.servidor.service.UsuarioService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    // --- Variables ---
    private final UsuarioService usuarioService;
    private final CanalService canalService;
    private final CanalRepository canalRepository;
    private final MensajeService mensajeService;
    private final TranscriptionService transcriptionService;
    private final Socket clientSocket;
    private final TCPServer tcpServer;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private UserDTO authenticatedUser;
    private String lastUploadedAudioFileName = null;
    private String lastTranscriptionResult = null;

    // Constructor
    public ClientHandler(Socket socket, UsuarioService us, TCPServer ts, CanalService cs, CanalRepository cr, MensajeService ms, TranscriptionService tsc) {
        this.clientSocket = socket;
        this.usuarioService = us;
        this.tcpServer = ts;
        this.canalService = cs;
        this.canalRepository = cr;
        this.mensajeService = ms;
        this.transcriptionService = tsc;
    }

    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Packet receivedPacket = (Packet) inputStream.readObject();
                switch (receivedPacket.getAction()) {
                    case REGISTER_REQUEST:
                        handleRegistration(receivedPacket);
                        break;
                    case LOGIN_REQUEST:
                        handleLogin(receivedPacket);
                        break;
                    case UPLOAD_AUDIO:
                        handleAudioUpload(receivedPacket);
                        break;
                    case SEND_MESSAGE_TO_USER:
                        MessageDTO userMessage = (MessageDTO) receivedPacket.getPayload(); // Get the message DTO
                        ClientHandler recipientHandler = tcpServer.findClientByUsername(userMessage.getRecipient());
                        if (recipientHandler != null) {
                            // Destinatario local
                            processAndForwardNotification(userMessage, recipientHandler);
                        } else {
                            // Destinatario en otro servidor, reenviar a federación
                            // PRIMERO: aplicar transcripción si es audio (PERO NO LIMPIAR VARIABLES TODAVÍA)
                            if (userMessage.isAudioMessage()) {
                                if (this.lastUploadedAudioFileName != null && this.lastTranscriptionResult != null) {
                                    userMessage.setContent(this.lastTranscriptionResult);
                                    userMessage.setAudioFileName(this.lastUploadedAudioFileName);
                                    // NO limpiar todavía, lo necesitamos para forwardToFederation
                                }
                            }
                            
                            // SEGUNDO: Guardar en BD local
                            mensajeService.guardarMensaje(userMessage);
                            
                            // TERCERO: Enviar eco al emisor (ANTES de reenviar a federación)
                            Packet echoPacket = new Packet(ActionType.NEW_MESSAGE, userMessage);
                            this.sendPacket(echoPacket);
                            System.out.println("📤 Eco enviado al emisor: " + userMessage.getSender() + " (destinatario remoto)");
                            
                            // CUARTO: Reenviar a federación (esto incluirá el audio en Base64)
                            if (!forwardToFederation(userMessage)) {
                                System.err.println("❌ Destinatario " + userMessage.getRecipient() + " no encontrado en federación.");
                            }
                            
                            // QUINTO: AHORA sí limpiamos las variables temporales
                            this.lastUploadedAudioFileName = null;
                            this.lastTranscriptionResult = null;
                        }
                        break;
                    case SEND_MESSAGE_TO_CHANNEL:
                        MessageDTO channelMessage = (MessageDTO) receivedPacket.getPayload(); // Get the message DTO
                        processAndForwardNotification(channelMessage, null); // Send the DTO, null means broadcast to channel members
                        // Además, reenviar a otros servidores si el canal es federado
                        forwardChannelMessageToFederation(channelMessage);
                        break;
                    case CREATE_CHANNEL_REQUEST:
                        handleCreateChannel(receivedPacket);
                        break;
                    case INVITE_USER_REQUEST:
                        handleInviteUser(receivedPacket);
                        break;
                    case INVITATION_RESPONSE:
                        handleInvitationResponse(receivedPacket);
                        break;
                    case DOWNLOAD_AUDIO_REQUEST:
                        handleAudioDownloadRequest(receivedPacket);
                        break;
                    default:
                        System.out.println("Acción desconocida recibida: " + receivedPacket.getAction());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Cliente desconectado: " + clientSocket.getInetAddress());
        } finally {
            tcpServer.removeClient(this);
            broadcastUserListUpdate();
            try { clientSocket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void handleAudioUpload(Packet packet) {
        MessageDTO messageWithBase64 = (MessageDTO) packet.getPayload();
        if (!messageWithBase64.isAudioMessage() || messageWithBase64.getAudioDataBase64() == null) {
            return;
        }

        try {
            byte[] audioData = Base64.getDecoder().decode(messageWithBase64.getAudioDataBase64());
            File audioDir = new File("audio_files");
            if (!audioDir.exists()) audioDir.mkdir();
            String uniqueFileName = UUID.randomUUID().toString() + ".wav";
            File audioFile = new File(audioDir, uniqueFileName);

            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioData);
            }
            System.out.println("Audio de " + messageWithBase64.getSender() + " guardado como: " + uniqueFileName);

            // --- ¡LÓGICA DE TRANSCRIPCIÓN AÑADIDA AQUÍ! ---
            String transcripcion = transcriptionService.transcribeAudio(audioData);

            // Guardamos ambos resultados para que el siguiente método los use
            this.lastUploadedAudioFileName = uniqueFileName;
            this.lastTranscriptionResult = transcripcion;

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error fatal al procesar el audio subido: " + e.getMessage());
            this.lastUploadedAudioFileName = null;
            this.lastTranscriptionResult = null;
        }
    }

    private void processAndForwardNotification(MessageDTO messageNotification, ClientHandler singleRecipient) {
        String uniqueFileName = null;
        String finalTextContent = messageNotification.getContent(); // Contenido original (placeholder)

        if (messageNotification.isAudioMessage()) {
            // Usamos los resultados REALES guardados en handleAudioUpload
            if (this.lastUploadedAudioFileName != null && this.lastTranscriptionResult != null) {
                uniqueFileName = this.lastUploadedAudioFileName;
                finalTextContent = this.lastTranscriptionResult; // La transcripción REAL

                // SOLO limpiamos si NO es un mensaje que irá a federación después
                // Para mensajes de canal, NO limpiamos aquí
                if (singleRecipient != null) {
                    // Es mensaje privado local, sí limpiamos
                    this.lastUploadedAudioFileName = null;
                    this.lastTranscriptionResult = null;
                }
                // Para canales, se limpiará en forwardChannelMessageToFederation
            } else {
                // Fallback si algo falló MUY gravemente en handleAudioUpload
                uniqueFileName = "error_al_subir.wav";
                finalTextContent = "[Error interno procesando audio]";
                System.err.println("❌ ERROR: processAndForwardNotification llamado para audio sin datos previos.");
            }

            // Actualizamos el DTO con los datos CORRECTOS para la DB y la notificación
            messageNotification.setContent(finalTextContent);
            messageNotification.setAudioFileName(uniqueFileName);
            messageNotification.setAudioDataBase64(null); // Nos aseguramos de que no lleve datos
        }

        // Guardamos en DB (texto o transcripción REAL + nombre de archivo REAL)
        // Solo si el receptor existe localmente
        mensajeService.guardarMensaje(messageNotification);

        // Creamos y reenviamos el paquete de NOTIFICACIÓN (con datos correctos)
        Packet forwardPacket = new Packet(ActionType.NEW_MESSAGE, messageNotification);

        // Lógica de reenvío
        if (singleRecipient != null) { 
            // Mensaje Privado
            singleRecipient.sendPacket(forwardPacket); // Siempre enviar al destinatario
            this.sendPacket(forwardPacket); // Eco al emisor
            System.out.println("📨 Notificación de mensaje enviada a " + messageNotification.getRecipient() + " y eco a " + messageNotification.getSender());

        } else { 
            // Mensaje de Canal
            // Siempre enviar eco al emisor
            this.sendPacket(forwardPacket);
            
            // Si el canal existe localmente, enviar a sus miembros (excepto el emisor)
            canalRepository.findByNombreCanal(messageNotification.getRecipient()).ifPresentOrElse(
                canal -> {
                    Set<String> miembros = canal.getMiembros().stream()
                        .map(Usuario::getNombreUsuario)
                        .filter(username -> !username.equals(messageNotification.getSender())) // Excluir al emisor
                        .collect(Collectors.toSet());
                    
                    if (!miembros.isEmpty()) {
                        System.out.println("📢 Reenviando notificación de canal a " + miembros.size() + " miembros locales (sin emisor).");
                        tcpServer.broadcastToUserList(forwardPacket, miembros);
                    } else {
                        System.out.println("ℹ️ No hay otros miembros locales conectados al canal.");
                    }
                },
                () -> {
                    // El canal no existe localmente (es remoto), pero el eco ya se envió
                    System.out.println("ℹ️ Canal remoto, solo eco enviado al emisor: " + messageNotification.getSender());
                }
            );
        }
    }

    private void handleAudioDownloadRequest(Packet packet) {
        AudioRequestDTO request = (AudioRequestDTO) packet.getPayload();
        String fileName = request.getFileName();
        try {
            File audioFile = new File("audio_files/" + fileName);
            if (audioFile.exists()) {
                byte[] audioData = Files.readAllBytes(audioFile.toPath());
                MessageDTO audioResponse = new MessageDTO();
                audioResponse.setAudioMessage(true);
                audioResponse.setAudioFileName(fileName);

                String audioBase64 = Base64.getEncoder().encodeToString(audioData);
                audioResponse.setAudioDataBase64(audioBase64); // Enviamos Base64

                Packet audioPacket = new Packet(ActionType.AUDIO_DATA_RESPONSE, audioResponse);
                sendPacket(audioPacket);
            } else { System.err.println("Archivo solicitado no existe: " + fileName); }
        } catch (IOException e) { System.err.println("Error al leer archivo para descarga: " + e.getMessage()); }
    }

    private void handleRegistration(Packet packet) {
        UserDTO userData = (UserDTO) packet.getPayload();
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreUsuario(userData.getUsername());
        nuevoUsuario.setEmail(userData.getEmail());
        nuevoUsuario.setPassword(userData.getPassword());

        try {
            usuarioService.registrarUsuario(nuevoUsuario);
            Packet successPacket = new Packet(ActionType.REGISTER_SUCCESS, "¡Registro exitoso! Ya puedes iniciar sesión.");
            sendPacket(successPacket);
        } catch (IllegalStateException e) {
            Packet failurePacket = new Packet(ActionType.REGISTER_FAILURE, e.getMessage());
            sendPacket(failurePacket);
        }
    }

    private void handleLogin(Packet packet) {
        UserDTO userData = (UserDTO) packet.getPayload();
        try {
            Usuario usuarioAutenticado = usuarioService.autenticarUsuario(userData.getEmail(), userData.getPassword());
            UserDTO userInfo = new UserDTO();
            userInfo.setUsername(usuarioAutenticado.getNombreUsuario());
            userInfo.setEmail(usuarioAutenticado.getEmail());
            userInfo.setServerIP(tcpServer.getServerRegistry().getLocalServerIP());
            userInfo.setServerName(tcpServer.getServerName());
            this.authenticatedUser = userInfo;
            tcpServer.addClient(this);

            // CAMBIO: Obtenemos TODOS los usuarios (locales + remotos)
            List<String> allUsernames = tcpServer.getAllAvailableUsernames();

            // ¡CAMBIO CLAVE! Obtenemos la lista de canales SOLO para este usuario.
            List<String> misCanales = canalService.findCanalesPorMiembro(userInfo.getUsername()).stream()
                    .map(Canal::getNombreCanal)
                    .collect(Collectors.toList());

            // Creamos el DTO de bienvenida con la lista de canales personalizada.
            LoginSuccessDTO loginPayload = new LoginSuccessDTO(userInfo, allUsernames, misCanales);
            Packet successPacket = new Packet(ActionType.LOGIN_SUCCESS, loginPayload);
            sendPacket(successPacket);

            // Notificamos a los demás que nos hemos conectado
            broadcastUserListUpdate();
        } catch (IllegalStateException e) {
            Packet failurePacket = new Packet(ActionType.LOGIN_FAILURE, e.getMessage());
            sendPacket(failurePacket);
        }
    }

    private void handleCreateChannel(Packet packet) {
        ChannelDTO channelData = (ChannelDTO) packet.getPayload();
        try {
            canalService.crearCanal(channelData.getChannelName(), channelData.getCreatorUsername());

            // ¡CAMBIO CLAVE! Ya no notificamos a todos. Solo actualizamos la lista para el creador.
            // Obtenemos la nueva lista completa de canales para el creador.
            List<String> misCanales = canalService.findCanalesPorMiembro(channelData.getCreatorUsername()).stream()
                    .map(Canal::getNombreCanal)
                    .collect(Collectors.toList());

            Packet channelListPacket = new Packet(ActionType.CHANNEL_LIST_UPDATE, misCanales);
            // Se la enviamos solo a él.
            sendPacket(channelListPacket);

        } catch (IllegalStateException e) {
            Packet failurePacket = new Packet(ActionType.CREATE_CHANNEL_FAILURE, e.getMessage());
            sendPacket(failurePacket);
        }
    }

    private void broadcastUserListUpdate() {
        // Enviar lista completa de usuarios (locales + remotos)
        List<String> allUsers = tcpServer.getAllAvailableUsernames();
        Packet userListPacket = new Packet(ActionType.USER_LIST_UPDATE, allUsers);
        tcpServer.broadcastPacket(userListPacket);        // Notificar a servidores federados sobre la actualización de usuarios locales
        notifyFederatedServersUserListUpdate(tcpServer.getConnectedUsernames());
    }
    
    private void notifyFederatedServersUserListUpdate(List<String> usernames) {
        ServerUserListDTO userListDTO = new ServerUserListDTO();
        userListDTO.setServerIP(tcpServer.getServerRegistry().getLocalServerIP());
        userListDTO.setServerName(tcpServer.getServerName());
        userListDTO.setUsernames(usernames);
        
        Packet syncPacket = new Packet(ActionType.SERVER_USER_LIST_SYNC, userListDTO);
        tcpServer.getServerRegistry().broadcastToFederation(syncPacket);
        
        System.out.println("📡 Sincronizando lista de usuarios con servidores federados: " + usernames);
    }



    public UserDTO getAuthenticatedUser() { return authenticatedUser; }

    public void sendPacket(Packet packet) {
        try {
            String username = (authenticatedUser != null) ? authenticatedUser.getUsername() : "no-autenticado";
            System.out.println("  📤 Enviando " + packet.getAction() + " a " + username);
            outputStream.writeObject(packet);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("❌ Error enviando paquete al cliente: " + e.getMessage());
        }
    }

    private void handleInviteUser(Packet packet) {
        InvitationDTO invitation = (InvitationDTO) packet.getPayload();
        System.out.println(invitation.getInviterUsername() + " está invitando a " + invitation.getInvitedUsername() + " al canal " + invitation.getChannelName());

        // Primero buscamos al usuario localmente
        ClientHandler invitedHandler = tcpServer.findClientByUsername(invitation.getInvitedUsername());

        if (invitedHandler != null) {
            // Si está conectado localmente, le reenviamos la invitación
            Packet invitationPacket = new Packet(ActionType.CHANNEL_INVITATION, invitation);
            invitedHandler.sendPacket(invitationPacket);

            // Notificamos al que invitó que la invitación se envió
            Packet successPacket = new Packet(ActionType.INVITE_SUCCESS, "Invitación enviada a " + invitation.getInvitedUsername());
            sendPacket(successPacket);
        } else {
            // Si no está conectado localmente, buscamos en servidores federados
            String serverKey = tcpServer.getServerRegistry().findServerByUsername(invitation.getInvitedUsername());
            
            if (serverKey != null) {
                // El usuario está en un servidor federado, enviamos la invitación por federación
                System.out.println("👥 Enviando invitación federada a " + invitation.getInvitedUsername() + " en servidor " + serverKey);
                Packet federatedInvite = new Packet(ActionType.FEDERATED_CHANNEL_INVITE, invitation);
                tcpServer.getServerRegistry().sendToServer(serverKey, federatedInvite);
                
                // Notificamos al que invitó que la invitación se envió
                Packet successPacket = new Packet(ActionType.INVITE_SUCCESS, "Invitación enviada a " + invitation.getInvitedUsername() + " (servidor remoto)");
                sendPacket(successPacket);
            } else {
                // Si no está en ningún lado, notificamos el fallo
                Packet failurePacket = new Packet(ActionType.INVITE_FAILURE, "El usuario " + invitation.getInvitedUsername() + " no está conectado.");
                sendPacket(failurePacket);
            }
        }
    }

    private void handleInvitationResponse(Packet packet) {
        InvitationDTO response = (InvitationDTO) packet.getPayload();
        if (response.isAccepted()) {
            // El usuario aceptó la invitación
            System.out.println("✅ " + response.getInvitedUsername() + " aceptó invitación al canal " + response.getChannelName());
            
            // Verificar si el canal existe localmente
            boolean canalExisteLocal = canalRepository.findByNombreCanal(response.getChannelName()).isPresent();
            
            if (canalExisteLocal) {
                // El canal está en ESTE servidor, agregar al usuario localmente
                try {
                    canalService.agregarMiembroACanal(response.getChannelName(), response.getInvitedUsername());

                    // Enviar lista actualizada de canales al usuario
                    ClientHandler nuevoMiembroHandler = tcpServer.findClientByUsername(response.getInvitedUsername());
                    if (nuevoMiembroHandler != null) {
                        List<String> susCanales = canalService.findCanalesPorMiembro(response.getInvitedUsername()).stream()
                                .map(Canal::getNombreCanal)
                                .collect(Collectors.toList());

                        Packet channelListPacket = new Packet(ActionType.CHANNEL_LIST_UPDATE, susCanales);
                        nuevoMiembroHandler.sendPacket(channelListPacket);
                    }
                } catch (IllegalStateException e) {
                    System.err.println("⚠️ Error al agregar miembro local al canal: " + e.getMessage());
                }
            } else {
                // El canal NO está en este servidor, es un canal remoto
                // Enviar la respuesta al servidor que tiene el canal mediante federación
                System.out.println("📡 Canal remoto, enviando respuesta de aceptación a servidor de origen");
                
                Packet federatedResponse = new Packet(ActionType.FEDERATED_INVITATION_RESPONSE, response);
                tcpServer.getServerRegistry().broadcastToFederation(federatedResponse);
                
                // Agregar el canal a la lista local del usuario aunque sea remoto
                // (esto es para que le aparezca en su lista de canales)
                List<String> susCanales = canalService.findCanalesPorMiembro(response.getInvitedUsername()).stream()
                        .map(Canal::getNombreCanal)
                        .collect(Collectors.toList());
                
                // Agregar manualmente el canal remoto a la lista
                if (!susCanales.contains(response.getChannelName())) {
                    susCanales.add(response.getChannelName());
                }
                
                Packet channelListPacket = new Packet(ActionType.CHANNEL_LIST_UPDATE, susCanales);
                sendPacket(channelListPacket);
            }
        } else {
            System.out.println("❌ " + response.getInvitedUsername() + " rechazó invitación al canal " + response.getChannelName());
        }
    }

    /**
     * Reenvía un mensaje privado a través de la federación
     */
    private boolean forwardToFederation(MessageDTO message) {
        // Si es un mensaje de audio, necesitamos incluir los datos del archivo
        if (message.isAudioMessage() && message.getAudioFileName() != null) {
            try {
                File audioFile = new File("audio_files/" + message.getAudioFileName());
                if (audioFile.exists()) {
                    byte[] audioData = Files.readAllBytes(audioFile.toPath());
                    String audioBase64 = Base64.getEncoder().encodeToString(audioData);
                    message.setAudioDataBase64(audioBase64);
                    System.out.println("📦 Audio incluido en mensaje federado: " + message.getAudioFileName());
                } else {
                    System.err.println("⚠️ Archivo de audio no encontrado para federación: " + message.getAudioFileName());
                }
            } catch (IOException e) {
                System.err.println("❌ Error leyendo audio para federación: " + e.getMessage());
            }
        }
        
        FederatedMessageDTO fedMsg = new FederatedMessageDTO();
        fedMsg.setOriginServerIP(tcpServer.getServerRegistry().getLocalServerIP());
        fedMsg.setOriginServerName(tcpServer.getServerName());
        fedMsg.setMessage(message);
        fedMsg.setRequiresAudioData(message.isAudioMessage());

        Packet fedPacket = new Packet(
                message.isAudioMessage() ? ActionType.FEDERATED_AUDIO : ActionType.FEDERATED_MESSAGE,
                fedMsg
        );

        // Broadcast a todos los servidores federados (en producción, buscar servidor específico)
        tcpServer.getServerRegistry().broadcastToFederation(fedPacket);
        System.out.println("📡 Mensaje reenviado a federación: " + message.getRecipient());
        return true;
    }

    /**
     * Reenvía un mensaje de canal a través de la federación
     */
    private void forwardChannelMessageToFederation(MessageDTO message) {
        // Si es un mensaje de audio, necesitamos incluir los datos del archivo
        if (message.isAudioMessage() && message.getAudioFileName() != null) {
            try {
                File audioFile = new File("audio_files/" + message.getAudioFileName());
                if (audioFile.exists()) {
                    byte[] audioData = Files.readAllBytes(audioFile.toPath());
                    String audioBase64 = Base64.getEncoder().encodeToString(audioData);
                    message.setAudioDataBase64(audioBase64);
                    System.out.println("📦 Audio incluido en mensaje de canal federado: " + message.getAudioFileName());
                } else {
                    System.err.println("⚠️ Archivo de audio no encontrado para federación: " + message.getAudioFileName());
                }
            } catch (IOException e) {
                System.err.println("❌ Error leyendo audio para federación: " + e.getMessage());
            }
            
            // AHORA sí limpiamos las variables temporales después de leer el archivo
            this.lastUploadedAudioFileName = null;
            this.lastTranscriptionResult = null;
        }
        
        FederatedMessageDTO fedMsg = new FederatedMessageDTO();
        fedMsg.setOriginServerIP(tcpServer.getServerRegistry().getLocalServerIP());
        fedMsg.setOriginServerName(tcpServer.getServerName());
        fedMsg.setMessage(message);
        fedMsg.setRequiresAudioData(message.isAudioMessage());

        Packet fedPacket = new Packet(
                message.isAudioMessage() ? ActionType.FEDERATED_AUDIO : ActionType.FEDERATED_MESSAGE,
                fedMsg
        );

        tcpServer.getServerRegistry().broadcastToFederation(fedPacket);
        System.out.println("📡 Mensaje de canal reenviado a federación: " + message.getRecipient());
    }
}