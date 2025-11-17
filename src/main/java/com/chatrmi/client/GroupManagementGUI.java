package com.chatrmi.client;

import com.chatrmi.interfaces.ChatService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface gráfica para gerenciamento de grupos
 */
public class GroupManagementGUI extends JFrame {
    
    private ChatClient client;
    private JTabbedPane tabbedPane;
    private JList<String> availableGroupsList;
    private DefaultListModel<String> availableGroupsModel;
    private JList<String> myGroupsList;
    private DefaultListModel<String> myGroupsModel;
    private JList<String> invitesList;
    private DefaultListModel<String> invitesModel;
    private Map<String, ChatService.GroupInfo> groupInfoMap;
    private Map<String, String> groupNameToIdMap;
    private Map<String, GroupChatGUI> openGroupChats; // groupId -> GroupChatGUI
    
    public GroupManagementGUI(ChatClient client) {
        this.client = client;
        this.groupInfoMap = new HashMap<>();
        this.groupNameToIdMap = new HashMap<>();
        this.openGroupChats = new HashMap<>();
        initializeGUI();
        refreshAll();
    }
    
    private void initializeGUI() {
        setTitle("Gerenciamento de Grupos - " + client.getUsername());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Grupos Disponíveis", createAvailableGroupsPanel());
        tabbedPane.addTab("Meus Grupos", createMyGroupsPanel());
        tabbedPane.addTab("Convites", createInvitesPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> refreshAll());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createAvailableGroupsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Grupos Disponíveis:");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        availableGroupsModel = new DefaultListModel<>();
        availableGroupsList = new JList<>(availableGroupsModel);
        availableGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(availableGroupsList);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Criar Grupo");
        JButton requestButton = new JButton("Solicitar Entrada");
        
        createButton.addActionListener(e -> createGroup());
        requestButton.addActionListener(e -> requestJoinGroup());
        
        buttonPanel.add(createButton);
        buttonPanel.add(requestButton);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMyGroupsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Meus Grupos:");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        myGroupsModel = new DefaultListModel<>();
        myGroupsList = new JList<>(myGroupsModel);
        myGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        myGroupsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openGroupChat();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(myGroupsList);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton openChatButton = new JButton("Abrir Chat");
        JButton inviteButton = new JButton("Convidar Usuário");
        JButton requestsButton = new JButton("Ver Solicitações");
        JButton leaveButton = new JButton("Sair");
        
        openChatButton.addActionListener(e -> openGroupChat());
        inviteButton.addActionListener(e -> inviteUser());
        requestsButton.addActionListener(e -> showRequests());
        leaveButton.addActionListener(e -> leaveGroup());
        
        buttonPanel.add(openChatButton);
        buttonPanel.add(inviteButton);
        buttonPanel.add(requestsButton);
        buttonPanel.add(leaveButton);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInvitesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Convites Pendentes:");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        invitesModel = new DefaultListModel<>();
        invitesList = new JList<>(invitesModel);
        invitesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(invitesList);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton acceptButton = new JButton("Aceitar");
        JButton rejectButton = new JButton("Rejeitar");
        
        acceptButton.addActionListener(e -> processInvite(true));
        rejectButton.addActionListener(e -> processInvite(false));
        
        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(
            this,
            "Digite o nome do grupo:",
            "Criar Grupo",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (groupName != null && !groupName.trim().isEmpty()) {
            String groupId = client.createGroup(groupName.trim());
            if (groupId != null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Grupo criado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
                );
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Erro ao criar grupo",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void requestJoinGroup() {
        String selected = availableGroupsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um grupo primeiro",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String groupId = groupNameToIdMap.get(selected);
        if (groupId != null) {
            client.requestJoinGroup(groupId);
            JOptionPane.showMessageDialog(
                this,
                "Solicitação enviada ao dono do grupo",
                "Solicitação Enviada",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void inviteUser() {
        String selected = myGroupsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um grupo primeiro",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String groupId = groupNameToIdMap.get(selected);
        if (groupId == null) return;
        
        ChatService.GroupInfo groupInfo = groupInfoMap.get(groupId);
        if (groupInfo == null || !groupInfo.getOwner().equals(client.getUsername())) {
            JOptionPane.showMessageDialog(
                this,
                "Apenas o dono do grupo pode convidar usuários",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        String[] users = new String[0];
        try {
            users = client.getChatService().getOnlineUsers();
        } catch (java.rmi.RemoteException e) {
            e.printStackTrace();
        }
        if (users == null || users.length == 0) {
            JOptionPane.showMessageDialog(
                this,
                "Não há usuários online",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String selectedUser = (String) JOptionPane.showInputDialog(
            this,
            "Selecione o usuário para convidar:",
            "Convidar Usuário",
            JOptionPane.PLAIN_MESSAGE,
            null,
            users,
            users[0]
        );
        
        if (selectedUser != null) {
            client.inviteToGroup(groupId, selectedUser);
            JOptionPane.showMessageDialog(
                this,
                "Convite enviado para " + selectedUser,
                "Convite Enviado",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void showRequests() {
        String selected = myGroupsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um grupo primeiro",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String groupId = groupNameToIdMap.get(selected);
        if (groupId == null) return;
        
        ChatService.GroupInfo groupInfo = groupInfoMap.get(groupId);
        if (groupInfo == null || !groupInfo.getOwner().equals(client.getUsername())) {
            JOptionPane.showMessageDialog(
                this,
                "Apenas o dono do grupo pode ver solicitações",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        String[] requests = client.getPendingRequests(groupId);
        if (requests == null || requests.length == 0) {
            JOptionPane.showMessageDialog(
                this,
                "Não há solicitações pendentes",
                "Solicitações",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        showRequestsDialog(groupId, requests);
    }
    
    private void showRequestsDialog(String groupId, String[] requests) {
        JDialog dialog = new JDialog(this, "Solicitações Pendentes", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JList<String> requestsList = new JList<>(requests);
        JScrollPane scrollPane = new JScrollPane(requestsList);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton approveButton = new JButton("Aprovar");
        JButton rejectButton = new JButton("Reprovar");
        
        approveButton.addActionListener(e -> {
            String selected = requestsList.getSelectedValue();
            if (selected != null) {
                client.processJoinRequest(groupId, selected, true);
                dialog.dispose();
                refreshAll();
            }
        });
        
        rejectButton.addActionListener(e -> {
            String selected = requestsList.getSelectedValue();
            if (selected != null) {
                client.processJoinRequest(groupId, selected, false);
                dialog.dispose();
                refreshAll();
            }
        });
        
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void openGroupChat() {
        String selected = myGroupsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um grupo primeiro",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String groupId = groupNameToIdMap.get(selected);
        if (groupId == null) return;
        
        ChatService.GroupInfo groupInfo = groupInfoMap.get(groupId);
        if (groupInfo == null) {
            JOptionPane.showMessageDialog(
                this,
                "Informações do grupo não encontradas",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        String groupName = groupInfo.getGroupName();
        
        GroupChatGUI existingChat = openGroupChats.get(groupId);
        if (existingChat != null && existingChat.isVisible()) {
            existingChat.toFront();
            existingChat.requestFocus();
            return;
        }
        
        GroupChatGUI groupChat = new GroupChatGUI(client, groupId, groupName, groupInfo);
        openGroupChats.put(groupId, groupChat);
        
        groupChat.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                openGroupChats.remove(groupId);
            }
        });
        
        groupChat.setVisible(true);
    }
    
    public void onGroupMessageReceived(String groupId, String groupName, String username, String message) {
        GroupChatGUI chatWindow = openGroupChats.get(groupId);
        if (chatWindow != null) {
            boolean isSent = username.equals(client.getUsername());
            if (!isSent) {
                chatWindow.appendMessage(username, message, false);
            }
        }
    }
    
    public void onGroupFileReceived(String groupId, String groupName, String username, String filename) {
        GroupChatGUI chatWindow = openGroupChats.get(groupId);
        if (chatWindow != null) {
            boolean isSent = username.equals(client.getUsername());
            chatWindow.appendFile(username, filename, isSent);
        }
    }
    
    public void onGroupUpdated(String groupName, ChatService.GroupInfo groupInfo) {
        String groupId = groupInfo.getGroupId();
        GroupChatGUI chatWindow = openGroupChats.get(groupId);
        if (chatWindow != null) {
            chatWindow.updateGroupInfo(groupInfo);
        }
    }
    
    private void leaveGroup() {
        String selected = myGroupsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um grupo primeiro",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String groupId = groupNameToIdMap.get(selected);
        if (groupId == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente sair do grupo " + selected + "?",
            "Confirmar Saída",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.leaveGroup(groupId);
            refreshAll();
        }
    }
    
    private void processInvite(boolean accepted) {
        String selected = invitesList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um convite primeiro",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String groupId = groupNameToIdMap.get(selected);
        if (groupId != null) {
            client.processInvite(groupId, accepted);
            refreshAll();
        }
    }
    
    public void refreshAll() {
        refreshAvailableGroups();
        refreshMyGroups();
        refreshInvites();
    }
    
    private void refreshAvailableGroups() {
        SwingUtilities.invokeLater(() -> {
            availableGroupsModel.clear();
            groupInfoMap.clear();
            groupNameToIdMap.clear();
            
            ChatService.GroupInfo[] groups = client.getAvailableGroups();
            for (ChatService.GroupInfo group : groups) {
                String displayName = group.getGroupName() + " (" + group.getMemberCount() + " membros)";
                availableGroupsModel.addElement(displayName);
                groupInfoMap.put(group.getGroupId(), group);
                groupNameToIdMap.put(displayName, group.getGroupId());
            }
        });
    }
    
    private void refreshMyGroups() {
        SwingUtilities.invokeLater(() -> {
            myGroupsModel.clear();
            
            ChatService.GroupInfo[] groups = client.getUserGroups();
            for (ChatService.GroupInfo group : groups) {
                String displayName = group.getGroupName();
                if (group.getOwner().equals(client.getUsername())) {
                    displayName += " (Dono)";
                }
                displayName += " (" + group.getMemberCount() + " membros)";
                myGroupsModel.addElement(displayName);
                groupInfoMap.put(group.getGroupId(), group);
                groupNameToIdMap.put(displayName, group.getGroupId());
            }
        });
    }
    
    private void refreshInvites() {
        SwingUtilities.invokeLater(() -> {
            invitesModel.clear();
            
            ChatService.GroupInfo[] invites = client.getPendingInvites();
            for (ChatService.GroupInfo group : invites) {
                String displayName = group.getGroupName() + " (por " + group.getOwner() + ")";
                invitesModel.addElement(displayName);
                groupInfoMap.put(group.getGroupId(), group);
                groupNameToIdMap.put(displayName, group.getGroupId());
            }
        });
    }
    
    public void onGroupCreated(ChatService.GroupInfo groupInfo) {
        refreshAll();
    }
    
    public void onGroupInviteReceived(String groupId, String groupName, String inviterUsername) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                inviterUsername + " convidou você para o grupo: " + groupName,
                "Novo Convite",
                JOptionPane.INFORMATION_MESSAGE
            );
            refreshAll();
        });
    }
    
    public void onJoinRequestReceived(String groupId, String groupName, String requestingUsername) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                requestingUsername + " solicitou entrada no grupo: " + groupName,
                "Nova Solicitação",
                JOptionPane.INFORMATION_MESSAGE
            );
            refreshAll();
        });
    }
    
    public void onAddedToGroup(String groupId, String groupName) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                "Você foi adicionado ao grupo: " + groupName,
                "Entrou no Grupo",
                JOptionPane.INFORMATION_MESSAGE
            );
            refreshAll();
        });
    }
    
    public void onGroupJoinRequestProcessed(String groupId, String groupName, boolean approved) {
        SwingUtilities.invokeLater(() -> {
            String message = approved 
                ? "Sua solicitação para o grupo " + groupName + " foi aprovada!"
                : "Sua solicitação para o grupo " + groupName + " foi reprovada.";
            JOptionPane.showMessageDialog(
                this,
                message,
                approved ? "Aprovado" : "Reprovado",
                approved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
            );
            refreshAll();
        });
    }
}

