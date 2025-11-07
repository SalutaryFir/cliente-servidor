package com.universidad.chat.servidor.network;

import com.universidad.chat.comun.dto.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registro de servidores federados y gestión de comunicación S2S
 */
@Component
public class ServerRegistry {

    // Mapa de IP:puerto -> FederatedServer
    private final Map<String, FederatedServer> federatedServers = new ConcurrentHashMap<>();
    
    // Caché de usuarios remotos por servidor
    private final Map<String, java.util.List<String>> remoteUsersCache = new ConcurrentHashMap<>();
    
    private String localServerIP;
    private String localServerName;
    private int localFederationPort;
    private TCPServer tcpServer; // Referencia al servidor TCP para obtener usuarios conectados

    // Clase interna para representar un servidor federado
    public static class FederatedServer {
        public ServerInfoDTO info;
        public Socket socket;
        public ObjectOutputStream outputStream;
        public ObjectInputStream inputStream;
        public long lastHeartbeat;

        public FederatedServer(ServerInfoDTO info, Socket socket) {
            this.info = info;
            this.socket = socket;
            this.lastHeartbeat = System.currentTimeMillis();
            try {
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
                this.inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error al inicializar streams para servidor federado: " + e.getMessage());
            }
        }

        public FederatedServer(ServerInfoDTO info, Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
            this.info = info;
            this.socket = socket;
            this.outputStream = oos;
            this.inputStream = ois;
            this.lastHeartbeat = System.currentTimeMillis();
        }
    }

    public void setLocalServerInfo(String ip, String name, int federationPort) {
        this.localServerIP = ip;
        this.localServerName = name;
        this.localFederationPort = federationPort;
    }
    
