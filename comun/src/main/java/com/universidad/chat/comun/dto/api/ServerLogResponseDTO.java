package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para logs del servidor vía API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerLogResponseDTO implements Serializable {
    private int totalLogs;
    private List<LogEntryDTO> logs;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogEntryDTO implements Serializable {
        private long timestamp;
        private String level; // INFO, WARN, ERROR, DEBUG
        private String message;
        private String source; // Componente que generó el log
    }
}
