package com.chatrmi.client;

import com.chatrmi.interfaces.ChatClientCallback;
import com.chatrmi.interfaces.ChatService;
import com.chatrmi.udp.UDPFileClient;
import com.chatrmi.udp.UDPFileDownloadClient;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Cliente do chat RMI com implementação de callback
 */
public class ChatClient extends UnicastRemoteObject implements ChatClientCallback {
    
    private ChatService chatService;
    private String username;
    private ChatClientGUI gui;
    private UDPFileClient udpFileClient;
    private UDPFileDownloadClient udpFileDownloadClient;
    private static final String SERVER_HOST = "localhost";
    private static final int UDP_FILE_PORT = 9876;
    private static final int UDP_DOWNLOAD_PORT = 9877;
    
    public ChatClient(String username) throws RemoteException {
        super();
        this.username = username;
        this.udpFileClient = new UDPFileClient(SERVER_HOST, UDP_FILE_PORT);
        this.udpFileDownloadClient = new UDPFileDownloadClient(SERVER_HOST, UDP_DOWNLOAD_PORT);
    }
    
    /**
     * Conecta ao servidor RMI
     */
    public boolean connect() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            chatService = (ChatService) registry.lookup("ChatService");
            
            // Registra o callback
            chatService.registerClient(username, this);
            
            // Obtém lista inicial de usuários
            String[] users = chatService.getOnlineUsers();
            if (gui != null) {
                gui.updateUsersList(users);
            }
            
            // Arquivos agora são mostrados via bubbles no chat
            
