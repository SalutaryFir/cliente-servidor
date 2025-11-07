package com.universidad.chat.servidor.vista;

import com.universidad.chat.comun.dto.ServerInfoDTO;
import com.universidad.chat.servidor.model.Usuario; // Import Usuario
import com.universidad.chat.servidor.network.TCPServer;
import com.universidad.chat.servidor.service.ReporteService;
import com.universidad.chat.servidor.service.UsuarioService; // Import UsuarioService

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays; // For clearing password field
import java.util.List;

/**
 * Ventana de interfaz gráfica para el servidor del chat.
 * Muestra logs, usuarios conectados, permite generar informes y registrar nuevos usuarios.
 * Implementa PropertyChangeListener para observar cambios en TCPServer.
 */
public class ServidorVista extends JFrame implements PropertyChangeListener {

    // --- Services and Core Components ---
    private final TCPServer tcpServer;
    private final ReporteService reporteService;
    private final UsuarioService usuarioService; // Service for user actions

    // --- UI Components ---
    private JTextArea logArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JLabel statusLabel;
    private JButton btnGenerateReports;
    private JTextArea reportArea;

    // --- Registration UI Components ---
    private JTextField txtRegUsername;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPassword;
    private JButton btnRegisterUser;

    // --- Federation UI Components ---
    private JTable federationTable;
    private DefaultTableModel federationTableModel;
    private JButton btnConnectServer;
    private JButton btnDisconnectServer;
    private JTextField txtServerIP;
    private JTextField txtServerPort;

    /**
     * Constructor for the server GUI window.
     * @param tcpServer Instance of the running TCPServer.
     * @param reporteService Instance of the ReporteService.
     * @param usuarioService Instance of the UsuarioService.
     */
    public ServidorVista(TCPServer tcpServer, ReporteService reporteService, UsuarioService usuarioService) {
        this.tcpServer = tcpServer;
        this.reporteService = reporteService;
        this.usuarioService = usuarioService; // Save the service instance

        // Register this window as an observer of the TCPServer
        this.tcpServer.addPropertyChangeListener(this);

        // Basic window setup
    setTitle("Servidor Chat Universitario - " + tcpServer.getServerName() + " - Panel de Control");
        setSize(800, 700); // Slightly taller to accommodate registration panel
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Create and initialize UI components
        inicializarComponentes();
        // Build the visual layout
        construirLayout();
        // Add listeners for button actions
        inicializarListeners();

        // Update initial information when the window is shown
        updateStatusLabel();
        updateUserList(tcpServer.getAllAvailableUsernames());
        logMessage("Interfaz gráfica del servidor iniciada.");
    }

    /**
     * Initializes all the graphical interface components.
     */
    private void inicializarComponentes() {
        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        // User List
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        // Status Label
        statusLabel = new JLabel("Status: Iniciado | Conexiones: 0 / ?");

        // Report Button
        btnGenerateReports = new JButton("Generar Informes");

        // Report Area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setText("--- Informes del Servidor ---\n(Haz clic en 'Generar Informes' para actualizar)");

        // Registration Components
        txtRegUsername = new JTextField(15);
        txtRegEmail = new JTextField(15);
        txtRegPassword = new JPasswordField(15);
        btnRegisterUser = new JButton("Registrar Usuario");

        // Federation Components
        federationTableModel = new DefaultTableModel(new Object[]{"Servidor", "IP", "Puerto", "Clientes"}, 0);
        federationTable = new JTable(federationTableModel);
        federationTable.setFillsViewportHeight(true);
        txtServerIP = new JTextField(12);
        txtServerPort = new JTextField(5);
        txtServerPort.setText("5001"); // Default federation port
        btnConnectServer = new JButton("Conectar");
        btnDisconnectServer = new JButton("Desconectar");
    }