    public void setTCPServer(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    public String getLocalServerIP() {
        return localServerIP;
    }

    public String getLocalServerName() {
        return localServerName;
    }

    /**
     * Registra un servidor federado entrante
     */
    public void registerServer(ServerInfoDTO serverInfo, Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
        String key = serverInfo.getIpAddress() + ":" + serverInfo.getFederationPort();
        FederatedServer fs = new FederatedServer(serverInfo, socket, oos, ois);
        federatedServers.put(key, fs);
        System.out.println("✅ Servidor federado registrado: " + serverInfo.getServerName() + " (" + key + ")");
    }

    /**
     * Conecta proactivamente a otro servidor
     */
    public boolean connectToServer(String remoteIP, int remoteFederationPort) {
        String key = remoteIP + ":" + remoteFederationPort;
        if (federatedServers.containsKey(key)) {
            System.out.println("⚠️ Ya estás conectado a " + key);
            return false;
        }

        try {
            Socket socket = new Socket(remoteIP, remoteFederationPort);
            // Preparar nuestra información local
            ServerInfoDTO myInfo = new ServerInfoDTO();
            myInfo.setServerName(localServerName);
            myInfo.setIpAddress(localServerIP);
            myInfo.setFederationPort(localFederationPort);
            myInfo.setTimestamp(System.currentTimeMillis());

            // Crear entrada en el registro (simulando respuesta)
            ServerInfoDTO remoteInfo = new ServerInfoDTO();
            remoteInfo.setIpAddress(remoteIP);
            remoteInfo.setFederationPort(remoteFederationPort);
            remoteInfo.setServerName("Servidor-" + remoteIP); // Placeholder

            FederatedServer fs = new FederatedServer(remoteInfo, socket);
            federatedServers.put(key, fs);

            // Enviar nuestro SERVER_REGISTER usando el stream ya creado en FederatedServer
            Packet registerPacket = new Packet(ActionType.SERVER_REGISTER, myInfo);
            fs.outputStream.writeObject(registerPacket);
            fs.outputStream.flush();

            System.out.println("✅ Conectado a servidor federado: " + key);
            
            // Enviar inmediatamente nuestra lista de usuarios conectados
            if (tcpServer != null) {
                sendUserListToServer(key);
            }
            
            // NUEVO: Iniciar un handler para recibir paquetes de este servidor
            // (necesario para recibir su lista de usuarios, topología, etc.)
            new Thread(new OutgoingConnectionHandler(socket, this, tcpServer, key)).start();
            
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error al conectar con servidor " + key + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un servidor federado por su clave
     */
    public FederatedServer getFederatedServer(String serverKey) {
        return federatedServers.get(serverKey);
    }

    /**
     * Actualiza el timestamp de un servidor federado (para heartbeat)
     */
    public void updateServerTimestamp(String serverKey) {
        FederatedServer fs = federatedServers.get(serverKey);
        if (fs != null && fs.info != null) {
            fs.info.setTimestamp(System.currentTimeMillis());
        }
    }

    /**
     * Desregistra un servidor federado
     */
    public void unregisterServer(String serverKey) {
        FederatedServer fs = federatedServers.remove(serverKey);
        if (fs != null) {
            try {
                fs.socket.close();
            } catch (IOException e) { /* ignore */ }
            clearRemoteUsers(serverKey); // Limpiar caché de usuarios
            System.out.println("❌ Servidor federado desconectado: " + serverKey);
        }
    }

    /**
     * Envía un paquete a un servidor específico
     */
    public void sendToServer(String serverKey, Packet packet) {
        FederatedServer fs = federatedServers.get(serverKey);
        if (fs != null && fs.outputStream != null) {
            try {
                synchronized (fs.outputStream) {
                    fs.outputStream.writeObject(packet);
                    fs.outputStream.flush();
                }
            } catch (IOException e) {
                System.err.println("Error enviando a servidor federado " + serverKey + ": " + e.getMessage());
                unregisterServer(serverKey);
            }
        }
    }

    /**
     * Broadcast a todos los servidores federados
     */
    public void broadcastToFederation(Packet packet) {
        java.util.List<String> failedServers = new java.util.ArrayList<>();
        
        federatedServers.forEach((key, fs) -> {
            try {
                if (fs.outputStream != null) {
                    synchronized (fs.outputStream) {
                        fs.outputStream.writeObject(packet);
                        fs.outputStream.flush();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error en broadcast a federación (" + key + "): " + e.getMessage());
                failedServers.add(key);
            }
        });
        
        // Desregistrar servidores con problemas DESPUÉS del loop
        failedServers.forEach(this::unregisterServer);
    }

    /**
     * Encuentra el servidor donde está un usuario remoto (por ahora, broadcast)
     */
    public String findServerForUser(String username) {
        // TODO: Implementar caché de usuarios remotos
        // Por ahora retornamos null para indicar que hay que hacer broadcast
        return null;
    }

    /**
     * Retorna la lista de servidores federados (para UI)
     */
    public Set<ServerInfoDTO> getFederatedServers() {
        return federatedServers.values().stream()
                .map(fs -> fs.info)
                .collect(Collectors.toSet());
    }

    /**
     * Verifica si un servidor está registrado
     */
    public boolean isServerRegistered(String serverKey) {
        return federatedServers.containsKey(serverKey);
    }
    
    /**
     * Envía la lista actual de usuarios conectados a un servidor específico
     */
    public void sendUserListToServer(String serverKey) {
        if (tcpServer == null) {
            return;
        }
        
        ServerUserListDTO userListDTO = new ServerUserListDTO();
        userListDTO.setServerIP(localServerIP);
        userListDTO.setServerName(localServerName);
        userListDTO.setUsernames(tcpServer.getConnectedUsernames());
        
        Packet syncPacket = new Packet(ActionType.SERVER_USER_LIST_SYNC, userListDTO);
        sendToServer(serverKey, syncPacket);
        
        System.out.println("📤 Lista de usuarios enviada a " + serverKey + ": " + userListDTO.getUsernames());
    }
    
    /**
     * Actualiza el caché de usuarios remotos de un servidor federado
     */
    public void updateRemoteUsers(String serverKey, java.util.List<String> usernames) {
        remoteUsersCache.put(serverKey, new java.util.ArrayList<>(usernames));
        System.out.println("🔄 Caché actualizado para " + serverKey + ": " + usernames);
        System.out.println("📊 Total usuarios remotos en caché: " + getAllRemoteUsers());
    }
    
    /**
     * Obtiene todos los usuarios remotos de todos los servidores federados
     */
    public java.util.List<String> getAllRemoteUsers() {
        java.util.List<String> allRemoteUsers = new java.util.ArrayList<>();
        remoteUsersCache.values().forEach(allRemoteUsers::addAll);
        return allRemoteUsers;
    }
    
    /**
     * Limpia el caché de usuarios de un servidor desconectado
     */
    public void clearRemoteUsers(String serverKey) {
        remoteUsersCache.remove(serverKey);
        System.out.println("🗑️ Cache limpiado para servidor desconectado: " + serverKey);
    }

    /**
     * Devuelve usuarios remotos anotados con el nombre del servidor de origen: "usuario (Servidor-X)"
     */
    public java.util.List<String> getAllRemoteUsersAnnotated() {
        java.util.List<String> annotated = new java.util.ArrayList<>();
        remoteUsersCache.forEach((serverKey, users) -> {
            String serverDisplay = serverKey;
            FederatedServer fs = federatedServers.get(serverKey);
            if (fs != null && fs.info != null && fs.info.getServerName() != null) {
                serverDisplay = fs.info.getServerName();
            }
            final String finalServerDisplay = serverDisplay;
            users.forEach(u -> annotated.add(u + " (" + finalServerDisplay + ")"));
        });
        return annotated;
    }

    /**
     * Actualiza el nombre amigable del servidor federado (útil cuando llega por S2S)
     */
    public void updateServerName(String serverKey, String serverName) {
        FederatedServer fs = federatedServers.get(serverKey);
        if (fs != null && fs.info != null && serverName != null && !serverName.isEmpty()) {
            fs.info.setServerName(serverName);
        }
    }
    
    /**
     * Encuentra el servidor federado que tiene un usuario específico
     * @param username Nombre del usuario a buscar
     * @return La clave del servidor (IP:puerto) o null si no se encuentra
     */
    public String findServerByUsername(String username) {
        for (Map.Entry<String, java.util.List<String>> entry : remoteUsersCache.entrySet()) {
            if (entry.getValue().contains(username)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Obtiene el FederatedServer por su clave
     * @param serverKey Clave del servidor (IP:puerto)
     * @return El FederatedServer o null si no existe
     */
    public FederatedServer getFederatedServerByKey(String serverKey) {
        return federatedServers.get(serverKey);
    }

    public int getLocalFederationPort() {
        return localFederationPort;
    }
    
    /**
     * Obtiene la topología completa de servidores (info de todos los servidores conectados)
     */
    public ServerTopologyDTO getTopology() {
        ServerTopologyDTO topology = new ServerTopologyDTO();
        java.util.List<ServerInfoDTO> serverList = new java.util.ArrayList<>();
        
        // Agregar info de este servidor local
        ServerInfoDTO localInfo = new ServerInfoDTO();
        localInfo.setServerName(localServerName);
        localInfo.setIpAddress(localServerIP);
        localInfo.setFederationPort(localFederationPort);
        localInfo.setTimestamp(System.currentTimeMillis());
        serverList.add(localInfo);
        
        // Agregar info de servidores federados
        federatedServers.values().forEach(fs -> {
            if (fs.info != null) {
                serverList.add(fs.info);
            }
        });
        
        topology.setServers(serverList);
        return topology;
    }
    
    /**
     * Propaga la topología a un servidor específico
     */
    public void sendTopologyToServer(String serverKey) {
        ServerTopologyDTO topology = getTopology();
        Packet topologyPacket = new Packet(ActionType.SERVER_TOPOLOGY_SYNC, topology);
        sendToServer(serverKey, topologyPacket);
        System.out.println("📡 Topología enviada a " + serverKey + " (" + topology.getServers().size() + " servidores)");
    }
    
    /**
     * Conecta a todos los servidores de una topología recibida
     */
    public void connectToTopology(ServerTopologyDTO topology) {
        if (topology == null || topology.getServers() == null) {
            return;
        }
        
        for (ServerInfoDTO serverInfo : topology.getServers()) {
            String remoteIP = serverInfo.getIpAddress();
            int remoteFedPort = serverInfo.getFederationPort();
            String key = remoteIP + ":" + remoteFedPort;
            
            // No conectarse a sí mismo
            if (remoteIP.equals(localServerIP) && remoteFedPort == localFederationPort) {
                continue;
            }
            
            // Si ya está conectado, saltar
            if (federatedServers.containsKey(key)) {
                System.out.println("⚠️ Ya conectado a " + key + ", omitiendo...");
                continue;
            }
            
            // Conectar al nuevo servidor
            System.out.println("🔗 Conectando automáticamente a servidor de la topología: " + key);
            connectToServer(remoteIP, remoteFedPort);
        }
    }
}
