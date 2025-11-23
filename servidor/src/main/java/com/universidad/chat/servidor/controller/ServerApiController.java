package com.universidad.chat.servidor.controller;

import com.universidad.chat.comun.dto.api.*;
import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.network.ServerRegistry;
import com.universidad.chat.servidor.network.TCPServer;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.repository.MensajeRepository;
import com.universidad.chat.servidor.repository.UsuarioRepository;
import com.universidad.chat.servidor.service.ServerLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador REST para exponer APIs de monitoreo y administración del servidor
 * Endpoints diseñados para ser consumidos por API Gateway y aplicación web
 */
@RestController
@RequestMapping("/api/v1/server")
@CrossOrigin(origins = "*") // Permitir todas las fuentes (ajustar en producción)
public class ServerApiController {

    @Autowired
    private TCPServer tcpServer;

    @Autowired
    private ServerRegistry serverRegistry;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CanalRepository canalRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private ServerLogService logService;

    @Value("${chat.server.name:Servidor-Desconocido}")
    private String serverName;

    @Value("${chat.server.client-port:5000}")
    private int clientPort;

    @Value("${chat.server.federation-port:5001}")
    private int federationPort;

    @Value("${chat.server.max-connections:10}")
    private int maxConnections;

    private final long serverStartTime = System.currentTimeMillis();

    /**
     * GET /api/v1/server/info
     * Obtiene información general del servidor
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDTO<ServerInfoResponseDTO>> getServerInfo() {
        try {
            List<String> federatedServers = serverRegistry.getAllServers().stream()
                    .map(info -> info.getServerName())
                    .collect(Collectors.toList());

            ServerInfoResponseDTO serverInfo = new ServerInfoResponseDTO(
                    serverName,
                    serverRegistry.getLocalServerIP(),
                    clientPort,
                    federationPort,
                    tcpServer.getClients().size(),
                    maxConnections,
                    System.currentTimeMillis() - serverStartTime,
                    federatedServers,
                    ServerInfoResponseDTO.ServerStatus.RUNNING
            );

            logService.info("Información del servidor solicitada", "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(serverInfo));
        } catch (Exception e) {
            logService.error("Error al obtener información del servidor: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener información del servidor"));
        }
    }

    /**
     * GET /api/v1/server/health
     * Health check endpoint para API Gateway
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("serverName", serverName);
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", System.currentTimeMillis() - serverStartTime);
        health.put("connections", tcpServer.getClients().size());

        return ResponseEntity.ok(ApiResponseDTO.success("Server is healthy", health));
    }

    /**
     * GET /api/v1/server/users
     * Obtiene lista de usuarios del servidor
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<UserListResponseDTO>> getUsers() {
        try {
            List<Usuario> allUsers = usuarioRepository.findAll();
            List<String> connectedUsernames = tcpServer.getConnectedUsernames();

            List<UserListResponseDTO.UserInfoDTO> userInfoList = allUsers.stream()
                    .map(user -> new UserListResponseDTO.UserInfoDTO(
                            user.getNombreUsuario(),
                            user.getEmail(),
                            connectedUsernames.contains(user.getNombreUsuario()),
                            LocalDateTime.now().toString() // Placeholder, implementar tracking real
                    ))
                    .collect(Collectors.toList());

            UserListResponseDTO response = new UserListResponseDTO(
                    allUsers.size(),
                    connectedUsernames.size(),
                    userInfoList
            );

            logService.info("Lista de usuarios solicitada", "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(response));
        } catch (Exception e) {
            logService.error("Error al obtener usuarios: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener lista de usuarios"));
        }
    }

    /**
     * GET /api/v1/server/users/connected
     * Obtiene solo usuarios conectados actualmente
     */
    @GetMapping("/users/connected")
    public ResponseEntity<ApiResponseDTO<List<String>>> getConnectedUsers() {
        try {
            List<String> connectedUsers = tcpServer.getConnectedUsernames();
            logService.info("Usuarios conectados solicitados: " + connectedUsers.size(), "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(connectedUsers));
        } catch (Exception e) {
            logService.error("Error al obtener usuarios conectados: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener usuarios conectados"));
        }
    }

    /**
     * GET /api/v1/server/channels
     * Obtiene lista de canales del servidor
     */
    @GetMapping("/channels")
    public ResponseEntity<ApiResponseDTO<ChannelListResponseDTO>> getChannels() {
        try {
            List<Canal> canales = canalRepository.findAll();

            List<ChannelListResponseDTO.ChannelInfoDTO> channelInfoList = canales.stream()
                    .map(canal -> new ChannelListResponseDTO.ChannelInfoDTO(
                            canal.getNombreCanal(),
                            canal.getCreador() != null ? canal.getCreador().getNombreUsuario() : "Desconocido",
                            canal.getMiembros().size(),
                            0, // Implementar conteo de mensajes si es necesario
                            canal.getFechaCreacion() != null ? canal.getFechaCreacion().toString() : "N/A",
                            false // Placeholder, implementar detección de canales federados
                    ))
                    .collect(Collectors.toList());

            ChannelListResponseDTO response = new ChannelListResponseDTO(
                    canales.size(),
                    channelInfoList
            );

            logService.info("Lista de canales solicitada", "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(response));
        } catch (Exception e) {
            logService.error("Error al obtener canales: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener lista de canales"));
        }
    }

