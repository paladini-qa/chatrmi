package com.chatrmi.server;

import com.chatrmi.model.Group;
import com.chatrmi.model.GroupRequest;
import com.chatrmi.interfaces.ChatClientCallback;
import com.chatrmi.interfaces.ChatService;
import com.chatrmi.observer.ChatObserver;
import com.chatrmi.observer.Subject;

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação do serviço de chat usando RMI
 * Utiliza o padrão Observer para gerenciar eventos
 */
public class ChatServiceImpl implements ChatService {
    
    private Map<String, ChatClientCallback> clients;
    private ChatSubject subject;
    private Map<String, Group> groups; // groupId -> Group
    private Map<String, Set<String>> userGroups; // username -> Set<groupId>
    private Map<String, Set<String>> pendingInvites; // username -> Set<groupId>
    private int groupIdCounter;
    
    public ChatServiceImpl() throws RemoteException {
        this.clients = new ConcurrentHashMap<>();
        this.subject = new ChatSubject();
        this.groups = new ConcurrentHashMap<>();
        this.userGroups = new ConcurrentHashMap<>();
        this.pendingInvites = new ConcurrentHashMap<>();
        this.groupIdCounter = 1;
    }
    
    @Override
    public void sendMessage(String username, String message) throws RemoteException {
        System.out.println("[" + username + "]: " + message);
        
        // Notifica observadores usando Observer pattern
        ChatObserver.MessageEvent event = new ChatObserver.MessageEvent(username, message);
        subject.notifyObservers(event);
        
        // Notifica todos os clientes via callback (servidor vai no cliente e roda update)
        broadcastMessage(username, message);
    }
    
    @Override
    public void registerClient(String username, ChatClientCallback callback) throws RemoteException {
        clients.put(username, callback);
        System.out.println("Cliente registrado: " + username);
        
        // Notifica sobre atualização de usuários
        String[] users = getOnlineUsers();
        ChatObserver.UserEvent event = new ChatObserver.UserEvent(users);
        subject.notifyObservers(event);
        
        // Notifica todos os clientes sobre a nova lista de usuários
        broadcastUsersUpdate();
    }
    
    @Override
    public void unregisterClient(String username) throws RemoteException {
        clients.remove(username);
        System.out.println("Cliente desconectado: " + username);
        
        // Notifica sobre atualização de usuários
        broadcastUsersUpdate();
    }
    
    @Override
    public String[] getOnlineUsers() throws RemoteException {
        Set<String> userSet = clients.keySet();
        return userSet.toArray(new String[0]);
    }
    
