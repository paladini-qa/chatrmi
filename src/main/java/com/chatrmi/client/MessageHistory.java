package com.chatrmi.client;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe para representar uma mensagem no histórico de chat
 */
public class MessageHistory implements Serializable {
    
    public enum MessageType {
        TEXT, FILE
    }
    
    private String username;
    private String content; // mensagem ou nome do arquivo
    private LocalDateTime timestamp;
    private boolean isSent;
    private MessageType type;
    
    public MessageHistory(String username, String content, LocalDateTime timestamp, boolean isSent, MessageType type) {
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.type = type;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getContent() {
        return content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public boolean isSent() {
        return isSent;
    }
    
    public MessageType getType() {
        return type;
    }
}

