package com.chatrmi.model;

import java.io.Serializable;

/**
 * Classe que representa uma solicitação de entrada em grupo
 */
public class GroupRequest implements Serializable {
    private String username;
    private String groupId;
    private String groupName;
    private long timestamp;
    
    public GroupRequest(String username, String groupId, String groupName) {
        this.username = username;
        this.groupId = groupId;
        this.groupName = groupName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}

