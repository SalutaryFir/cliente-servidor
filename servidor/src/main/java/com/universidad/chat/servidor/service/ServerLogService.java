package com.universidad.chat.servidor.service;

import com.universidad.chat.comun.dto.api.ServerLogResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Servicio para gestión centralizada de logs del servidor
 * Almacena logs en memoria para consulta vía API REST
 */
@Service
public class ServerLogService {
    
    private static final int MAX_LOGS = 5000; // Límite de logs en memoria
    private final ConcurrentLinkedQueue<ServerLogResponseDTO.LogEntryDTO> logs = new ConcurrentLinkedQueue<>();
    
    /**
     * Registra un nuevo log
     */
    public void log(String level, String message, String source) {
        ServerLogResponseDTO.LogEntryDTO logEntry = new ServerLogResponseDTO.LogEntryDTO(
            System.currentTimeMillis(),
            level,
            message,
            source
        );
        
        logs.add(logEntry);
        
        // Limitar tamaño de logs en memoria
        while (logs.size() > MAX_LOGS) {
            logs.poll(); // Eliminar el más antiguo
        }
        
        // Opcionalmente imprimir en consola también
        System.out.println(String.format("[%s] [%s] %s", level, source, message));
    }
    
    /**
     * Log de nivel INFO
     */
    public void info(String message, String source) {
        log("INFO", message, source);
    }
    
    /**
     * Log de nivel WARN
     */
    public void warn(String message, String source) {
        log("WARN", message, source);
    }
    
    /**
     * Log de nivel ERROR
     */
    public void error(String message, String source) {
        log("ERROR", message, source);
    }
    
    /**
     * Log de nivel DEBUG
     */
    public void debug(String message, String source) {
        log("DEBUG", message, source);
    }
    
    /**
     * Obtiene todos los logs
     */
    public List<ServerLogResponseDTO.LogEntryDTO> getAllLogs() {
        return new ArrayList<>(logs);
    }
    
    /**
     * Obtiene los últimos N logs
     */
    public List<ServerLogResponseDTO.LogEntryDTO> getRecentLogs(int limit) {
        List<ServerLogResponseDTO.LogEntryDTO> allLogs = new ArrayList<>(logs);
        Collections.reverse(allLogs); // Más recientes primero
        return allLogs.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Filtra logs por nivel
     */
    public List<ServerLogResponseDTO.LogEntryDTO> getLogsByLevel(String level) {
        return logs.stream()
            .filter(log -> log.getLevel().equalsIgnoreCase(level))
            .collect(Collectors.toList());
    }
    
    /**
     * Filtra logs por fuente (componente)
     */
    public List<ServerLogResponseDTO.LogEntryDTO> getLogsBySource(String source) {
        return logs.stream()
            .filter(log -> log.getSource().equalsIgnoreCase(source))
            .collect(Collectors.toList());
    }
    
    /**
     * Limpia todos los logs
     */
    public void clearLogs() {
        logs.clear();
        info("Logs limpiados", "ServerLogService");
    }
}
