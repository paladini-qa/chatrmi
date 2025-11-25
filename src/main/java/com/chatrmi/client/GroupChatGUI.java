package com.chatrmi.client;

import com.chatrmi.interfaces.ChatService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Interface gráfica para chat de grupo
 */
public class GroupChatGUI extends JFrame {
    
    private ChatClient client;
    private String groupId;
    private String groupName;
    private JPanel chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JList<String> membersList;
    private DefaultListModel<String> membersModel;
    private ChatService.GroupInfo groupInfo;
    
    public GroupChatGUI(ChatClient client, String groupId, String groupName, ChatService.GroupInfo groupInfo) {
        this.client = client;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupInfo = groupInfo;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Grupo: " + groupName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatPanel.setBackground(new Color(0xF0, 0xF0, 0xF0));
        
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(new Color(0xF0, 0xF0, 0xF0));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0xF0, 0xF0, 0xF0));
        
        chatArea.addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            }
        });
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        
        messageField = new JTextField();
        messageField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        messageField.addActionListener(e -> sendMessage());
        
        JButton fileButton = new JButton("Arq");
        fileButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        fileButton.setPreferredSize(new Dimension(40, 35));
        fileButton.setFocusPainted(false);
        fileButton.addActionListener(e -> selectAndSendFile());
        
        sendButton = new JButton("Enviar");
        sendButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        sendButton.setPreferredSize(new Dimension(80, 35));
        sendButton.setBackground(new Color(0x25, 0xD3, 0x66));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(200, 0));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel membersLabel = new JLabel("Membros:");
        membersLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        membersModel = new DefaultListModel<>();
        membersList = new JList<>(membersModel);
        membersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        JScrollPane membersScrollPane = new JScrollPane(membersList);
        
        updateMembersList();
        
        sidePanel.add(membersLabel, BorderLayout.NORTH);
        sidePanel.add(membersScrollPane, BorderLayout.CENTER);
        
        add(chatPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);
        
        messageField.requestFocus();
        
        // Restaurar histórico antes de mostrar mensagem de entrada
        restoreHistory();
        appendMessage("Sistema", "Você entrou no grupo: " + groupName, false);
    }
    
    /**
     * Restaura o histórico de mensagens do grupo
     */
    private void restoreHistory() {
        java.util.List<MessageHistory> history = client.getGroupHistory(groupId);
        for (MessageHistory msg : history) {
            String timestamp = msg.getTimestamp().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            
            if (msg.getType() == MessageHistory.MessageType.TEXT) {
                MessageBubble bubble = new MessageBubble(
                    msg.getUsername(), 
                    msg.getContent(), 
                    timestamp, 
                    msg.isSent()
                );
                chatArea.add(bubble);
                chatArea.add(Box.createVerticalStrut(4));
            } else {
                FileBubble fileBubble = new FileBubble(
                    msg.getUsername(), 
                    msg.getContent(), 
                    timestamp, 
                    msg.isSent(), 
                    client
                );
                chatArea.add(fileBubble);
                chatArea.add(Box.createVerticalStrut(4));
            }
        }
        chatArea.revalidate();
        chatArea.repaint();
        
        // Scroll para o final após restaurar
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private void updateMembersList() {
        if (groupInfo != null) {
            membersModel.clear();
            String[] members = groupInfo.getMembers();
            for (String member : members) {
                String displayName = member;
                if (member.equals(groupInfo.getOwner())) {
                    displayName += " (Dono)";
                }
                membersModel.addElement(displayName);
            }
        }
    }
    
    public void updateGroupInfo(ChatService.GroupInfo newGroupInfo) {
        this.groupInfo = newGroupInfo;
        SwingUtilities.invokeLater(() -> updateMembersList());
    }
    
    private void selectAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo para enviar");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            appendFile(client.getUsername(), selectedFile.getName(), true);
            client.sendFile(selectedFile);
        }
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            appendMessage(client.getUsername(), message, true);
            client.sendGroupMessage(groupId, message);
            messageField.setText("");
        }
    }
    
    public void appendMessage(String username, String message, boolean isSent) {
        SwingUtilities.invokeLater(() -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String timestamp = now.format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            
            // Salvar no histórico (exceto mensagens do sistema de entrada)
            // Mensagens enviadas pelo próprio usuário devem ser salvas
            if (isSent && (!"Sistema".equals(username) || !message.contains("entrou no grupo"))) {
                client.addToGroupHistory(groupId, username, message, isSent, MessageHistory.MessageType.TEXT);
            }
            
            MessageBubble bubble = new MessageBubble(username, message, timestamp, isSent);
            chatArea.add(bubble);
            chatArea.add(Box.createVerticalStrut(4));
            
            chatArea.revalidate();
            chatArea.repaint();
            
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }
    
    public void appendFile(String username, String filename, boolean isSent) {
        SwingUtilities.invokeLater(() -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String timestamp = now.format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            
            // Salvar no histórico (arquivos enviados pelo próprio usuário)
            if (isSent) {
                client.addToGroupHistory(groupId, username, filename, isSent, MessageHistory.MessageType.FILE);
            }
            
            FileBubble fileBubble = new FileBubble(username, filename, timestamp, isSent, client);
            chatArea.add(fileBubble);
            chatArea.add(Box.createVerticalStrut(4));
            
            chatArea.revalidate();
            chatArea.repaint();
            
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
}

