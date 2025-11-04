package com.chatrmi.observer;

/**
 * Classe abstrata para observadores de chat
 */
public abstract class ChatObserver implements Observer {
    
    @Override
    public void update(Object data) {
        if (data instanceof MessageEvent) {
            onMessageEvent((MessageEvent) data);
        } else if (data instanceof UserEvent) {
            onUserEvent((UserEvent) data);
        } else if (data instanceof FileEvent) {
            onFileEvent((FileEvent) data);
        }
    }
    
    /**
     * Chamado quando há um evento de mensagem
     * @param event Evento de mensagem
     */
    protected abstract void onMessageEvent(MessageEvent event);
    
    /**
     * Chamado quando há um evento de usuário
     * @param event Evento de usuário
     */
    protected abstract void onUserEvent(UserEvent event);
    
    /**
     * Chamado quando há um evento de arquivo
     * @param event Evento de arquivo
     */
    protected abstract void onFileEvent(FileEvent event);
    
    /**
     * Classe para eventos de mensagem
     */
    public static class MessageEvent {
        private String username;
        private String message;
        
        public MessageEvent(String username, String message) {
            this.username = username;
            this.message = message;
        }
        
        public String getUsername() { return username; }
        public String getMessage() { return message; }
    }
    
    /**
     * Classe para eventos de usuário
     */
    public static class UserEvent {
        private String[] users;
        
        public UserEvent(String[] users) {
            this.users = users;
        }
        
        public String[] getUsers() { return users; }
    }
    
    /**
     * Classe para eventos de arquivo
     */
    public static class FileEvent {
        private String username;
        private String filename;
        
        public FileEvent(String username, String filename) {
            this.username = username;
            this.filename = filename;
        }
        
        public String getUsername() { return username; }
        public String getFilename() { return filename; }
    }
}

