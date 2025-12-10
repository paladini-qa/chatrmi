package com.chatrmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface de callback para notificações do servidor ao cliente
 */
public interface ChatClientCallback extends Remote {
    
    /**
     * Método chamado pelo servidor para notificar o cliente sobre uma nova mensagem
     * @param username Nome do usuário que enviou a mensagem
     * @param message Conteúdo da mensagem
     * @throws RemoteException
     */
    void onMessageReceived(String username, String message) throws RemoteException;
    
    /**
     * Método chamado pelo servidor para notificar sobre um novo arquivo
     * @param username Nome do usuário que enviou o arquivo
     * @param filename Nome do arquivo
     * @throws RemoteException
     */
    void onFileReceived(String username, String filename) throws RemoteException;
    
    /**
     * Método chamado pelo servidor para atualizar a lista de usuários
     * @param users Array com os nomes dos usuários online
     * @throws RemoteException
     */
    void onUsersUpdated(String[] users) throws RemoteException;
    
    // ========== CALLBACKS DE GRUPOS ==========
    
    /**
     * Notifica sobre um novo grupo criado
     * @param groupInfo Informações do grupo
     * @throws RemoteException
     */
    void onGroupCreated(com.chatrmi.interfaces.ChatService.GroupInfo groupInfo) throws RemoteException;
    
    /**
     * Notifica sobre convite recebido
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @param inviterUsername Nome do usuário que convidou
     * @throws RemoteException
     */
    void onGroupInviteReceived(String groupId, String groupName, String inviterUsername) throws RemoteException;
    
    /**
     * Notifica sobre solicitação de entrada recebida (para dono do grupo)
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @param requestingUsername Nome do usuário solicitando
     * @throws RemoteException
     */
    void onJoinRequestReceived(String groupId, String groupName, String requestingUsername) throws RemoteException;
    
    /**
     * Notifica sobre atualização em grupo (membro adicionado/removido)
     * @param groupName Nome do grupo
     * @param groupInfo Informações atualizadas do grupo
     * @throws RemoteException
     */
    void onGroupUpdated(String groupName, com.chatrmi.interfaces.ChatService.GroupInfo groupInfo) throws RemoteException;
    
    /**
     * Notifica sobre mensagem de grupo
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @param username Nome do usuário que enviou
     * @param message Mensagem
     * @throws RemoteException
     */
    void onGroupMessageReceived(String groupId, String groupName, String username, String message) throws RemoteException;
    
    /**
     * Notifica quando uma solicitação de entrada foi processada (aprovada/reprovada)
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @param approved true se aprovado, false se reprovado
     * @throws RemoteException
     */
    void onGroupJoinRequestProcessed(String groupId, String groupName, boolean approved) throws RemoteException;
    
    /**
     * Notifica quando foi adicionado a um grupo
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @throws RemoteException
     */
    void onAddedToGroup(String groupId, String groupName) throws RemoteException;
    
    /**
     * Notifica quando foi removido de um grupo
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @throws RemoteException
     */
    void onRemovedFromGroup(String groupId, String groupName) throws RemoteException;
    
    /**
     * Notifica sobre arquivo recebido em grupo
     * @param groupId ID do grupo
     * @param groupName Nome do grupo
     * @param username Nome do usuário que enviou
     * @param filename Nome do arquivo
     * @throws RemoteException
     */
    void onGroupFileReceived(String groupId, String groupName, String username, String filename) throws RemoteException;
    
    // GroupInfo é a mesma classe de ChatService, então não precisa duplicar
}

