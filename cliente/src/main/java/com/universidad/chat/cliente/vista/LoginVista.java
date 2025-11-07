package com.universidad.chat.cliente.vista;

import com.universidad.chat.cliente.repository.MensajeCacheRepository;
import com.universidad.chat.cliente.service.NetworkService;
import com.universidad.chat.comun.dto.ActionType;
import com.universidad.chat.comun.dto.Packet;
import com.universidad.chat.comun.dto.UserDTO;
import com.universidad.chat.comun.dto.LoginSuccessDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class LoginVista extends JFrame implements PropertyChangeListener {

    // --- Componentes de la Interfaz ---
    private JPanel panelPrincipal;
    private JPanel panelFormulario;
    private JLabel lblTitulo;
    private JLabel lblEmail;
    private JLabel lblPassword;
    private JLabel lblUsername;
    private JLabel lblConfirmPassword;
    private JLabel lblToggle;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtUsername;
    private JPasswordField txtConfirmPassword;
    private JButton btnAccion;

    // --- Lógica de la Vista ---
    private boolean esModoLogin = true;
    private final NetworkService networkService; // Referencia al servicio de red
    private final MensajeCacheRepository cacheRepository;

    /**
     * Constructor que recibe el servicio de red para la comunicación.
     * @param networkService La instancia del servicio que se conecta al servidor.
     */
    public LoginVista(NetworkService networkService, MensajeCacheRepository cacheRepository) {
        this.networkService = networkService;
        this.cacheRepository = cacheRepository; // <-- Guardar la referencia
        this.networkService.addPropertyChangeListener(this);

        // 1. Configuración básica de la ventana
        setTitle("Chat Universitario - Acceso");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 2. Creación de la interfaz
        inicializarComponentes();
        construirInterfaz();
        inicializarListeners(); // <-- Conectamos los botones a las acciones
    }

    private void inicializarComponentes() {
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelFormulario = new JPanel(new GridBagLayout());

        lblTitulo = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));

        lblEmail = new JLabel("Email:");
        lblPassword = new JLabel("Contraseña:");
        lblUsername = new JLabel("Nombre de Usuario:");
        lblConfirmPassword = new JLabel("Confirmar Contraseña:");
        lblToggle = new JLabel("<html><u>¿No tienes una cuenta? Regístrate aquí.</u></html>");
        lblToggle.setForeground(Color.BLUE);
        lblToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        txtEmail = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtUsername = new JTextField(20);
        txtConfirmPassword = new JPasswordField(20);

        btnAccion = new JButton("Iniciar Sesión");
    }

    private void construirInterfaz() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 0: Email
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(lblEmail, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulario.add(txtEmail, gbc);

        // Fila 1: Contraseña
        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(lblPassword, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panelFormulario.add(txtPassword, gbc);

        // Fila 2: Username (para registro)
        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(lblUsername, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panelFormulario.add(txtUsername, gbc);

        // Fila 3: Confirmar Contraseña (para registro)
        gbc.gridx = 0; gbc.gridy = 3;
        panelFormulario.add(lblConfirmPassword, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panelFormulario.add(txtConfirmPassword, gbc);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelInferior.add(btnAccion);
        panelInferior.add(lblToggle);

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
        actualizarVistaSegunModo();
    }

    private void inicializarListeners() {
        // Listener para el botón principal (Login/Registro)
        btnAccion.addActionListener(e -> {
            if (esModoLogin) {
                // ¡Ahora implementamos la lógica de Login!
                handleLogin();
            } else {
                handleRegistro();
            }
        });

        // El listener de lblToggle no cambia
        lblToggle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                esModoLogin = !esModoLogin;
                actualizarVistaSegunModo();
            }
        });
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email y contraseña son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setPassword(password);

        Packet loginPacket = new Packet(ActionType.LOGIN_REQUEST, userDTO);
        networkService.sendPacket(loginPacket);
    }


    private void handleRegistro() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setEmail(email);
        userDTO.setPassword(password);

        Packet registroPacket = new Packet(ActionType.REGISTER_REQUEST, userDTO);
        networkService.sendPacket(registroPacket);

        JOptionPane.showMessageDialog(this, "Petición de registro enviada al servidor.", "Registro Enviado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarVistaSegunModo() {
        if (esModoLogin) {
            lblUsername.setVisible(false);
            txtUsername.setVisible(false);
            lblConfirmPassword.setVisible(false);
            txtConfirmPassword.setVisible(false);

            lblTitulo.setText("Iniciar Sesión");
            btnAccion.setText("Iniciar Sesión");
            lblToggle.setText("<html><u>¿No tienes una cuenta? Regístrate aquí.</u></html>");
        } else {
            lblUsername.setVisible(true);
            txtUsername.setVisible(true);
            lblConfirmPassword.setVisible(true);
            txtConfirmPassword.setVisible(true);

            lblTitulo.setText("Registro de Usuario");
            btnAccion.setText("Registrarse");
            lblToggle.setText("<html><u>¿Ya tienes una cuenta? Inicia sesión.</u></html>");
        }
        // Reajusta el tamaño de la ventana para que se adapte a los componentes visibles
        this.pack();
        this.setSize(400, getHeight());
    }

    // Este método se ejecuta AUTOMÁTICAMENTE en un hilo de fondo cuando el NetworkService recibe un paquete.
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("newPacket".equals(evt.getPropertyName())) {
            Packet receivedPacket = (Packet) evt.getNewValue();

            SwingUtilities.invokeLater(() -> {
                switch (receivedPacket.getAction()) {
                    case REGISTER_SUCCESS:
                        JOptionPane.showMessageDialog(this, (String) receivedPacket.getPayload(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        esModoLogin = true;
                        actualizarVistaSegunModo();
                        break;

                    case REGISTER_FAILURE:
                        JOptionPane.showMessageDialog(this, (String) receivedPacket.getPayload(), "Error de Registro", JOptionPane.ERROR_MESSAGE);
                        break;

                    // --- NUEVOS CASES PARA LOGIN ---
                    case LOGIN_SUCCESS:
                        // 1. Recibimos el DTO de bienvenida completo
                        LoginSuccessDTO loginPayload = (LoginSuccessDTO) receivedPacket.getPayload();
                        UserDTO userInfo = loginPayload.getUserInfo();
                        List<String> allUsernames = loginPayload.getAllUsernames();
                        List<String> allChannelNames = loginPayload.getAllChannelNames(); // <-- Extraemos la nueva lista

                        JOptionPane.showMessageDialog(this, "¡Bienvenido, " + userInfo.getUsername() + "!", "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);

                        this.dispose();

                        // 2. Pasamos AMBAS listas a la nueva ventana
                        new ChatPrincipalVista(networkService, userInfo, allUsernames, allChannelNames, cacheRepository).setVisible(true);
                        break;

                    case LOGIN_FAILURE:
                        JOptionPane.showMessageDialog(this, (String) receivedPacket.getPayload(), "Error de Login", JOptionPane.ERROR_MESSAGE);
                        break;
                    // --- FIN DE LOS NUEVOS CASES ---
                }
            });
        }
    }
}