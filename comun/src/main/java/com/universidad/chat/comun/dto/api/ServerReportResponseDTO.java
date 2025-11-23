package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DTO para reporte completo del servidor vía API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerReportResponseDTO implements Serializable {
    private String serverName;
    private String generatedAt;
    private ServerInfoResponseDTO serverInfo;
    private ServerStatsResponseDTO stats;
    private List<String> topActiveUsers;
    private List<String> topActiveChannels;
    private Map<String, Integer> messagesByHour; // Últimas 24h
    private List<String> recentErrors;
    private FederationStatus federationStatus;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FederationStatus implements Serializable {
        private int connectedServers;
        private int totalRemoteUsers;
        private List<String> serverNames;
    }
}