    /**
     * GET /api/v1/server/logs
     * Obtiene logs del servidor con paginación
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponseDTO<ServerLogResponseDTO>> getLogs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String source
    ) {
        try {
            List<ServerLogResponseDTO.LogEntryDTO> logs;

            if (level != null) {
                logs = logService.getLogsByLevel(level);
            } else if (source != null) {
                logs = logService.getLogsBySource(source);
            } else {
                logs = logService.getRecentLogs(limit);
            }

            ServerLogResponseDTO response = new ServerLogResponseDTO(
                    logs.size(),
                    logs
            );

            return ResponseEntity.ok(ApiResponseDTO.success(response));
        } catch (Exception e) {
            logService.error("Error al obtener logs: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener logs"));
        }
    }

    /**
     * DELETE /api/v1/server/logs
     * Limpia todos los logs del servidor
     */
    @DeleteMapping("/logs")
    public ResponseEntity<ApiResponseDTO<String>> clearLogs() {
        try {
            logService.clearLogs();
            return ResponseEntity.ok(ApiResponseDTO.success("Logs limpiados correctamente", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al limpiar logs"));
        }
    }

    /**
     * GET /api/v1/server/stats
     * Obtiene estadísticas del servidor
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<ServerStatsResponseDTO>> getStats() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;
            int memoryPercent = (int) ((usedMemory * 100) / totalMemory);

            ServerStatsResponseDTO.MemoryStats memoryStats = new ServerStatsResponseDTO.MemoryStats(
                    totalMemory,
                    usedMemory,
                    freeMemory,
                    memoryPercent
            );

            ServerStatsResponseDTO stats = new ServerStatsResponseDTO(
                    serverName,
                    mensajeRepository.count(), // Total de mensajes procesados
                    0L, // Implementar conteo de audios si se guarda metadata
                    (int) canalRepository.count(),
                    (int) usuarioRepository.count(),
                    tcpServer.getClients().size(),
                    0.0, // Implementar métrica de tiempo de respuesta promedio
                    System.currentTimeMillis() - serverStartTime,
                    memoryStats
            );

            logService.info("Estadísticas del servidor solicitadas", "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(stats));
        } catch (Exception e) {
            logService.error("Error al obtener estadísticas: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener estadísticas"));
        }
    }

    /**
     * GET /api/v1/server/report
     * Genera un reporte completo del servidor
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponseDTO<ServerReportResponseDTO>> getReport() {
        try {
            // Obtener información del servidor
            ServerInfoResponseDTO serverInfo = getServerInfo().getBody().getData();

            // Obtener estadísticas
            ServerStatsResponseDTO stats = getStats().getBody().getData();

            // Top usuarios activos (simplificado, implementar lógica real)
            List<String> topUsers = tcpServer.getConnectedUsernames().stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // Top canales activos
            List<String> topChannels = canalRepository.findAll().stream()
                    .limit(5)
                    .map(Canal::getNombreCanal)
                    .collect(Collectors.toList());

            // Mensajes por hora (placeholder)
            Map<String, Integer> messagesByHour = new HashMap<>();
            messagesByHour.put("00-01", 0);
            messagesByHour.put("01-02", 0);
            // Implementar lógica real según necesidad

            // Errores recientes
            List<String> recentErrors = logService.getLogsByLevel("ERROR").stream()
                    .limit(10)
                    .map(ServerLogResponseDTO.LogEntryDTO::getMessage)
                    .collect(Collectors.toList());

            // Estado de federación
            ServerReportResponseDTO.FederationStatus federationStatus = new ServerReportResponseDTO.FederationStatus(
                    serverRegistry.getAllServers().size(),
                    serverRegistry.getAllRemoteUsers().size(),
                    serverRegistry.getAllServers().stream()
                            .map(info -> info.getServerName())
                            .collect(Collectors.toList())
            );

            ServerReportResponseDTO report = new ServerReportResponseDTO(
                    serverName,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    serverInfo,
                    stats,
                    topUsers,
                    topChannels,
                    messagesByHour,
                    recentErrors,
                    federationStatus
            );

            logService.info("Reporte completo del servidor generado", "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(report));
        } catch (Exception e) {
            logService.error("Error al generar reporte: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al generar reporte"));
        }
    }

    /**
     * GET /api/v1/server/federation
     * Obtiene información sobre servidores federados
     */
    @GetMapping("/federation")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getFederationInfo() {
        try {
            Map<String, Object> federationInfo = new HashMap<>();
            federationInfo.put("connectedServers", serverRegistry.getAllServers().size());
            federationInfo.put("servers", serverRegistry.getAllServers());
            federationInfo.put("remoteUsers", serverRegistry.getAllRemoteUsers());

            logService.info("Información de federación solicitada", "ServerApiController");
            return ResponseEntity.ok(ApiResponseDTO.success(federationInfo));
        } catch (Exception e) {
            logService.error("Error al obtener información de federación: " + e.getMessage(), "ServerApiController");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Error al obtener información de federación"));
        }
    }
}
