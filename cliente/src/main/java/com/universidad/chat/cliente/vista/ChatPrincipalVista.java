package com.universidad.chat.cliente.vista;

import com.universidad.chat.cliente.service.NetworkService;
import com.universidad.chat.comun.dto.*;
import com.universidad.chat.cliente.model.MensajeCache; // <-- Importar
import com.universidad.chat.cliente.repository.MensajeCacheRepository;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;


public class ChatPrincipalVista extends JFrame implements PropertyChangeListener {

    private final NetworkService networkService;
    private final UserDTO usuarioActual;
    private final MensajeCacheRepository cacheRepository;
    private JScrollPane chatScrollPane;
    private JTextField campoMensaje;
    private JButton btnEnviar;
    private JToggleButton btnGrabarAudio;
    private JList<String> listaUsuarios, listaCanales;
    private DefaultListModel<String> modeloUsuarios, modeloCanales;
    private final Map<String, JPanel> conversationPanels = new ConcurrentHashMap<>();
    private String currentChatTarget = null;
    private boolean isRecording = false;
    private TargetDataLine audioLine;
    private ByteArrayOutputStream audioStream;

    // Constructor
    public ChatPrincipalVista(NetworkService networkService, UserDTO usuarioActual, List<String> initialUsernames, List<String> initialChannelNames, MensajeCacheRepository cacheRepository) {
        this.networkService = networkService;
        this.usuarioActual = usuarioActual;
        this.cacheRepository = cacheRepository;
        this.networkService.addPropertyChangeListener(this);
        
        // Construir título con info del servidor
        String serverInfo = usuarioActual.getServerName() != null ? 
            usuarioActual.getServerName() + " (" + usuarioActual.getServerIP() + ")" :
            "Servidor Local";
        setTitle("Chat Universitario - " + usuarioActual.getUsername() + " @ " + serverInfo);
        
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        inicializarComponentes();
        initialUsernames.forEach(modeloUsuarios::addElement);
        initialChannelNames.forEach(modeloCanales::addElement);
        construirLayout();
        inicializarListeners();
        crearBarraMenu();
    }

    private void inicializarComponentes() {
        modeloUsuarios = new DefaultListModel<>();
        modeloCanales = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaCanales = new JList<>(modeloCanales);
        chatScrollPane = new JScrollPane();
        campoMensaje = new JTextField(40);
        btnEnviar = new JButton("Enviar");

        // --- Componente de audio actualizado ---
        btnGrabarAudio = new JToggleButton("🎤 Grabar");
    }

