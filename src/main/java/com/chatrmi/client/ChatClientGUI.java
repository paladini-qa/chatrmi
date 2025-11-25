package com.chatrmi.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Interface gráfica do cliente de chat
 */
public class ChatClientGUI extends JFrame {

    private ChatClient client;
    private JList<String> usersList;
    private DefaultListModel<String> usersListModel;
    private JLabel statusLabel;
    private JButton groupsButton;
    private JButton globalChatButton;
    private GroupManagementGUI groupManagementGUI;
    private GlobalChatGUI globalChatGUI;

    public ChatClientGUI(ChatClient client) {
        this.client = client;
        client.setGUI(this);
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Chat RMI - " + client.getUsername());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("<html><center><h2>Bem-vindo ao Chat RMI!</h2>" +
                "<p>Use os botões abaixo para acessar as funcionalidades:</p></center></html>");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(welcomeLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonsPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        globalChatButton = new JButton("CHAT GLOBAL");
        globalChatButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        globalChatButton.setPreferredSize(new Dimension(0, 60));
        globalChatButton.setBackground(new Color(0x25, 0xD3, 0x66)); // Verde WhatsApp
        globalChatButton.setForeground(Color.WHITE);
        globalChatButton.setFocusPainted(false);
        globalChatButton.addActionListener(e -> openGlobalChatWindow());

        groupsButton = new JButton("GRUPOS");
        groupsButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        groupsButton.setPreferredSize(new Dimension(0, 60));
        groupsButton.setBackground(new Color(100, 149, 237)); // Azul
        groupsButton.setForeground(Color.WHITE);
        groupsButton.setFocusPainted(false);
        groupsButton.addActionListener(e -> openGroupsWindow());

        buttonsPanel.add(globalChatButton);
        buttonsPanel.add(groupsButton);

        infoPanel.add(buttonsPanel, BorderLayout.SOUTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel groupsPanelTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton groupsButtonTop = new JButton("GRUPOS");
        groupsButtonTop.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        groupsButtonTop.setPreferredSize(new Dimension(220, 40));
        groupsButtonTop.setBackground(new Color(100, 149, 237));
        groupsButtonTop.setForeground(Color.WHITE);
        groupsButtonTop.addActionListener(e -> openGroupsWindow());
        groupsPanelTop.add(groupsButtonTop);

        JPanel mainSidePanel = new JPanel(new BorderLayout());

        JPanel usersPanel = new JPanel(new BorderLayout());
        JLabel usersLabel = new JLabel("Usuários Online:");
        usersLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        usersListModel = new DefaultListModel<>();
        usersList = new JList<>(usersListModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setPreferredSize(new Dimension(0, 200));

        usersPanel.add(usersLabel, BorderLayout.NORTH);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);

        mainSidePanel.add(usersPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Conectado");
        statusLabel.setForeground(Color.GREEN);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        sidePanel.add(groupsPanelTop, BorderLayout.NORTH);
        sidePanel.add(mainSidePanel, BorderLayout.CENTER);
        sidePanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
    }

    private void openGlobalChatWindow() {
        if (globalChatGUI == null || !globalChatGUI.isVisible()) {
            if (globalChatGUI == null) {
                globalChatGUI = new GlobalChatGUI(client);
            }
            globalChatGUI.setVisible(true);
            globalChatGUI.toFront();
            globalChatGUI.requestFocus();
        } else {
            globalChatGUI.toFront();
            globalChatGUI.requestFocus();
        }
    }

    private void selectAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo para enviar");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (globalChatGUI != null && globalChatGUI.isVisible()) {
                globalChatGUI.appendMessage("Sistema", "Enviando arquivo: " + selectedFile.getName() + "...", false);
            }
            client.sendFile(selectedFile);
        }
    }

    public void appendMessage(String username, String message) {
        boolean isSent = username.equals(client.getUsername());
        // Salvar no histórico mesmo se a janela não estiver aberta
        if (!isSent) {
            client.addToGlobalHistory(username, message, false, MessageHistory.MessageType.TEXT);
        }
        
        if (globalChatGUI != null && globalChatGUI.isVisible()) {
            if (!isSent) {
                globalChatGUI.appendMessage(username, message, false);
            }
        }
    }

    public void appendFile(String username, String filename) {
        boolean isSent = username.equals(client.getUsername());
        // Salvar no histórico mesmo se a janela não estiver aberta
        client.addToGlobalHistory(username, filename, isSent, MessageHistory.MessageType.FILE);
        
        if (globalChatGUI != null && globalChatGUI.isVisible()) {
            globalChatGUI.appendFile(username, filename, isSent);
        }
    }

    public void updateUsersList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            usersListModel.clear();
            for (String user : users) {
                usersListModel.addElement(user);
            }
            statusLabel.setText(users.length + " usuário(s) online");
        });
    }

    private void disconnect() {
        client.disconnect();
    }

    private void openGroupsWindow() {
        if (groupManagementGUI == null || !groupManagementGUI.isVisible()) {
            groupManagementGUI = new GroupManagementGUI(client);
            groupManagementGUI.setVisible(true);
            groupManagementGUI.toFront();
            groupManagementGUI.requestFocus();
        } else {
            groupManagementGUI.toFront();
            groupManagementGUI.requestFocus();
        }
    }

    public void onGroupCreated(com.chatrmi.interfaces.ChatService.GroupInfo groupInfo) {
        if (groupManagementGUI != null) {
            groupManagementGUI.onGroupCreated(groupInfo);
        }
    }

    public void onGroupInviteReceived(String groupId, String groupName, String inviterUsername) {
        if (globalChatGUI != null && globalChatGUI.isVisible()) {
            globalChatGUI.appendMessage("Sistema",
                    "Você recebeu um convite para o grupo: " + groupName + " (de " + inviterUsername + ")", false);
        }
        if (groupManagementGUI != null) {
            groupManagementGUI.onGroupInviteReceived(groupId, groupName, inviterUsername);
        }
    }

    public void onJoinRequestReceived(String groupId, String groupName, String requestingUsername) {
        if (globalChatGUI != null && globalChatGUI.isVisible()) {
            globalChatGUI.appendMessage("Sistema", requestingUsername + " solicitou entrada no grupo: " + groupName,
                    false);
        }
        if (groupManagementGUI != null) {
            groupManagementGUI.onJoinRequestReceived(groupId, groupName, requestingUsername);
        }
    }

    public void onGroupUpdated(String groupName, com.chatrmi.interfaces.ChatService.GroupInfo groupInfo) {
        if (groupManagementGUI != null) {
            groupManagementGUI.refreshAll();
            groupManagementGUI.onGroupUpdated(groupName, groupInfo);
        }
    }

    public void onGroupMessageReceived(String groupId, String groupName, String username, String message) {
        if (groupManagementGUI != null) {
            groupManagementGUI.onGroupMessageReceived(groupId, groupName, username, message);
        }
    }

    public void onGroupFileReceived(String groupId, String groupName, String username, String filename) {
        if (groupManagementGUI != null) {
            groupManagementGUI.onGroupFileReceived(groupId, groupName, username, filename);
        }
    }

    public void onGroupJoinRequestProcessed(String groupId, String groupName, boolean approved) {
        if (groupManagementGUI != null) {
            groupManagementGUI.onGroupJoinRequestProcessed(groupId, groupName, approved);
        }
    }

    public void onAddedToGroup(String groupId, String groupName) {
        if (globalChatGUI != null && globalChatGUI.isVisible()) {
            globalChatGUI.appendMessage("Sistema", "Você foi adicionado ao grupo: " + groupName, false);
        }
        if (groupManagementGUI != null) {
            groupManagementGUI.onAddedToGroup(groupId, groupName);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String serverHost = args.length > 0 ? args[0] : "localhost";
            
            // Abrir tela inicial de login/cadastro
            LoginGUI loginGUI = new LoginGUI(serverHost);
            loginGUI.setVisible(true);
        });
    }
}
