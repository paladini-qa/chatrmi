package com.chatrmi.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Interface gráfica inicial com opções de Login e Cadastro
 */
public class LoginGUI extends JFrame {
    
    private JButton loginButton;
    private JButton registerButton;
    private String serverHost;
    
    public LoginGUI(String serverHost) {
        this.serverHost = serverHost;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Chat RMI - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(60, 60, 60, 60));
        
        // Título
        JLabel titleLabel = new JLabel("<html><center><h1>Chat RMI</h1></center></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Painel de botões
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBorder(new EmptyBorder(50, 0, 0, 0));
        
        loginButton = new JButton("LOGAR");
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        loginButton.setPreferredSize(new Dimension(0, 65));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        loginButton.setBackground(new Color(0x25, 0xD3, 0x66)); // Verde
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(e -> openLoginWindow());
        
        registerButton = new JButton("CADASTRAR");
        registerButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        registerButton.setPreferredSize(new Dimension(0, 65));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        registerButton.setBackground(new Color(100, 149, 237)); // Azul
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(e -> openRegisterWindow());
        
        buttonsPanel.add(loginButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        buttonsPanel.add(registerButton);
        
        mainPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void openLoginWindow() {
        LoginWindow loginWindow = new LoginWindow(this, serverHost);
        loginWindow.setVisible(true);
        this.setVisible(false);
    }
    
    private void openRegisterWindow() {
        RegisterWindow registerWindow = new RegisterWindow(this, serverHost);
        registerWindow.setVisible(true);
        this.setVisible(false);
    }
    
    public void showWindow() {
        this.setVisible(true);
    }
}

