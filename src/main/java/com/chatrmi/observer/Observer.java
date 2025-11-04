package com.chatrmi.observer;

/**
 * Interface Observer do padrão Observer
 */
public interface Observer {
    /**
     * Método chamado quando o assunto é atualizado
     * @param data Dados da atualização
     */
    void update(Object data);
}

