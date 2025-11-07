package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * DTO para información de un servidor en la federación
 */
@Data
public class ServerInfoDTO implements Serializable {
    private String serverName;      // Nombre identificador del servidor
    private String ipAddress;       // IP del servidor
    private int clientPort;         // Puerto para clientes (ej: 5000)
    private int federationPort;     // Puerto para S2S (ej: 5001)
    private int connectedClients;   // Número de clientes conectados
    private long timestamp;         // Para heartbeat
}
