package com.chatrmi.client;

import com.chatrmi.interfaces.ChatClientCallback;
import com.chatrmi.interfaces.ChatService;
import com.chatrmi.udp.UDPFileClient;
import com.chatrmi.udp.UDPFileDownloadClient;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
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
    private String serverHost;
    private static final int UDP_FILE_PORT = 9876;
    private static final int UDP_DOWNLOAD_PORT = 9877;

    /**
     * Obtém o IP local do cliente (não loopback)
     */
    private static String getClientIP() {
        try {
            // Primeiro, verificar se já foi configurado via propriedade do sistema
            String hostname = System.getProperty("java.rmi.server.hostname");
            if (hostname != null && !hostname.isEmpty() && !hostname.equals("localhost")) {
                return hostname;
            }

            // Tentar obter IP da interface de rede principal (não loopback)
            for (NetworkInterface iface : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!iface.isLoopback() && iface.isUp()) {
                    for (InetAddress addr : java.util.Collections.list(iface.getInetAddresses())) {
                        if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                            String ip = addr.getHostAddress();
                            // Configurar para uso futuro
                            System.setProperty("java.rmi.server.hostname", ip);
                            return ip;
                        }
                    }
                }
            }

            // Fallback: usar getLocalHost()
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            System.setProperty("java.rmi.server.hostname", ip);
            return ip;
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível detectar o IP do cliente automaticamente. Usando 'localhost'.");
            System.err.println(
                    "Isso pode causar problemas com callbacks em rede. Configure java.rmi.server.hostname manualmente.");
            return "localhost";
        }
    }

    public ChatClient(String username, String serverHost) throws RemoteException {
        super();
        this.username = username;
        this.serverHost = serverHost != null ? serverHost : "localhost";

        // IMPORTANTE: Configurar o IP do cliente ANTES de exportar o objeto remoto
        // Isso permite que o servidor faça callbacks de volta ao cliente
        String clientIP = getClientIP();
        System.setProperty("java.rmi.server.hostname", clientIP);

        this.udpFileClient = new UDPFileClient(this.serverHost, UDP_FILE_PORT);
        this.udpFileDownloadClient = new UDPFileDownloadClient(this.serverHost, UDP_DOWNLOAD_PORT);
    }

    public boolean connect() {
        try {
            // Configurar timeout para conexão
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000");
            System.setProperty("sun.rmi.transport.tcp.readTimeout", "10000");

            Registry registry = LocateRegistry.getRegistry(serverHost, 1099);
            chatService = (ChatService) registry.lookup("ChatService");
            chatService.registerClient(username, this);

            String[] users = chatService.getOnlineUsers();
            if (gui != null) {
                gui.updateUsersList(users);
            }

            return true;
        } catch (java.rmi.ConnectException e) {
            System.err.println("\n[ERRO DE CONEXÃO]");
            System.err.println("Não foi possível conectar ao servidor em " + serverHost + ":1099");
            System.err.println("\nPossíveis causas:");
            System.err.println("1. O servidor não está rodando");
            System.err.println("2. O IP do servidor está incorreto");
            System.err.println("3. Firewall bloqueando a conexão (porta 1099)");
            System.err.println("4. Os PCs não estão na mesma rede");
            System.err.println("\nDetalhes: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (java.rmi.ConnectIOException e) {
            System.err.println("\n[ERRO DE CONEXÃO]");
            System.err.println("Erro de I/O ao conectar ao servidor em " + serverHost + ":1099");
            System.err.println("\nPossíveis causas:");
            System.err.println("1. Firewall bloqueando a conexão");
            System.err.println("2. Rede não acessível");
            System.err.println("3. Servidor não está escutando na porta 1099");
            System.err.println("\nDetalhes: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (NotBoundException e) {
            System.err.println("\n[ERRO]");
            System.err.println("O serviço 'ChatService' não foi encontrado no servidor");
            System.err.println("Verifique se o servidor foi iniciado corretamente");
            System.err.println("\nDetalhes: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (RemoteException e) {
            System.err.println("\n[ERRO REMOTO]");
            System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
            System.err.println("\nDetalhes: " + e.getClass().getName());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("\n[ERRO INESPERADO]");
            System.err.println("Erro inesperado ao conectar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            if (chatService != null) {
                chatService.unregisterClient(username);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
    }

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

    public void sendFile(File file) {
        new Thread(() -> {
            udpFileClient.sendFile(file, username);
        }).start();
    }

    @Override
    public void onMessageReceived(String username, String message) throws RemoteException {
        if (gui != null) {
            gui.appendMessage(username, message);
        }
    }

    @Override
    public void onFileReceived(String username, String filename) throws RemoteException {
        if (gui != null) {
            gui.appendFile(username, filename);
        }
    }

    public void downloadFile(String filename) {
        new Thread(() -> {
            if (gui != null) {
                gui.appendMessage("Sistema", "Baixando arquivo: " + filename + "...");
            }

            File downloadedFile = udpFileDownloadClient.downloadFile(filename);

            if (gui != null) {
                if (downloadedFile != null) {
                    gui.appendMessage("Sistema", "Arquivo baixado com sucesso: " + filename);
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(udpFileDownloadClient.getDownloadDir()));
                    } catch (Exception e) {
                    }
                } else {
                    gui.appendMessage("Sistema", "Erro ao baixar arquivo: " + filename);
                }
            }
        }).start();
    }

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

    @Override
    public void onUsersUpdated(String[] users) throws RemoteException {
        if (gui != null) {
            gui.updateUsersList(users);
        }
    }

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
    public void onJoinRequestReceived(String groupId, String groupName, String requestingUsername)
            throws RemoteException {
        if (gui != null) {
            gui.onJoinRequestReceived(groupId, groupName, requestingUsername);
        }
    }

    @Override
    public void onGroupUpdated(String groupName, com.chatrmi.interfaces.ChatService.GroupInfo groupInfo)
            throws RemoteException {
        if (gui != null) {
            gui.onGroupUpdated(groupName, groupInfo);
        }
    }

    @Override
    public void onGroupMessageReceived(String groupId, String groupName, String username, String message)
            throws RemoteException {
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

    public void setGUI(ChatClientGUI gui) {
        this.gui = gui;
    }

    public String getUsername() {
        return username;
    }

    public ChatService getChatService() {
        return chatService;
    }

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
