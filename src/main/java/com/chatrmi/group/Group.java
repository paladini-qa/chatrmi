package com.chatrmi.group;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa um grupo de chat
 */
public class Group implements Serializable {
    private String groupName;
    private String owner;
    private Set<String> members;
    private Set<String> pendingRequests; // Usuários que solicitaram entrada
    
    public Group(String groupName, String owner) {
        this.groupName = groupName;
        this.owner = owner;
        this.members = new HashSet<>();
        this.members.add(owner); // Dono é automaticamente membro
        this.pendingRequests = new HashSet<>();
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public Set<String> getMembers() {
        return new HashSet<>(members);
    }
    
    public boolean addMember(String username) {
        return members.add(username);
    }
    
    public boolean removeMember(String username) {
        return members.remove(username);
    }
    
    public boolean isMember(String username) {
        return members.contains(username);
    }
    
    public boolean isOwner(String username) {
        return owner.equals(username);
    }
    
    public void addPendingRequest(String username) {
        pendingRequests.add(username);
    }
    
    public void removePendingRequest(String username) {
        pendingRequests.remove(username);
    }
    
    public Set<String> getPendingRequests() {
        return new HashSet<>(pendingRequests);
    }
    
    public boolean hasPendingRequest(String username) {
        return pendingRequests.contains(username);
    }
}

