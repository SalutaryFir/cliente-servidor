package com.universidad.chat.comun.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para enviar historial de mensajes al cliente
 * Puede ser historial de un chat privado o de un canal
 */
public class MessageHistoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String chatId; // Username del contacto o nombre del canal (con #)
    private List<MessageDTO> messages;
    
    public MessageHistoryDTO() {}
    
    public MessageHistoryDTO(String chatId, List<MessageDTO> messages) {
        this.chatId = chatId;
        this.messages = messages;
    }
    
    public String getChatId() {
        return chatId;
    }
    
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
    
    public List<MessageDTO> getMessages() {
        return messages;
    }
    
    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages;
    }
}
