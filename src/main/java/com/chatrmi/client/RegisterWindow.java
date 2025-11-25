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
 * Janela de cadastro com campos de usuário e senha
 */
public class RegisterWindow extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private LoginGUI parent;
    private String serverHost;
    
    public RegisterWindow(LoginGUI parent, String serverHost) {
        this.parent = parent;
        this.serverHost = serverHost;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Cadastro - Chat RMI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(50, 60, 50, 60));
        
        // Título
        JLabel titleLabel = new JLabel("<html><center><h2>Cadastro</h2></center></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
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
        passwordPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
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
        
        // Campo de confirmação de senha
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout());
        confirmPasswordPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        JLabel confirmPasswordLabel = new JLabel("Confirmar Senha:");
        confirmPasswordLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        confirmPasswordField = new JPasswordField(25);
        confirmPasswordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(0, 40));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        confirmPasswordPanel.add(confirmPasswordLabel, BorderLayout.NORTH);
        confirmPasswordPanel.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        confirmPasswordPanel.add(confirmPasswordField, BorderLayout.SOUTH);
        
        centerPanel.add(usernamePanel);
        centerPanel.add(passwordPanel);
        centerPanel.add(confirmPasswordPanel);
        
        // Painel de botões
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        registerButton = new JButton("CADASTRAR");
        registerButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(0, 50));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        registerButton.setBackground(new Color(100, 149, 237));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(e -> performRegister());
        
        backButton = new JButton("VOLTAR");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        backButton.setPreferredSize(new Dimension(0, 40));
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.setBackground(Color.LIGHT_GRAY);
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> goBack());
        
        buttonsPanel.add(registerButton);
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(backButton);
        
        centerPanel.add(buttonsPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Permitir cadastro com Enter
        getRootPane().setDefaultButton(registerButton);
        confirmPasswordField.addActionListener(e -> performRegister());
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void performRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
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
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        }
        
        // Desabilitar botão durante tentativa de cadastro
        registerButton.setEnabled(false);
        registerButton.setText("Cadastrando...");
        
        // Executar cadastro em thread separada para não travar a UI
        new Thread(() -> {
            try {
                // Conectar ao servidor RMI
                Registry registry = LocateRegistry.getRegistry(serverHost, 1099);
                ChatService chatService = (ChatService) registry.lookup("ChatService");
                
                // Tentar cadastrar
                boolean registerSuccess = chatService.registerUser(username, password);
                
                SwingUtilities.invokeLater(() -> {
                    if (registerSuccess) {
                        // Cadastro bem-sucedido - mostrar mensagem e redirecionar para login
                        JOptionPane.showMessageDialog(this,
                                "Cadastro realizado com sucesso!\nVocê será redirecionado para a tela de login.",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Fechar janela de cadastro e abrir janela de login
                        this.dispose();
                        LoginWindow loginWindow = new LoginWindow(parent, serverHost);
                        loginWindow.setVisible(true);
                        // Preencher o campo de usuário na tela de login
                        loginWindow.setUsername(username);
                        
                    } else {
                        // Cadastro falhou (usuário já existe)
                        JOptionPane.showMessageDialog(this,
                                "Este nome de usuário já está em uso. Por favor, escolha outro.",
                                "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
                        registerButton.setEnabled(true);
                        registerButton.setText("CADASTRAR");
                        usernameField.setText("");
                        passwordField.setText("");
                        confirmPasswordField.setText("");
                    }
                });
                
            } catch (RemoteException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao conectar ao servidor: " + e.getMessage(),
                            "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                    registerButton.setEnabled(true);
                    registerButton.setText("CADASTRAR");
                });
            } catch (NotBoundException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Serviço não encontrado no servidor.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    registerButton.setEnabled(true);
                    registerButton.setText("CADASTRAR");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Erro inesperado: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    registerButton.setEnabled(true);
                    registerButton.setText("CADASTRAR");
                });
            }
        }).start();
    }
    
    private void goBack() {
        this.dispose();
        parent.showWindow();
    }
}