    @Override
    public String[] getAvailableFiles() throws RemoteException {
        File uploadDir = new File("uploads");
        if (!uploadDir.exists() || !uploadDir.isDirectory()) {
            return new String[0];
        }
        
        File[] files = uploadDir.listFiles(File::isFile);
        if (files == null) {
            return new String[0];
        }
        
        List<String> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(file.getName());
        }
        return fileList.toArray(new String[0]);
    }
    
    @Override
    public ChatService.FileInfo requestFileDownload(String filename) throws RemoteException {
        File file = new File("uploads", filename);
        if (!file.exists() || !file.isFile()) {
            throw new RemoteException("Arquivo não encontrado: " + filename);
        }
        return new ChatService.FileInfo(filename, file.length());
    }
    
    /**
     * Envia mensagem para todos os clientes usando callback
     */
    private void broadcastMessage(String username, String message) {
        clients.forEach((user, callback) -> {
            try {
                // Servidor vai no cliente e roda o método update (callback)
                callback.onMessageReceived(username, message);
            } catch (RemoteException e) {
                System.err.println("Erro ao enviar mensagem para " + user + ": " + e.getMessage());
                // Remove cliente desconectado
                clients.remove(user);
            }
        });
    }
    
    /**
     * Notifica todos os clientes sobre atualização na lista de usuários
     */
    private void broadcastUsersUpdate() {
        String[] users = clients.keySet().toArray(new String[0]);
        clients.forEach((user, callback) -> {
            try {
                // Servidor vai no cliente e roda o método update
                callback.onUsersUpdated(users);
            } catch (RemoteException e) {
                System.err.println("Erro ao atualizar lista de usuários para " + user + ": " + e.getMessage());
                clients.remove(user);
            }
        });
    }
    
    /**
     * Notifica clientes sobre recebimento de arquivo
     */
    public void notifyFileReceived(String username, String filename) {
        ChatObserver.FileEvent event = new ChatObserver.FileEvent(username, filename);
        subject.notifyObservers(event);
        
        clients.forEach((user, callback) -> {
            try {
                // Não notifica o próprio usuário que enviou (já foi mostrado ao enviar)
                if (!user.equals(username)) {
                    // Servidor vai no cliente e roda o método update
                    callback.onFileReceived(username, filename);
                }
            } catch (RemoteException e) {
                System.err.println("Erro ao notificar arquivo para " + user + ": " + e.getMessage());
                clients.remove(user);
            }
        });
    }
    
    /**
     * Retorna o subject para registro de observadores
     */
    public ChatSubject getSubject() {
        return subject;
    }
    
    // ========== IMPLEMENTAÇÃO DE MÉTODOS DE GRUPOS ==========
    
    @Override
    public String createGroup(String groupName, String ownerUsername) throws RemoteException {
        String groupId = "GROUP_" + groupIdCounter++;
        
        Group group = new Group(groupId, groupName, ownerUsername);
        groups.put(groupId, group);
        
        // Adiciona ao conjunto de grupos do usuário
        userGroups.computeIfAbsent(ownerUsername, k -> ConcurrentHashMap.newKeySet()).add(groupId);
        
        System.out.println("Grupo criado: " + groupName + " (ID: " + groupId + ", dono: " + ownerUsername + ")");
        
        // Notifica todos os clientes sobre o novo grupo
        ChatService.GroupInfo groupInfo = convertToGroupInfo(group);
        broadcastGroupCreated(groupInfo);
        
        return groupId;
    }
    
    @Override
    public ChatService.GroupInfo[] getAvailableGroups() throws RemoteException {
        return groups.values().stream()
            .map(this::convertToGroupInfo)
            .toArray(ChatService.GroupInfo[]::new);
    }
    
    @Override
    public ChatService.GroupInfo[] getUserGroups(String username) throws RemoteException {
        return groups.values().stream()
            .filter(g -> g.isMember(username))
            .map(this::convertToGroupInfo)
            .toArray(ChatService.GroupInfo[]::new);
    }
    
    @Override
    public void inviteToGroup(String groupId, String inviterUsername, String invitedUsername) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null || !group.isOwner(inviterUsername)) {
            throw new RemoteException("Grupo não encontrado ou usuário não é dono");
        }
        
        if (group.isMember(invitedUsername)) {
            throw new RemoteException("Usuário já é membro do grupo");
        }
        
        // Adiciona convite pendente
        pendingInvites.computeIfAbsent(invitedUsername, k -> ConcurrentHashMap.newKeySet()).add(groupId);
        
        System.out.println("Convite enviado: " + invitedUsername + " -> " + group.getGroupName());
        
        // Notifica o usuário convidado
        ChatClientCallback callback = clients.get(invitedUsername);
        if (callback != null) {
            try {
                callback.onGroupInviteReceived(groupId, group.getGroupName(), inviterUsername);
            } catch (RemoteException e) {
                System.err.println("Erro ao notificar convite: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void requestJoinGroup(String groupId, String username) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new RemoteException("Grupo não encontrado");
        }
        
        if (group.isMember(username)) {
            throw new RemoteException("Usuário já é membro do grupo");
        }
        
        GroupRequest existingRequest = group.getRequest(username);
        if (existingRequest != null) {
            throw new RemoteException("Solicitação já existe");
        }
        
        GroupRequest request = new GroupRequest(username, groupId, group.getGroupName());
        group.addRequest(request);
        
        System.out.println("Solicitação de entrada: " + username + " -> " + group.getGroupName());
        
        // Notifica o dono do grupo
        String owner = group.getOwner();
        ChatClientCallback callback = clients.get(owner);
        if (callback != null) {
            try {
                callback.onJoinRequestReceived(groupId, group.getGroupName(), username);
            } catch (RemoteException e) {
                System.err.println("Erro ao notificar solicitação: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void processJoinRequest(String groupId, String ownerUsername, String requestingUsername, boolean approved) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null || !group.isOwner(ownerUsername)) {
            throw new RemoteException("Grupo não encontrado ou usuário não é dono");
        }
        
        GroupRequest request = group.getRequest(requestingUsername);
        if (request == null) {
            throw new RemoteException("Não há solicitação pendente deste usuário");
        }
        
        group.removeRequest(requestingUsername);
        
        if (approved) {
            group.addMember(requestingUsername);
            userGroups.computeIfAbsent(requestingUsername, k -> ConcurrentHashMap.newKeySet()).add(groupId);
            
            System.out.println("Solicitação aprovada: " + requestingUsername + " entrou em " + group.getGroupName());
            
            // Notifica o usuário aprovado
            ChatClientCallback userCallback = clients.get(requestingUsername);
            if (userCallback != null) {
                try {
                    userCallback.onGroupJoinRequestProcessed(groupId, group.getGroupName(), true);
                    userCallback.onAddedToGroup(groupId, group.getGroupName());
                } catch (RemoteException e) {
                    System.err.println("Erro ao notificar aprovação: " + e.getMessage());
                }
            }
            
            // Notifica todos os membros sobre atualização
            ChatService.GroupInfo groupInfo = convertToGroupInfo(group);
            broadcastGroupUpdate(groupId, groupInfo);
        } else {
            System.out.println("Solicitação reprovada: " + requestingUsername + " não entrou em " + group.getGroupName());
            
            // Notifica o usuário reprovado
            ChatClientCallback userCallback = clients.get(requestingUsername);
            if (userCallback != null) {
                try {
                    userCallback.onGroupJoinRequestProcessed(groupId, group.getGroupName(), false);
                } catch (RemoteException e) {
                    System.err.println("Erro ao notificar reprovação: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public void processInvite(String groupId, String username, boolean accepted) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new RemoteException("Grupo não encontrado");
        }
        
        Set<String> userInvites = pendingInvites.get(username);
        if (userInvites == null || !userInvites.contains(groupId)) {
            throw new RemoteException("Não há convite pendente para este grupo");
        }
        
        userInvites.remove(groupId);
        
        if (accepted) {
            group.addMember(username);
            userGroups.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(groupId);
            
            System.out.println("Convite aceito: " + username + " entrou em " + group.getGroupName());
            
            // Notifica o usuário que aceitou
            ChatClientCallback userCallback = clients.get(username);
            if (userCallback != null) {
                try {
                    userCallback.onAddedToGroup(groupId, group.getGroupName());
                } catch (RemoteException e) {
                    System.err.println("Erro ao notificar entrada no grupo: " + e.getMessage());
                }
            }
            
            // Notifica todos os membros sobre atualização
            ChatService.GroupInfo groupInfo = convertToGroupInfo(group);
            broadcastGroupUpdate(groupId, groupInfo);
        } else {
            System.out.println("Convite rejeitado: " + username + " não entrou em " + group.getGroupName());
        }
    }
    
    @Override
    public void sendGroupMessage(String groupId, String username, String message) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null || !group.isMember(username)) {
            throw new RemoteException("Grupo não encontrado ou usuário não é membro");
        }
        
        System.out.println("[GRUPO:" + group.getGroupName() + "] [" + username + "]: " + message);
        
        // Envia mensagem para todos os membros do grupo
        group.getMembers().forEach(member -> {
            ChatClientCallback callback = clients.get(member);
            if (callback != null) {
                try {
                    callback.onGroupMessageReceived(groupId, group.getGroupName(), username, message);
                } catch (RemoteException e) {
                    System.err.println("Erro ao enviar mensagem de grupo para " + member + ": " + e.getMessage());
                    clients.remove(member);
                }
            }
        });
    }
    
    @Override
    public String[] getPendingRequests(String groupId, String ownerUsername) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null || !group.isOwner(ownerUsername)) {
            return new String[0];
        }
        
        return group.getPendingRequests().stream()
            .map(GroupRequest::getUsername)
            .toArray(String[]::new);
    }
    
    @Override
    public ChatService.GroupInfo[] getPendingInvites(String username) throws RemoteException {
        Set<String> userInvites = pendingInvites.get(username);
        if (userInvites == null || userInvites.isEmpty()) {
            return new ChatService.GroupInfo[0];
        }
        
        return userInvites.stream()
            .map(groups::get)
            .filter(Objects::nonNull)
            .map(this::convertToGroupInfo)
            .toArray(ChatService.GroupInfo[]::new);
    }
    
    @Override
    public void leaveGroup(String groupId, String username) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null || !group.isMember(username)) {
            throw new RemoteException("Grupo não encontrado ou usuário não é membro");
        }
        
        // Dono não pode sair (ou podemos permitir que saia e o grupo seja deletado)
        if (group.isOwner(username)) {
            throw new RemoteException("Dono do grupo não pode sair. Delete o grupo se necessário.");
        }
        
        group.removeMember(username);
        userGroups.getOrDefault(username, Collections.emptySet()).remove(groupId);
        
        System.out.println("Usuário " + username + " saiu do grupo " + group.getGroupName());
        
        // Notifica todos os membros sobre atualização
        ChatService.GroupInfo groupInfo = convertToGroupInfo(group);
        broadcastGroupUpdate(groupId, groupInfo);
    }
    
    @Override
    public ChatService.GroupInfo getGroupInfo(String groupId) throws RemoteException {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new RemoteException("Grupo não encontrado");
        }
        return convertToGroupInfo(group);
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private ChatService.GroupInfo convertToGroupInfo(Group group) {
        String[] members = group.getMembers().toArray(new String[0]);
        return new ChatService.GroupInfo(group.getGroupId(), group.getGroupName(), group.getOwner(), members);
    }
    
    private void broadcastGroupCreated(ChatService.GroupInfo groupInfo) {
        clients.forEach((user, callback) -> {
            try {
                callback.onGroupCreated(groupInfo);
            } catch (RemoteException e) {
                System.err.println("Erro ao notificar criação de grupo para " + user + ": " + e.getMessage());
                clients.remove(user);
            }
        });
    }
    
    private void broadcastGroupUpdate(String groupId, ChatService.GroupInfo groupInfo) {
        Group group = groups.get(groupId);
        if (group == null) return;
        
        // Notifica apenas os membros do grupo
        group.getMembers().forEach(member -> {
            ChatClientCallback callback = clients.get(member);
            if (callback != null) {
                try {
                    callback.onGroupUpdated(group.getGroupName(), groupInfo);
                } catch (RemoteException e) {
                    System.err.println("Erro ao atualizar grupo para " + member + ": " + e.getMessage());
                    clients.remove(member);
                }
            }
        });
    }
    
    /**
     * Classe interna que estende Subject para eventos de chat
     */
    public static class ChatSubject extends Subject {
        // Herda métodos attach, detach e notifyObservers de Subject
    }
}

