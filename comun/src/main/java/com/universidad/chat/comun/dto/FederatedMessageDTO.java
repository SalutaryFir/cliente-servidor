package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * DTO para encapsular mensajes que viajan entre servidores
 * Añade información de origen para routing
 */
@Data
public class FederatedMessageDTO implements Serializable {
    private String originServerIP;      // IP del servidor que origina el mensaje
    private String originServerName;    // Nombre del servidor origen
    private MessageDTO message;         // El mensaje original
    private boolean requiresAudioData;  // Si es true, el audio debe solicitarse al servidor origen
}
