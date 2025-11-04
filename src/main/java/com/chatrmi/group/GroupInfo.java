package com.chatrmi.group;

import java.io.Serializable;

/**
 * Informações de um grupo para transmissão via RMI
 */
public class GroupInfo implements Serializable {
    private String groupName;
    private String owner;
    private String[] members;
    private int memberCount;
    
    public GroupInfo(String groupName, String owner, String[] members) {
        this.groupName = groupName;
        this.owner = owner;
        this.members = members;
        this.memberCount = members.length;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public String[] getMembers() {
        return members;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
}

