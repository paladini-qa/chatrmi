package com.chatrmi.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Componente para exibir arquivo em formato de bubble estilo WhatsApp
 */
public class FileBubble extends JPanel {
    
    private static final Color SENT_MESSAGE_COLOR = new Color(0xDC, 0xF8, 0xC6); // Verde claro
    private static final Color RECEIVED_MESSAGE_COLOR = new Color(0xFF, 0xFF, 0xFF); // Branco
    private static final Color TIMESTAMP_COLOR = new Color(0x99, 0x99, 0x99);
    
    private boolean isSent;
    private String username;
    private String filename;
    private String timestamp;
    private ChatClient client;
    
    public FileBubble(String username, String filename, String timestamp, boolean isSent, ChatClient client) {
        this.username = username;
        this.filename = filename;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.client = client;
        
        initializeComponent();
    }
    
    private void initializeComponent() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Painel principal da mensagem
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        
        // Bubble da mensagem
        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setOpaque(true);
        bubblePanel.setBackground(isSent ? SENT_MESSAGE_COLOR : RECEIVED_MESSAGE_COLOR);
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, isSent),
            new EmptyBorder(12, 12, 12, 12)
        ));
        
        // Label do nome do usuário (apenas para mensagens recebidas)
        if (!isSent && username != null && !username.equals("Sistema")) {
            JLabel usernameLabel = new JLabel(username);
            usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            usernameLabel.setForeground(new Color(0x00, 0x7A, 0xFF)); // Azul
            usernameLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
            bubblePanel.add(usernameLabel, BorderLayout.NORTH);
        }
        
        // Painel com informações do arquivo
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setOpaque(false);
        
        // Icone de arquivo e nome
        JPanel fileInfoPanel = new JPanel(new BorderLayout(10, 0));
        fileInfoPanel.setOpaque(false);
        
        JLabel fileIcon = new JLabel("[FILE]");
        fileIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        JLabel fileNameLabel = new JLabel("<html><b>" + escapeHtml(filename) + "</b></html>");
        fileNameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        fileInfoPanel.add(fileIcon, BorderLayout.WEST);
        fileInfoPanel.add(fileNameLabel, BorderLayout.CENTER);
        
        filePanel.add(fileInfoPanel, BorderLayout.CENTER);
        
        // Botão de download
        JButton downloadButton = new JButton("Baixar");
        downloadButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        downloadButton.setPreferredSize(new Dimension(80, 30));
        downloadButton.setBackground(new Color(0x25, 0xD3, 0x66)); // Verde WhatsApp
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        downloadButton.addActionListener(e -> {
            if (client != null) {
                client.downloadFile(filename);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        buttonPanel.add(downloadButton);
        
        // Timestamp e botão de download no mesmo painel inferior
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        // Timestamp
        JLabel timestampLabel = new JLabel(timestamp);
        timestampLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        timestampLabel.setForeground(TIMESTAMP_COLOR);
        
        // Layout: botão à esquerda, timestamp à direita
        JPanel bottomContentPanel = new JPanel(new BorderLayout());
        bottomContentPanel.setOpaque(false);
        bottomContentPanel.add(buttonPanel, BorderLayout.WEST);
        bottomContentPanel.add(timestampLabel, BorderLayout.EAST);
        
        bottomPanel.add(bottomContentPanel, BorderLayout.CENTER);
        filePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        bubblePanel.add(filePanel, BorderLayout.CENTER);
        
        messagePanel.add(bubblePanel, BorderLayout.CENTER);
        
        // Adiciona padding baseado na posição (esquerda ou direita)
        JPanel containerPanel = new JPanel(new FlowLayout(isSent ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        containerPanel.setOpaque(false);
        containerPanel.setBorder(new EmptyBorder(4, isSent ? 50 : 10, 4, isSent ? 10 : 50));
        containerPanel.add(messagePanel);
        
        add(containerPanel, BorderLayout.CENTER);
    }
    
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;");
    }
    
    /**
     * Classe para criar bordas arredondadas
     */
    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        private boolean isSent;
        
        RoundedBorder(int radius, boolean isSent) {
            this.radius = radius;
            this.isSent = isSent;
        }
        
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 1, this.radius + 1);
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xE0, 0xE0, 0xE0)); // Cor da borda
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }
}

