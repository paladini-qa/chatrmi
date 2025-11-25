package com.chatrmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota para o serviço de chat
 */
public interface ChatService extends Remote {
    
    /**
     * Envia uma mensagem para o servidor
     * @param username Nome do usuário
     * @param message Mensagem a ser enviada
     * @throws RemoteException
     */
    void sendMessage(String username, String message) throws RemoteException;
    
    /**
     * Registra um cliente para receber callbacks
     * @param username Nome do usuário
     * @param callback Interface de callback do cliente
     * @throws RemoteException
     */
    void registerClient(String username, ChatClientCallback callback) throws RemoteException;
    
    /**
     * Remove o registro de um cliente
     * @param username Nome do usuário
     * @throws RemoteException
     */
    void unregisterClient(String username) throws RemoteException;
    
    /**
     * Retorna a lista de usuários online
     * @return Array com os nomes dos usuários
     * @throws RemoteException
     */
    String[] getOnlineUsers() throws RemoteException;
    
    /**
     * Retorna a lista de arquivos disponíveis
     * @return Array com os nomes dos arquivos
     * @throws RemoteException
     */
    String[] getAvailableFiles() throws RemoteException;
    
    /**
     * Solicita o download de um arquivo (retorna informações para download via UDP)
     * @param filename Nome do arquivo a ser baixado
     * @return Informações do arquivo (nome e tamanho)
     * @throws RemoteException
     */
    FileInfo requestFileDownload(String filename) throws RemoteException;
    
    /**
     * Classe para informações de arquivo
     */
    class FileInfo implements java.io.Serializable {
        private String filename;
        private long fileSize;
        
        public FileInfo(String filename, long fileSize) {
            this.filename = filename;
            this.fileSize = fileSize;
        }
        
        public String getFilename() { return filename; }
        public long getFileSize() { return fileSize; }
    }
    
    // ========== MÉTODOS DE GRUPOS ==========
    
    /**
     * Cria um novo grupo
     * @param groupName Nome do grupo
     * @param ownerUsername Nome do criador (dono)
     * @return ID do grupo criado
     * @throws RemoteException
     */
    String createGroup(String groupName, String ownerUsername) throws RemoteException;
    
    /**
     * Retorna lista de todos os grupos disponíveis
     * @return Array com informações dos grupos
     * @throws RemoteException
     */
    GroupInfo[] getAvailableGroups() throws RemoteException;
    
    /**
     * Retorna grupos de um usuário específico
     * @param username Nome do usuário
     * @return Array com informações dos grupos
     * @throws RemoteException
     */
    GroupInfo[] getUserGroups(String username) throws RemoteException;
    
    /**
     * Convidar usuário para um grupo
     * @param groupId ID do grupo
     * @param inviterUsername Nome do usuário que está convidando (deve ser dono)
     * @param invitedUsername Nome do usuário convidado
     * @throws RemoteException
     */
    void inviteToGroup(String groupId, String inviterUsername, String invitedUsername) throws RemoteException;
    
    /**
     * Solicita entrada em um grupo
     * @param groupId ID do grupo
     * @param username Nome do usuário solicitando
     * @throws RemoteException
     */
    void requestJoinGroup(String groupId, String username) throws RemoteException;
    
    /**
     * Aprova ou reprova uma solicitação de entrada
     * @param groupId ID do grupo
     * @param ownerUsername Nome do dono
     * @param requestingUsername Nome do usuário solicitando
     * @param approved true para aprovar, false para reprovar
     * @throws RemoteException
     */
    void processJoinRequest(String groupId, String ownerUsername, String requestingUsername, boolean approved) throws RemoteException;
    
    /**
     * Processa um convite (aceita ou rejeita)
     * @param groupId ID do grupo
     * @param username Nome do usuário convidado
     * @param accepted true para aceitar, false para rejeitar
     * @throws RemoteException
     */
    void processInvite(String groupId, String username, boolean accepted) throws RemoteException;
    
    /**
     * Envia mensagem para um grupo
     * @param groupId ID do grupo
     * @param username Nome do usuário enviando
     * @param message Mensagem
     * @throws RemoteException
     */
    void sendGroupMessage(String groupId, String username, String message) throws RemoteException;
    
    /**
     * Retorna solicitações pendentes para um grupo (apenas para dono)
     * @param groupId ID do grupo
     * @param ownerUsername Nome do dono
     * @return Array com nomes dos usuários solicitando entrada
     * @throws RemoteException
     */
    String[] getPendingRequests(String groupId, String ownerUsername) throws RemoteException;
    
    /**
     * Retorna convites pendentes de um usuário
     * @param username Nome do usuário
     * @return Array com informações dos grupos que o convidaram
     * @throws RemoteException
     */
    GroupInfo[] getPendingInvites(String username) throws RemoteException;
    
    /**
     * Sai de um grupo
     * @param groupId ID do grupo
     * @param username Nome do usuário
     * @throws RemoteException
     */
    void leaveGroup(String groupId, String username) throws RemoteException;
    
    /**
     * Obtém informações de um grupo específico
     * @param groupId ID do grupo
     * @return Informações do grupo
     * @throws RemoteException
     */
    GroupInfo getGroupInfo(String groupId) throws RemoteException;
    
    // ========== MÉTODOS DE AUTENTICAÇÃO ==========
    
    /**
     * Registra um novo usuário no sistema
     * @param username Nome de usuário
     * @param password Senha do usuário
     * @return true se o cadastro foi bem-sucedido, false se o usuário já existe
     * @throws RemoteException
     */
    boolean registerUser(String username, String password) throws RemoteException;
    
    /**
     * Autentica um usuário
     * @param username Nome de usuário
     * @param password Senha do usuário
     * @return true se as credenciais estão corretas, false caso contrário
     * @throws RemoteException
     */
    boolean login(String username, String password) throws RemoteException;
    
    /**
     * Classe para informações de grupo
     */
    class GroupInfo implements java.io.Serializable {
        private String groupId;
        private String groupName;
        private String owner;
        private String[] members;
        private int memberCount;
        
        public GroupInfo(String groupId, String groupName, String owner, String[] members) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.owner = owner;
            this.members = members;
            this.memberCount = members.length;
        }
        
        public String getGroupId() { return groupId; }
        public String getGroupName() { return groupName; }
        public String getOwner() { return owner; }
        public String[] getMembers() { return members; }
        public int getMemberCount() { return memberCount; }
    }
}

