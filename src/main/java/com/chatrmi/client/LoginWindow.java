package com.chatrmi.client;

import com.chatrmi.interfaces.ChatService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Janela de login com campos de usuário e senha
 */
public class LoginWindow extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton backButton;
    private LoginGUI parent;
    private String serverHost;
    
    public LoginWindow(LoginGUI parent, String serverHost) {
        this.parent = parent;
        this.serverHost = serverHost;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Login - Chat RMI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(50, 60, 50, 60));
        
        // Título
        JLabel titleLabel = new JLabel("<html><center><h2>Login</h2></center></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(Box.createVerticalStrut(30), BorderLayout.CENTER);
        
        // Painel central com campos
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Campo de usuário
        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        JLabel usernameLabel = new JLabel("Usuário:");
        usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        usernameField = new JTextField(25);
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(0, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        usernamePanel.add(usernameLabel, BorderLayout.NORTH);
        usernamePanel.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        usernamePanel.add(usernameField, BorderLayout.SOUTH);
        
        // Campo de senha
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        JLabel passwordLabel = new JLabel("Senha:");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField = new JPasswordField(25);
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(0, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordPanel.add(passwordLabel, BorderLayout.NORTH);
        passwordPanel.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        passwordPanel.add(passwordField, BorderLayout.SOUTH);
        
        centerPanel.add(usernamePanel);
        centerPanel.add(passwordPanel);
        
        // Painel de botões
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        loginButton = new JButton("LOGAR");
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(0, 50));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        loginButton.setBackground(new Color(0x25, 0xD3, 0x66));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(e -> performLogin());
        
        backButton = new JButton("VOLTAR");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        backButton.setPreferredSize(new Dimension(0, 40));
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.setBackground(Color.LIGHT_GRAY);
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> goBack());
        
        buttonsPanel.add(loginButton);
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(backButton);
        
        centerPanel.add(buttonsPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Permitir login com Enter
        getRootPane().setDefaultButton(loginButton);
        passwordField.addActionListener(e -> performLogin());
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, digite o nome de usuário.", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, digite a senha.", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Desabilitar botão durante tentativa de login
        loginButton.setEnabled(false);
        loginButton.setText("Conectando...");
        
        // Executar login em thread separada para não travar a UI
        new Thread(() -> {
            try {
                // Conectar ao servidor RMI
                Registry registry = LocateRegistry.getRegistry(serverHost, 1099);
                ChatService chatService = (ChatService) registry.lookup("ChatService");
                
                // Tentar fazer login
                boolean loginSuccess = chatService.login(username, password);
                
                SwingUtilities.invokeLater(() -> {
                    if (loginSuccess) {
                        // Login bem-sucedido - criar cliente e abrir interface principal
                        try {
                            ChatClient client = new ChatClient(username, serverHost);
                            ChatClientGUI gui = new ChatClientGUI(client);
                            gui.setVisible(true);
                            
                            boolean connected = client.connect();
                            if (!connected) {
                                StringBuilder errorMsg = new StringBuilder();
                                errorMsg.append("Erro ao conectar ao servidor em ").append(serverHost).append(".\n\n");
                                errorMsg.append("Verifique:\n");
                                errorMsg.append("1. O servidor está rodando?\n");
                                errorMsg.append("2. O IP está correto?\n");
                                errorMsg.append("3. Firewall permite conexões na porta 1099?\n");
                                errorMsg.append("4. Os PCs estão na mesma rede?\n");
                                
                                JOptionPane.showMessageDialog(gui, errorMsg.toString(),
                                        "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                                gui.dispose();
                                parent.showWindow();
                                this.setVisible(true);
                                loginButton.setEnabled(true);
                                loginButton.setText("LOGAR");
                                return;
                            }
                            
                            // Fechar janela de login
                            this.dispose();
                            parent.dispose();
                            
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(this,
                                    "Erro ao iniciar cliente: " + e.getMessage(),
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                            loginButton.setEnabled(true);
                            loginButton.setText("LOGAR");
                        }
                    } else {
                        // Login falhou
                        JOptionPane.showMessageDialog(this,
                                "Usuário ou senha incorretos.",
                                "Erro de Login", JOptionPane.ERROR_MESSAGE);
                        loginButton.setEnabled(true);
                        loginButton.setText("LOGAR");
                        passwordField.setText("");
                    }
                });
                
            } catch (RemoteException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao conectar ao servidor: " + e.getMessage(),
                            "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGAR");
                });
            } catch (NotBoundException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Serviço não encontrado no servidor.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGAR");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erro inesperado: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGAR");
                });
            }
        }).start();
    }
    
    private void goBack() {
        this.dispose();
        parent.showWindow();
    }
    
    public void setUsername(String username) {
        usernameField.setText(username);
    }
}