    private void construirLayout() {
        JPanel panelIzquierdo = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel panelUsuarios = new JPanel(new BorderLayout());
        panelUsuarios.add(new JLabel("Usuarios Conectados:", SwingConstants.CENTER), BorderLayout.NORTH);
        panelUsuarios.add(new JScrollPane(listaUsuarios), BorderLayout.CENTER);
        JPanel panelCanales = new JPanel(new BorderLayout());
        panelCanales.add(new JLabel("Canales:", SwingConstants.CENTER), BorderLayout.NORTH);
        panelCanales.add(new JScrollPane(listaCanales), BorderLayout.CENTER);
        panelIzquierdo.add(panelUsuarios);
        panelIzquierdo.add(panelCanales);
        JPanel panelDerecho = new JPanel(new BorderLayout(10, 10));
        JPanel panelInput = new JPanel(new BorderLayout(10, 0));
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotones.add(btnGrabarAudio);
        panelBotones.add(btnEnviar);
        panelInput.add(campoMensaje, BorderLayout.CENTER);
        panelInput.add(panelBotones, BorderLayout.EAST);
        panelDerecho.add(chatScrollPane, BorderLayout.CENTER);
        panelDerecho.add(panelInput, BorderLayout.SOUTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPane.setDividerLocation(200);
        add(splitPane);
    }

    private void inicializarListeners() {
        btnEnviar.addActionListener(e -> handleSendMessage(false));
        campoMensaje.addActionListener(e -> handleSendMessage(false));
        btnGrabarAudio.addActionListener(e -> {
            if (btnGrabarAudio.isSelected()) {
                startRecording();
                btnGrabarAudio.setText("⏹️ Detener y Enviar");
            } else {
                stopAndSendRecording();
                btnGrabarAudio.setText("🎤 Grabar");
            }
        });
        listaUsuarios.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaUsuarios.getSelectedValue() != null) {
                listaCanales.clearSelection();
                currentChatTarget = "user:" + listaUsuarios.getSelectedValue();
                loadConversation();
            }
        });
        listaCanales.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaCanales.getSelectedValue() != null) {
                listaUsuarios.clearSelection();
                currentChatTarget = "channel:" + listaCanales.getSelectedValue();
                loadConversation();
            }
        });
    }
    private void loadConversation() {
        if (currentChatTarget == null) {
            chatScrollPane.setViewportView(new JPanel()); // Panel vacío si no hay selección
            return;
        }

        // 1. Obtiene o crea el panel usando computeIfAbsent.
        //    La función lambda AHORA solo crea el panel y carga el historial CORRECTAMENTE.
        JPanel activePanel = conversationPanels.computeIfAbsent(currentChatTarget, k -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Layout vertical

            // Carga el historial directamente desde H2
            List<MensajeCache> historial = cacheRepository.findByChatTargetAndOwnerUsernameOrderByTimestampAsc(k, usuarioActual.getUsername());

            // --- CORRECCIÓN ---
            // Construye las líneas visuales DIRECTAMENTE, SIN LLAMAR a addMessageToPanel
            for (MensajeCache msg : historial) {
                JPanel line = createVisualMessageLine(msg.getDisplayText(), msg.getAudioFileName());
                panel.add(line); // Añade la línea directamente al panel que estamos creando
            }
            return panel; // Devuelve el panel ya poblado
        });

        // 2. Muestra el panel (sin cambios)
        chatScrollPane.setViewportView(activePanel);
        chatScrollPane.revalidate();
        chatScrollPane.repaint();

        // 3. Scroll al final (sin cambios)
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void handleSendMessage(boolean isAudio) {
        if (isAudio) return; // Audio logic is handled by stopAndSendRecording

        String textoMensaje = campoMensaje.getText().trim();
        if (textoMensaje.isEmpty() || currentChatTarget == null) return;

        String recipientName = currentChatTarget.split(":", 2)[1];
        boolean isChannel = currentChatTarget.startsWith("channel:");

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSender(usuarioActual.getUsername());
        messageDTO.setRecipient(recipientName);
        messageDTO.setAudioMessage(false);
        messageDTO.setContent(textoMensaje);

        Packet packet = new Packet(
                isChannel ? ActionType.SEND_MESSAGE_TO_CHANNEL : ActionType.SEND_MESSAGE_TO_USER,
                messageDTO
        );
        networkService.sendPacket(packet);

        // Mostrar inmediatamente SOLO en chats privados (para evitar duplicados en canales)
        if (!isChannel) {
            String messageForDisplay = "Yo -> " + recipientName + ": " + textoMensaje;
            addMessageToPanel(currentChatTarget, messageForDisplay, null, true);
        }

        campoMensaje.setText(""); // Limpiamos el campo después de enviar Y mostrar
    }

    private void startRecording() {
        try {
            AudioFormat format = getAudioFormat(); // Asumiendo que tienes getAudioFormat()
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                JOptionPane.showMessageDialog(this, "El formato de audio no es soportado.", "Error de Grabación", JOptionPane.ERROR_MESSAGE);
                // --- FIX: Reiniciar estado si falla ---
                btnGrabarAudio.setSelected(false);
                btnGrabarAudio.setText("🎤 Grabar");
                isRecording = false; // Asegura que no se intente detener algo que no empezó
                return;
            }

            audioLine = (TargetDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();

            // --- Inicialización CORRECTA ---
            audioStream = new ByteArrayOutputStream(); // Ahora está garantizado que se inicializa si no hay error previo
            isRecording = true;

            new Thread(() -> {
                byte[] buffer = new byte[1024];
                // --- FIX: Usar try-with-resources o asegurar cierre ---
                // Añadimos un try-catch dentro del hilo por si falla la lectura
                try {
                    while (isRecording && audioLine != null && audioLine.isOpen()) {
                        int bytesRead = audioLine.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            audioStream.write(buffer, 0, bytesRead);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error durante la lectura del audio: " + e.getMessage());
                    // Podrías notificar al usuario aquí si quieres
                } finally {
                    // Aseguramos que los recursos se liberen incluso si el hilo se interrumpe
                    if (audioLine != null && audioLine.isOpen()) {
                        audioLine.stop();
                        audioLine.close();
                    }
                }
            }).start();

        } catch (LineUnavailableException e) {
            JOptionPane.showMessageDialog(this, "Error al acceder al micrófono. ¿Está conectado y no en uso?", "Error de Grabación", JOptionPane.ERROR_MESSAGE);
            btnGrabarAudio.setSelected(false);
            btnGrabarAudio.setText("🎤 Grabar");
            isRecording = false; // Reiniciar estado
            audioStream = null; // Asegurar que sea null si falla
        } catch (Exception e) { // Captura genérica por si acaso
            JOptionPane.showMessageDialog(this, "Error inesperado al iniciar grabación: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            btnGrabarAudio.setSelected(false);
            btnGrabarAudio.setText("🎤 Grabar");
            isRecording = false;
            audioStream = null;
        }
    }

    private void playAudio(byte[] audioData) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            JOptionPane.showMessageDialog(this, "No se pudo reproducir el archivo de audio. Asegúrate de que es un formato WAV válido.", "Error de Reproducción", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Dentro de ChatPrincipalVista.java

    private void stopAndSendRecording() {
        // --- FIX: Comprobar si la grabación realmente se inició ---
        if (!isRecording || audioLine == null || audioStream == null) {
            // Si no se estaba grabando o algo falló al inicio, simplemente reseteamos el botón
            isRecording = false; // Asegurar estado
            if(audioLine != null && audioLine.isOpen()) { // Intentar cerrar si quedó abierto
                audioLine.stop();
                audioLine.close();
            }
            btnGrabarAudio.setSelected(false); // Deseleccionar botón
            btnGrabarAudio.setText("🎤 Grabar");
            // Mostramos error solo si audioStream era null (indicando fallo en startRecording)
            if(audioStream == null){
                JOptionPane.showMessageDialog(this, "La grabación no pudo iniciarse correctamente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        isRecording = false; // Detiene el hilo de lectura
        audioLine.stop();
        audioLine.close();

        if (currentChatTarget == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un chat antes de enviar un audio.", "Error", JOptionPane.ERROR_MESSAGE);
            btnGrabarAudio.setText("🎤 Grabar"); // Resetear botón
            return;
        }

        byte[] rawAudioData = audioStream.toByteArray();
        if (rawAudioData.length == 0) {
            JOptionPane.showMessageDialog(this, "La grabación está vacía.", "Error", JOptionPane.ERROR_MESSAGE);
            btnGrabarAudio.setText("🎤 Grabar"); // Resetear botón
            return;
        }

        try {
            // Convertimos los datos crudos a formato WAV en memoria
            ByteArrayOutputStream wavStream = new ByteArrayOutputStream();
            AudioFormat format = getAudioFormat(); // Assuming you have the getAudioFormat() method
            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(rawAudioData),
                    format,
                    rawAudioData.length / format.getFrameSize()
            );

            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavStream);
            byte[] wavAudioData = wavStream.toByteArray(); // Bytes con cabecera WAV

            // Preparamos DTO para SUBIR el audio (con Base64)
            MessageDTO uploadDTO = new MessageDTO();
            uploadDTO.setSender(usuarioActual.getUsername());
            uploadDTO.setRecipient(currentChatTarget.split(":", 2)[1]);
            uploadDTO.setAudioMessage(true);
            uploadDTO.setContent("Audio grabado por " + usuarioActual.getUsername()); // Placeholder

            String audioBase64 = Base64.getEncoder().encodeToString(wavAudioData);
            uploadDTO.setAudioDataBase64(audioBase64);

            // Enviamos paquete de subida
            Packet audioDataPacket = new Packet(ActionType.UPLOAD_AUDIO, uploadDTO);
            networkService.sendPacket(audioDataPacket);


            MessageDTO notificationDTO = new MessageDTO();
            notificationDTO.setSender(uploadDTO.getSender());
            notificationDTO.setRecipient(uploadDTO.getRecipient());
            notificationDTO.setAudioMessage(true);
            notificationDTO.setContent("Audio grabado"); // Placeholder, servidor lo reemplaza
            // notificationDTO.setAudioDataBase64(null); // No es necesario, es null por defecto

        // Crear el paquete de notificación al destino correcto (usuario o canal)
        Packet notificationPacket = new Packet(
            currentChatTarget.startsWith("channel:") ? ActionType.SEND_MESSAGE_TO_CHANNEL : ActionType.SEND_MESSAGE_TO_USER,
            notificationDTO
        );

            // Enviamos paquete de notificación
            networkService.sendPacket(notificationPacket);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al empaquetar o enviar el audio.", "Error de Envío", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Aseguramos que el botón se resetee después de intentar enviar
            btnGrabarAudio.setText("🎤 Grabar");
        }
    }

    private AudioFormat getAudioFormat() {
        // Definimos el formato estándar para WAV (PCM 16kHz, 16-bit, mono)
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
    }

    private void crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuCanales = new JMenu("Canales");

        // Ítem para Crear Canal (sin cambios)
        JMenuItem itemCrearCanal = new JMenuItem("Crear Nuevo Canal...");
        itemCrearCanal.addActionListener(e -> {
            String nombreCanal = JOptionPane.showInputDialog(
                    this,
                    "Introduce el nombre del nuevo canal (debe empezar con #):",
                    "Crear Canal",
                    JOptionPane.PLAIN_MESSAGE
            );
            if (nombreCanal != null && !nombreCanal.trim().isEmpty() && nombreCanal.trim().startsWith("#")) {
                ChannelDTO channelDTO = new ChannelDTO();
                channelDTO.setChannelName(nombreCanal.trim());
                channelDTO.setCreatorUsername(usuarioActual.getUsername());
                Packet packet = new Packet(ActionType.CREATE_CHANNEL_REQUEST, channelDTO);
                networkService.sendPacket(packet);
            } else if (nombreCanal != null) {
                JOptionPane.showMessageDialog(this, "El nombre del canal debe comenzar con #.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        });
        menuCanales.add(itemCrearCanal);

        // Ítem para Invitar Usuario con la nueva lógica
        JMenuItem itemInvitarUsuario = new JMenuItem("Invitar Usuario al Canal...");
        itemInvitarUsuario.addActionListener(e -> {
            String canalSeleccionado = listaCanales.getSelectedValue();
            // 1. Verificamos que un canal esté seleccionado
            if (canalSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona primero el canal al que deseas invitar.", "Canal no Seleccionado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Pedimos el nombre del usuario a invitar
            String usuarioAInvitar = JOptionPane.showInputDialog(
                    this,
                    "Escribe el nombre de usuario exacto a invitar al canal '" + canalSeleccionado + "':",
                    "Invitar Usuario",
                    JOptionPane.PLAIN_MESSAGE
            );

            // 3. Si el usuario escribió un nombre, enviamos la invitación
            if (usuarioAInvitar != null && !usuarioAInvitar.trim().isEmpty()) {
                InvitationDTO invitation = new InvitationDTO();
                invitation.setChannelName(canalSeleccionado);
                invitation.setInvitedUsername(usuarioAInvitar.trim());
                invitation.setInviterUsername(usuarioActual.getUsername());
                Packet packet = new Packet(ActionType.INVITE_USER_REQUEST, invitation);
                networkService.sendPacket(packet);
            }
        });
        menuCanales.add(itemInvitarUsuario);

        menuBar.add(menuCanales);
        setJMenuBar(menuBar);
    }

    private void addMessageToPanel(String chatTarget, String text, String audioFileName, boolean saveToCache) {
        // 1. Obtiene el panel (DEBE existir porque loadConversation ya lo creó)
        JPanel conversationPanel = conversationPanels.get(chatTarget);
        if (conversationPanel == null) { // Seguridad extra, aunque no debería pasar
            conversationPanel = new JPanel();
            conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
            conversationPanels.put(chatTarget, conversationPanel);
            // Si es la primera vez, aseguramos que se muestre si es el chat actual
            if (chatTarget.equals(currentChatTarget)) {
                chatScrollPane.setViewportView(conversationPanel);
            }
        }

        // 2. Crea la línea visual y la añade
        JPanel messageLinePanel = createVisualMessageLine(text, audioFileName);
        conversationPanel.add(messageLinePanel);

        // 3. Guarda en H2 si es necesario
        if (saveToCache) {
            MensajeCache msg = new MensajeCache();
            msg.setChatTarget(chatTarget);
            msg.setDisplayText(text);
            msg.setAudioFileName(audioFileName);
            msg.setTimestamp(LocalDateTime.now());
            msg.setOwnerUsername(usuarioActual.getUsername());
            cacheRepository.save(msg);
        }

        // 4. Actualiza la vista si estamos viendo ese chat
        if (chatTarget.equals(currentChatTarget)) {
            conversationPanel.revalidate();
            conversationPanel.repaint();
            // Scroll automático al final
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }
    }

    private JPanel createVisualMessageLine(String text, String audioFileName) {
        JPanel messageLinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        messageLinePanel.add(new JLabel(text));

        if (audioFileName != null) {
            JButton playButton = new JButton("▶ Play");
            playButton.addActionListener(e -> {
                // Pide el audio al servidor al hacer clic
                AudioRequestDTO request = new AudioRequestDTO();
                request.setFileName(audioFileName);
                networkService.sendPacket(new Packet(ActionType.DOWNLOAD_AUDIO_REQUEST, request));
            });
            messageLinePanel.add(playButton);
        }
        // Arreglo visual
        messageLinePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageLinePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, messageLinePanel.getPreferredSize().height));
        return messageLinePanel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"newPacket".equals(evt.getPropertyName())) return;
        Packet receivedPacket = (Packet) evt.getNewValue();

        SwingUtilities.invokeLater(() -> {
            switch (receivedPacket.getAction()) {

                case USER_LIST_UPDATE:
                    List<String> usernames = (List<String>) receivedPacket.getPayload();
                    modeloUsuarios.clear();
                    usernames.forEach(modeloUsuarios::addElement);
                    break;

                case CHANNEL_LIST_UPDATE:
                    List<String> channelNames = (List<String>) receivedPacket.getPayload();
                    modeloCanales.clear();
                    channelNames.forEach(modeloCanales::addElement);
                    break;

                case CREATE_CHANNEL_FAILURE: // Shows a notification
                    JOptionPane.showMessageDialog(this, (String) receivedPacket.getPayload(), "Error al Crear Canal", JOptionPane.ERROR_MESSAGE);
                    break;

                case CHANNEL_INVITATION: // Shows invitation dialog
                    InvitationDTO invitation = (InvitationDTO) receivedPacket.getPayload();
                    int response = JOptionPane.showConfirmDialog(
                            this,
                            invitation.getInviterUsername() + " te ha invitado a unirte al canal '" + invitation.getChannelName() + "'. ¿Aceptas?",
                            "Nueva Invitación de Canal",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    invitation.setAccepted(response == JOptionPane.YES_OPTION);
                    Packet responsePacket = new Packet(ActionType.INVITATION_RESPONSE, invitation);
                    networkService.sendPacket(responsePacket);
                    break;

                // --- BUG FIX ---
                case INVITE_SUCCESS:
                case INVITE_FAILURE:
                    // These cases now correctly show a notification and stop execution.
                    JOptionPane.showMessageDialog(this, (String) receivedPacket.getPayload(), "Estado de la Invitación", JOptionPane.INFORMATION_MESSAGE);
                    break; // <-- Added break to prevent fall-through
                // --- END BUG FIX ---

                case NEW_MESSAGE: // Handles text and audio messages
                    MessageDTO message = (MessageDTO) receivedPacket.getPayload();
                    String sender = message.getSender();
                    String recipient = message.getRecipient();
                    boolean isMyOwnMessage = sender.equals(usuarioActual.getUsername());

                    // Evitar eco duplicado SOLO para mensajes de texto privados (no para audios)
                    if (isMyOwnMessage && !recipient.startsWith("#") && !message.isAudioMessage()) {
                        return;
                    }

                    String chatTarget = recipient.startsWith("#") ?
                            "channel:" + recipient :
                            (isMyOwnMessage ? "user:" + recipient : "user:" + sender);

                    // --- INICIALIZACIÓN PARA EVITAR ERROR ---
                    String displayText = ""; // Inicializamos con cadena vacía
                    String audioFileName = null;
                    // --- FIN INICIALIZACIÓN ---

                    if (message.isAudioMessage()) {
                        audioFileName = message.getAudioFileName();
                        displayText = (recipient.startsWith("#") ? "[" + recipient + "] " : "") +
                                (isMyOwnMessage ? "Yo" : sender) +
                                " envió un audio: " + message.getContent(); // Content tiene la transcripción
                    } else {
                        if (isMyOwnMessage) {
                            displayText = "Yo -> " + recipient + ": " + message.getContent();
                        } else {
                            displayText = (recipient.startsWith("#") ? "[" + recipient + "] " : "") +
                                    sender + " dice: " + message.getContent();
                        }
                    }

                    // Ahora displayText siempre tiene un valor asignado
                    addMessageToPanel(chatTarget, displayText, audioFileName, true);
                    break;

                case AUDIO_DATA_RESPONSE:
                    MessageDTO audioDataMessage = (MessageDTO) receivedPacket.getPayload();
                    // 1. Get the Base64 String
                    String audioBase64 = audioDataMessage.getAudioDataBase64();
                    if (audioBase64 != null) {
                        try {
                            // 2. Decode the Base64 String back to bytes
                            byte[] audioData = Base64.getDecoder().decode(audioBase64);
                            // 3. Play the decoded bytes
                            playAudio(audioData);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Error: Received invalid Base64 audio data.");
                            JOptionPane.showMessageDialog(this, "Error al decodificar el audio recibido.", "Error de Audio", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        System.err.println("Error: Received AUDIO_DATA_RESPONSE with null Base64 data.");
                    }
                    break;

                case MESSAGE_HISTORY:
                    // Recibimos historial de mensajes de una conversación o canal
                    MessageHistoryDTO history = (MessageHistoryDTO) receivedPacket.getPayload();
                    String historyChat = history.getChatId(); // Username del contacto o nombre del canal
                    List<MessageDTO> messages = history.getMessages();
                    
                    System.out.println("📜 Historial recibido para " + historyChat + ": " + messages.size() + " mensajes");
                    
                    // Determinar si es un canal o usuario
                    String historyChatTarget = historyChat.startsWith("#") ? "channel:" + historyChat : "user:" + historyChat;
                    
                    // Agregar cada mensaje del historial al panel correspondiente
                    for (MessageDTO msg : messages) {
                        String senderName = msg.getSender();
                        boolean isOwnMessage = senderName.equals(usuarioActual.getUsername());
                        
                        String historyDisplayText;
                        String audioFile = null;
                        
                        if (msg.isAudioMessage()) {
                            audioFile = msg.getAudioFileName();
                            historyDisplayText = (historyChat.startsWith("#") ? "[" + historyChat + "] " : "") +
                                    (isOwnMessage ? "Yo" : senderName) +
                                    " envió un audio: " + msg.getContent();
                        } else {
                            if (isOwnMessage) {
                                historyDisplayText = "Yo -> " + msg.getRecipient() + ": " + msg.getContent();
                            } else {
                                historyDisplayText = (historyChat.startsWith("#") ? "[" + historyChat + "] " : "") +
                                        senderName + " dice: " + msg.getContent();
                            }
                        }
                        
                        // Agregar al panel SIN hacer scroll automático (historial)
                        addMessageToPanel(historyChatTarget, historyDisplayText, audioFile, false);
                    }
                    
                    System.out.println("✅ Historial de " + historyChat + " cargado en la interfaz");
                    break;

                default:
                    // Ignorar otras acciones no manejadas en la vista del chat
                    break;
            }
        });
    }
}