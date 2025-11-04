package com.chatrmi.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstrata Subject do padrão Observer
 */
public abstract class Subject {
    private List<Observer> observers = new ArrayList<>();
    
    /**
     * Adiciona um observador
     * @param observer Observador a ser adicionado
     */
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Remove um observador
     * @param observer Observador a ser removido
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifica todos os observadores
     * @param data Dados a serem passados para os observadores
     */
    public void notifyObservers(Object data) {
        for (Observer observer : observers) {
            observer.update(data);
        }
    }
    
    /**
     * Retorna o número de observadores
     * @return Número de observadores
     */
    public int getObserverCount() {
        return observers.size();
    }
}

