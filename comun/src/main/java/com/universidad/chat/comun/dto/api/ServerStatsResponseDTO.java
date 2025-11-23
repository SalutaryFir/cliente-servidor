package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para estadísticas del servidor vía API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerStatsResponseDTO implements Serializable {
    private String serverName;
    private long totalMessagesProcessed;
    private long totalAudiosProcessed;
    private int totalChannels;
    private int totalRegisteredUsers;
    private int currentConnections;
    private double averageResponseTimeMs;
    private long uptimeMillis;
    private MemoryStats memoryStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryStats implements Serializable {
        private long totalMemoryMB;
        private long usedMemoryMB;
        private long freeMemoryMB;
        private int memoryUsagePercent;
    }
}
