package com.chatrmi.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Componente para exibir mensagem em formato de bubble estilo WhatsApp
 */
public class MessageBubble extends JPanel {
    
    private static final Color SENT_MESSAGE_COLOR = new Color(0xDC, 0xF8, 0xC6); // Verde claro
    private static final Color RECEIVED_MESSAGE_COLOR = new Color(0xFF, 0xFF, 0xFF); // Branco
    private static final Color SENT_TEXT_COLOR = Color.BLACK;
    private static final Color RECEIVED_TEXT_COLOR = Color.BLACK;
    private static final Color TIMESTAMP_COLOR = new Color(0x99, 0x99, 0x99);
    
    private JLabel messageLabel;
    private JLabel timestampLabel;
    private boolean isSent;
    private String username;
    private String message;
    private String timestamp;
    
    public MessageBubble(String username, String message, String timestamp, boolean isSent) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.isSent = isSent;
        
        initializeComponent();
    }
    
    private void initializeComponent() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Painel principal da mensagem
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        
        // Bubble da mensagem - se ajusta ao tamanho do conteúdo
        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setOpaque(true);
        bubblePanel.setBackground(isSent ? SENT_MESSAGE_COLOR : RECEIVED_MESSAGE_COLOR);
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, isSent),
            new EmptyBorder(8, 12, 8, 12)
        ));
        // Permite que o bubble se ajuste ao tamanho do conteúdo (sem largura mínima)
        bubblePanel.setPreferredSize(null);
        
        // Label do nome do usuário (apenas para mensagens recebidas)
        if (!isSent && username != null && !username.equals("Sistema")) {
            JLabel usernameLabel = new JLabel(username);
            usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            usernameLabel.setForeground(new Color(0x00, 0x7A, 0xFF)); // Azul
            usernameLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
            bubblePanel.add(usernameLabel, BorderLayout.NORTH);
        }
        
        // Texto da mensagem - sempre usa HTML para permitir quebra de linha automática
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        int maxWidth = 280; // Largura máxima do bubble
        
        // Sempre usa HTML para permitir quebra de linha automática
        // Para textos curtos, o bubble se ajusta ao conteúdo
        // Para textos longos, quebra linha no max-width
        String htmlText = "<html><body style='max-width: " + maxWidth + "px; word-wrap: break-word; white-space: pre-wrap;'>" + 
            escapeHtml(message) + "</body></html>";
        messageLabel = new JLabel(htmlText);
        
        messageLabel.setFont(font);
        messageLabel.setForeground(isSent ? SENT_TEXT_COLOR : RECEIVED_TEXT_COLOR);
        messageLabel.setVerticalAlignment(SwingConstants.TOP);
        
        // Painel com mensagem e timestamp usando BoxLayout para se adaptar ao conteúdo
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(messageLabel);
        
        // Timestamp com data e hora
        timestampLabel = new JLabel(timestamp);
        timestampLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        timestampLabel.setForeground(TIMESTAMP_COLOR);
        timestampLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timestampLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        
        contentPanel.add(timestampLabel);
        bubblePanel.add(contentPanel, BorderLayout.CENTER);
        
        // Força o bubblePanel a se ajustar ao tamanho do conteúdo
        bubblePanel.setPreferredSize(null);
        bubblePanel.setMaximumSize(new Dimension(maxWidth + 50, Integer.MAX_VALUE));
        
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
                  .replace(">", "&gt;")
                  .replace("\n", "<br>");
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

