package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para información general del servidor expuesta vía API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfoResponseDTO implements Serializable {
    private String serverName;
    private String serverIp;
    private int clientPort;
    private int federationPort;
    private int connectedClients;
    private int maxConnections;
    private long uptimeMillis;
    private List<String> federatedServers;
    private ServerStatus status;
    
    public enum ServerStatus {
        RUNNING, DEGRADED, MAINTENANCE
    }
}