    /**
     * Builds the visual layout of the components within the window.
     */
    private void construirLayout() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // Top panel for status and report button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(statusLabel);
        topPanel.add(btnGenerateReports);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Main horizontal split pane (Left: Logs/Reports/Reg, Right: Users)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.65); // Give more initial space to the left side

        // Left side: Vertical split (Top: Logs, Bottom: Reports/Registration)
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setResizeWeight(0.6); // Give more initial space to logs

        // Log Panel (Top Left)
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(new JLabel("Logs del Servidor:", SwingConstants.CENTER), BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        leftSplit.setTopComponent(logPanel);

        // Bottom Left: Another vertical split (Top: Reports, Bottom: Registration)
        JSplitPane bottomSplitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitLeft.setResizeWeight(0.6); // More initial space for reports

        // Report Panel (Middle Left)
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.add(new JLabel("Informes:", SwingConstants.CENTER), BorderLayout.NORTH);
        reportPanel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        bottomSplitLeft.setTopComponent(reportPanel);

        // Registration Panel (Bottom Left)
        JPanel registrationPanel = new JPanel(new GridBagLayout());
        registrationPanel.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Usuario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(2, 5, 2, 5);
        registrationPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        registrationPanel.add(txtRegUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        registrationPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        registrationPanel.add(txtRegEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        registrationPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        registrationPanel.add(txtRegPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; // Reset weightx
        registrationPanel.add(btnRegisterUser, gbc);
        bottomSplitLeft.setBottomComponent(registrationPanel); // Add registration panel

        // Add the bottom split (Reports + Reg) to the left split
        leftSplit.setBottomComponent(bottomSplitLeft);

        // Set the completed left side to the main split
        mainSplit.setLeftComponent(leftSplit);

        // Right side: Vertical split (Top: User List, Bottom: Federation Panel)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setResizeWeight(0.5);

        // User List Panel (Top Right)
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(new JLabel("Usuarios Conectados:", SwingConstants.CENTER), BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        rightSplit.setTopComponent(userPanel);

        // Federation Panel (Bottom Right)
        JPanel federationPanel = new JPanel(new BorderLayout());
        federationPanel.setBorder(BorderFactory.createTitledBorder("Servidores Federados"));
        
        // Connection controls
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectPanel.add(new JLabel("IP:"));
        connectPanel.add(txtServerIP);
        connectPanel.add(new JLabel("Puerto:"));
        connectPanel.add(txtServerPort);
        connectPanel.add(btnConnectServer);
        connectPanel.add(btnDisconnectServer);
        federationPanel.add(connectPanel, BorderLayout.NORTH);
        
        // Federation table
        federationPanel.add(new JScrollPane(federationTable), BorderLayout.CENTER);
        rightSplit.setBottomComponent(federationPanel);

        // Add the right split to main split
        mainSplit.setRightComponent(rightSplit);

        // Add the main split pane to the center of the main panel
        mainPanel.add(mainSplit, BorderLayout.CENTER);
        // Add the main panel to the frame
        add(mainPanel);
    }

    /**
     * Adds ActionListeners to the buttons.
     */
    private void inicializarListeners() {
        // Action for the report button
        btnGenerateReports.addActionListener(e -> {
            logMessage("Generando informes...");
            // Run report generation in a separate thread to avoid freezing UI
            new Thread(() -> {
                try {
                    String reportText = reporteService.generarTodosLosReportesTexto();
                    // Update the report area on the EDT
                    SwingUtilities.invokeLater(() -> reportArea.setText(reportText));
                } catch (Exception ex) {
                    String errorMsg = "Error al generar informes: " + ex.getMessage();
                    logMessage(errorMsg);
                    SwingUtilities.invokeLater(() -> reportArea.setText(errorMsg));
                    ex.printStackTrace();
                }
            }).start();
        });

        // Action for the registration button
        btnRegisterUser.addActionListener(e -> handleRegisterUser());

        // Action for the federation connect button
        btnConnectServer.addActionListener(e -> handleConnectToServer());
        
        // Action for the federation disconnect button
        btnDisconnectServer.addActionListener(e -> handleDisconnectFromServer());
        
        // Listener para selección en la tabla de federación
        federationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && federationTable.getSelectedRow() != -1) {
                int selectedRow = federationTable.getSelectedRow();
                String ip = (String) federationTableModel.getValueAt(selectedRow, 1);
                Integer port = (Integer) federationTableModel.getValueAt(selectedRow, 2);
                
                txtServerIP.setText(ip);
                txtServerPort.setText(String.valueOf(port));
            }
        });

        // Timer para actualizar lista de servidores federados cada 5 segundos
        Timer federationUpdateTimer = new Timer(5000, e -> updateFederationTable());
        federationUpdateTimer.start();
    }

    /**
     * Handles the logic when the "Registrar Usuario" button is clicked.
     */
    private void handleRegisterUser() {
        String username = txtRegUsername.getText().trim();
        String email = txtRegEmail.getText().trim();
        char[] passwordChars = txtRegPassword.getPassword();
        String password = new String(passwordChars);

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
            Arrays.fill(passwordChars, ' '); // Clear password array even on error
            return;
        }

        // Create Usuario object (photo and IP are not handled in this simple GUI)
        Usuario newUser = new Usuario();
        newUser.setNombreUsuario(username);
        newUser.setEmail(email);
        newUser.setPassword(password); // Remember: Password should be hashed in a real app!

        try {
            Usuario savedUser = usuarioService.registrarUsuario(newUser);
            logMessage("Usuario registrado desde GUI: " + savedUser.getNombreUsuario());
            JOptionPane.showMessageDialog(this, "Usuario '" + savedUser.getNombreUsuario() + "' registrado con éxito.", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            // Clear fields after success
            txtRegUsername.setText("");
            txtRegEmail.setText("");
            txtRegPassword.setText("");

        } catch (IllegalStateException ex) {
            // Handle case where user/email already exists
            logMessage("Error al registrar usuario desde GUI: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error al registrar: " + ex.getMessage(), "Error de Registro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Handle other unexpected errors
            logMessage("Error inesperado al registrar usuario: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            Arrays.fill(passwordChars, ' '); // Always clear password array for security
        }
    }

    /**
     * Appends a message to the log area in a thread-safe manner.
     * @param message The message to log.
     */
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }

    /**
     * Updates the status label in a thread-safe manner.
     */
    private void updateStatusLabel() {
        SwingUtilities.invokeLater(() ->
                statusLabel.setText(
                        "Status: Corriendo | Conexiones: " +
                                tcpServer.getConnectedClientCount() + " / " +
                                tcpServer.getMaxConnections()
                )
        );
    }

    /**
     * Updates the list of connected users in a thread-safe manner.
     * @param usernames The new list of usernames.
     */
    private void updateUserList(List<String> usernames) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            if (usernames != null) {
                usernames.forEach(userListModel::addElement);
            }
        });
    }


    /**
     * Called by TCPServer's PropertyChangeSupport when observed properties change.
     * @param evt The event describing the change.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if ("clientListChanged".equals(propertyName)) {
            // Update the user list and connection count
            // Ensure we get the correct type from the event
            Object newValue = evt.getNewValue();
            if (newValue instanceof List) {
                @SuppressWarnings("unchecked") // We expect a List<String>
                List<String> usernames = (List<String>) newValue;
                updateUserList(usernames);
            }
            updateStatusLabel();
        } else if ("logMessage".equals(propertyName)) {
            // Add a message to the log area
            if (evt.getNewValue() instanceof String) {
                logMessage((String) evt.getNewValue());
            }
        } else if ("remoteUsers".equals(propertyName)) {
            // Actualización de usuarios remotos desde servidores federados
            if (evt.getNewValue() instanceof String) {
                String remoteUsersInfo = (String) evt.getNewValue();
                logMessage("📡 USUARIOS REMOTOS: " + remoteUsersInfo);
            }
        }
    }

    /**
     * Handles connecting to a remote server
     */
    private void handleConnectToServer() {
        String ip = txtServerIP.getText().trim();
        String portStr = txtServerPort.getText().trim();

        if (ip.isEmpty() || portStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa IP y puerto del servidor.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            logMessage("Intentando conectar a servidor " + ip + ":" + port);
            
            boolean success = tcpServer.getServerRegistry().connectToServer(ip, port);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Conectado exitosamente a " + ip + ":" + port, "Exito", JOptionPane.INFORMATION_MESSAGE);
                updateFederationTable();
                txtServerIP.setText("");
                txtServerPort.setText("5001");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo conectar. Verifica IP/puerto y que el servidor este activo.", "Error de Conexion", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El puerto debe ser un numero valido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles disconnecting from a selected server
     */
    private void handleDisconnectFromServer() {
        String ip = txtServerIP.getText().trim();
        String portStr = txtServerPort.getText().trim();
        
        if (ip.isEmpty() || portStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un servidor de la tabla o ingresa IP y puerto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            String serverKey = ip + ":" + port;
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas desconectar de " + serverKey + "?",
                "Confirmar Desconexión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                logMessage("Desconectando de servidor " + serverKey);
                tcpServer.getServerRegistry().unregisterServer(serverKey);
                updateFederationTable();
                txtServerIP.setText("");
                txtServerPort.setText("5001");
                JOptionPane.showMessageDialog(this, "Desconectado de " + serverKey, "Desconexión Exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El puerto debe ser un numero valido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza la tabla de servidores federados
     */
    private void updateFederationTable() {
        SwingUtilities.invokeLater(() -> {
            federationTableModel.setRowCount(0);
            
            for (ServerInfoDTO server : tcpServer.getServerRegistry().getFederatedServers()) {
                federationTableModel.addRow(new Object[]{
                    server.getServerName() != null ? server.getServerName() : "Desconocido",
                    server.getIpAddress(),
                    server.getFederationPort(),
                    server.getConnectedClients()
                });
            }
        });
    }
}