            return true;
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Desconecta do servidor
     */
    public void disconnect() {
        try {
            if (chatService != null) {
                chatService.unregisterClient(username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
    }
    
    /**
     * Envia uma mensagem
     */
    public void sendMessage(String message) {
        try {
            if (chatService != null) {
                chatService.sendMessage(username, message);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Envia um arquivo
     */
    public void sendFile(File file) {
        new Thread(() -> {
            boolean success = udpFileClient.sendFile(file, username);
            // Não mostra aqui porque já foi mostrado ao selecionar o arquivo
            // e também será mostrado via callback quando o servidor confirmar
        }).start();
    }
    
    /**
     * Callback: chamado pelo servidor quando uma mensagem é recebida
     */
    @Override
    public void onMessageReceived(String username, String message) throws RemoteException {
        if (gui != null) {
            gui.appendMessage(username, message);
        }
    }
    
    /**
     * Callback: chamado pelo servidor quando um arquivo é recebido
     */
    @Override
    public void onFileReceived(String username, String filename) throws RemoteException {
        if (gui != null) {
            // O servidor já filtra e não envia notificação para o próprio usuário
            // Então sempre mostra aqui (são arquivos de outras pessoas)
            gui.appendFile(username, filename);
        }
    }
    
    /**
     * Baixa um arquivo do servidor
     */
    public void downloadFile(String filename) {
        new Thread(() -> {
            if (gui != null) {
                gui.appendMessage("Sistema", "Baixando arquivo: " + filename + "...");
            }
            
            File downloadedFile = udpFileDownloadClient.downloadFile(filename);
            
            if (gui != null) {
                if (downloadedFile != null) {
                    gui.appendMessage("Sistema", "Arquivo baixado com sucesso: " + filename);
                    // Abre pasta de downloads
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(udpFileDownloadClient.getDownloadDir()));
                    } catch (Exception e) {
                        // Ignora erro ao abrir pasta
                    }
                } else {
                    gui.appendMessage("Sistema", "Erro ao baixar arquivo: " + filename);
                }
            }
        }).start();
    }
    
    /**
     * Obtém lista de arquivos disponíveis
     */
    public String[] getAvailableFiles() {
        try {
            if (chatService != null) {
                return chatService.getAvailableFiles();
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao obter lista de arquivos: " + e.getMessage());
        }
        return new String[0];
    }
    
    /**
     * Callback: chamado pelo servidor quando a lista de usuários é atualizada
     */
    @Override
    public void onUsersUpdated(String[] users) throws RemoteException {
        if (gui != null) {
            gui.updateUsersList(users);
        }
    }
    
    // ========== CALLBACKS DE GRUPOS ==========
    
    @Override
    public void onGroupCreated(com.chatrmi.interfaces.ChatService.GroupInfo groupInfo) throws RemoteException {
        if (gui != null) {
            gui.onGroupCreated(groupInfo);
        }
    }
    
    @Override
    public void onGroupInviteReceived(String groupId, String groupName, String inviterUsername) throws RemoteException {
        if (gui != null) {
            gui.onGroupInviteReceived(groupId, groupName, inviterUsername);
        }
    }
    
    @Override
    public void onJoinRequestReceived(String groupId, String groupName, String requestingUsername) throws RemoteException {
        if (gui != null) {
            gui.onJoinRequestReceived(groupId, groupName, requestingUsername);
        }
    }
    
    @Override
    public void onGroupUpdated(String groupName, com.chatrmi.interfaces.ChatService.GroupInfo groupInfo) throws RemoteException {
        if (gui != null) {
            gui.onGroupUpdated(groupName, groupInfo);
        }
    }
    
    @Override
    public void onGroupMessageReceived(String groupId, String groupName, String username, String message) throws RemoteException {
        if (gui != null) {
            gui.onGroupMessageReceived(groupId, groupName, username, message);
        }
    }
    
    @Override
    public void onGroupJoinRequestProcessed(String groupId, String groupName, boolean approved) throws RemoteException {
        if (gui != null) {
            gui.onGroupJoinRequestProcessed(groupId, groupName, approved);
        }
    }
    
    @Override
    public void onAddedToGroup(String groupId, String groupName) throws RemoteException {
        if (gui != null) {
            gui.onAddedToGroup(groupId, groupName);
        }
    }
    
    /**
     * Define a interface gráfica
     */
    public void setGUI(ChatClientGUI gui) {
        this.gui = gui;
    }
    
    public String getUsername() {
        return username;
    }
    
    public ChatService getChatService() {
        return chatService;
    }
    
    // ========== MÉTODOS DE GRUPO PARA GUI ==========
    
    public String createGroup(String groupName) {
        try {
            if (chatService != null) {
                return chatService.createGroup(groupName, username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao criar grupo: " + e.getMessage());
        }
        return null;
    }
    
    public ChatService.GroupInfo[] getAvailableGroups() {
        try {
            if (chatService != null) {
                return chatService.getAvailableGroups();
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao obter grupos: " + e.getMessage());
        }
        return new ChatService.GroupInfo[0];
    }
    
    public ChatService.GroupInfo[] getUserGroups() {
        try {
            if (chatService != null) {
                return chatService.getUserGroups(username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao obter grupos do usuário: " + e.getMessage());
        }
        return new ChatService.GroupInfo[0];
    }
    
    public void inviteToGroup(String groupId, String invitedUsername) {
        try {
            if (chatService != null) {
                chatService.inviteToGroup(groupId, username, invitedUsername);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao convidar: " + e.getMessage());
            if (gui != null) {
                gui.appendMessage("Sistema", "Erro ao convidar: " + e.getMessage());
            }
        }
    }
    
    public void requestJoinGroup(String groupId) {
        try {
            if (chatService != null) {
                chatService.requestJoinGroup(groupId, username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao solicitar entrada: " + e.getMessage());
            if (gui != null) {
                gui.appendMessage("Sistema", "Erro ao solicitar entrada: " + e.getMessage());
            }
        }
    }
    
    public void processJoinRequest(String groupId, String requestingUsername, boolean approved) {
        try {
            if (chatService != null) {
                chatService.processJoinRequest(groupId, username, requestingUsername, approved);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao processar solicitação: " + e.getMessage());
            if (gui != null) {
                gui.appendMessage("Sistema", "Erro ao processar solicitação: " + e.getMessage());
            }
        }
    }
    
    public void processInvite(String groupId, boolean accepted) {
        try {
            if (chatService != null) {
                chatService.processInvite(groupId, username, accepted);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao processar convite: " + e.getMessage());
            if (gui != null) {
                gui.appendMessage("Sistema", "Erro ao processar convite: " + e.getMessage());
            }
        }
    }
    
    public void sendGroupMessage(String groupId, String message) {
        try {
            if (chatService != null) {
                chatService.sendGroupMessage(groupId, username, message);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao enviar mensagem de grupo: " + e.getMessage());
        }
    }
    
    public String[] getPendingRequests(String groupId) {
        try {
            if (chatService != null) {
                return chatService.getPendingRequests(groupId, username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao obter solicitações: " + e.getMessage());
        }
        return new String[0];
    }
    
    public ChatService.GroupInfo[] getPendingInvites() {
        try {
            if (chatService != null) {
                return chatService.getPendingInvites(username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao obter convites: " + e.getMessage());
        }
        return new ChatService.GroupInfo[0];
    }
    
    public void leaveGroup(String groupId) {
        try {
            if (chatService != null) {
                chatService.leaveGroup(groupId, username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao sair do grupo: " + e.getMessage());
            if (gui != null) {
                gui.appendMessage("Sistema", "Erro ao sair do grupo: " + e.getMessage());
            }
        }
    }
}

