package com.chatrmi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um grupo de chat
 */
public class Group implements Serializable {
    private String groupId;
    private String groupName;
    private String owner;
    private List<String> members;
    private List<GroupRequest> pendingRequests;
    
    public Group(String groupId, String groupName, String owner) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.members.add(owner); // Dono é automaticamente membro
        this.pendingRequests = new ArrayList<>();
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public List<String> getMembers() {
        return new ArrayList<>(members);
    }
    
    public List<GroupRequest> getPendingRequests() {
        return new ArrayList<>(pendingRequests);
    }
    
    public boolean isOwner(String username) {
        return owner.equals(username);
    }
    
    public boolean isMember(String username) {
        return members.contains(username);
    }
    
    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }
    
    public void removeMember(String username) {
        members.remove(username);
    }
    
    public void addRequest(GroupRequest request) {
        // Verifica se já existe uma solicitação deste usuário
        pendingRequests.removeIf(r -> r.getUsername().equals(request.getUsername()));
        pendingRequests.add(request);
    }
    
    public void removeRequest(String username) {
        pendingRequests.removeIf(r -> r.getUsername().equals(username));
    }
    
    public GroupRequest getRequest(String username) {
        return pendingRequests.stream()
            .filter(r -> r.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
}

