# 🚀 API REST del Servidor de Chat Federado

## Descripción General
Cada servidor de chat expone una API REST completa para monitoreo, administración y obtención de métricas. Esta API está diseñada para ser consumida por un API Gateway y una aplicación web administrativa.

## Configuración de los Servidores

### Servidor 1
- **Puerto HTTP (API):** 8080
- **Puerto TCP (Chat):** 9999
- **Puerto Federación:** 5001
- **Base URL:** `http://localhost:8080/api/v1/server`

### Servidor 2
- **Puerto HTTP (API):** 8081
- **Puerto TCP (Chat):** 9999
- **Puerto Federación:** 5002
- **Base URL:** `http://localhost:8081/api/v1/server`

### Servidor 3
- **Puerto HTTP (API):** 8082
- **Puerto TCP (Chat):** 9999
- **Puerto Federación:** 5003
- **Base URL:** `http://localhost:8082/api/v1/server`

### Servidor 4
- **Puerto HTTP (API):** 8083
- **Puerto TCP (Chat):** 9999
- **Puerto Federación:** 5004
- **Base URL:** `http://localhost:8083/api/v1/server`

---

## Endpoints Disponibles

### 📊 Información del Servidor

#### `GET /api/v1/server/info`
Obtiene información general del servidor.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "serverName": "Servidor-1",
    "serverIp": "192.168.1.100",
    "clientPort": 9999,
    "federationPort": 5001,
    "connectedClients": 5,
    "maxConnections": 10,
    "uptimeMillis": 3600000,
    "federatedServers": ["Servidor-2", "Servidor-3"],
    "status": "RUNNING"
  },
  "timestamp": 1700000000000
}
```

---

#### `GET /api/v1/server/health`
Health check endpoint para balanceadores de carga y API Gateway.

**Respuesta:**
```json
{
  "success": true,
  "message": "Server is healthy",
  "data": {
    "status": "UP",
    "serverName": "Servidor-1",
    "timestamp": 1700000000000,
    "uptime": 3600000,
    "connections": 5
  },
  "timestamp": 1700000000000
}
```

---

### 👥 Usuarios

#### `GET /api/v1/server/users`
Obtiene lista completa de usuarios registrados en el servidor.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalUsers": 15,
    "connectedUsers": 5,
    "users": [
      {
        "username": "alice",
        "email": "alice@example.com",
        "online": true,
        "lastConnection": "2025-11-23T10:30:00"
      }
    ]
  },
  "timestamp": 1700000000000
}
```

---

#### `GET /api/v1/server/users/connected`
Obtiene solo usuarios conectados actualmente.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": ["alice", "bob", "charlie"],
  "timestamp": 1700000000000
}
```

---

### 📢 Canales

#### `GET /api/v1/server/channels`
Obtiene lista de todos los canales del servidor.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalChannels": 8,
    "channels": [
      {
        "channelName": "#general",
        "creator": "alice",
        "memberCount": 5,
        "messageCount": 0,
        "createdAt": "2025-11-20T08:00:00",
        "federated": false
      }
    ]
  },
  "timestamp": 1700000000000
}
```

---

### 📝 Logs

#### `GET /api/v1/server/logs`
Obtiene logs del servidor con opciones de filtrado.

**Parámetros de Query:**
- `limit` (default: 100): Número máximo de logs a retornar
- `level` (opcional): Filtrar por nivel (INFO, WARN, ERROR, DEBUG)
- `source` (opcional): Filtrar por fuente/componente

**Ejemplos:**
```
GET /api/v1/server/logs?limit=50
GET /api/v1/server/logs?level=ERROR
GET /api/v1/server/logs?source=TCPServer
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalLogs": 50,
    "logs": [
      {
        "timestamp": 1700000000000,
        "level": "INFO",
        "message": "Nuevo cliente conectado",
        "source": "TCPServer"
      }
    ]
  },
  "timestamp": 1700000000000
}
```

---

#### `DELETE /api/v1/server/logs`
Limpia todos los logs del servidor.

**Respuesta:**
```json
{
  "success": true,
  "message": "Logs limpiados correctamente",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 📈 Estadísticas

#### `GET /api/v1/server/stats`
Obtiene estadísticas detalladas del servidor.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "serverName": "Servidor-1",
    "totalMessagesProcessed": 1500,
    "totalAudiosProcessed": 25,
    "totalChannels": 8,
    "totalRegisteredUsers": 15,
    "currentConnections": 5,
    "averageResponseTimeMs": 0.0,
    "uptimeMillis": 3600000,
    "memoryStats": {
      "totalMemoryMB": 512,
      "usedMemoryMB": 256,
      "freeMemoryMB": 256,
      "memoryUsagePercent": 50
    }
  },
  "timestamp": 1700000000000
}
```

---

### 📄 Reporte Completo

#### `GET /api/v1/server/report`
Genera un reporte completo y detallado del servidor.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "serverName": "Servidor-1",
    "generatedAt": "2025-11-23T12:00:00",
    "serverInfo": { /* ... */ },
    "stats": { /* ... */ },
    "topActiveUsers": ["alice", "bob", "charlie"],
    "topActiveChannels": ["#general", "#random"],
    "messagesByHour": {
      "00-01": 0,
      "01-02": 0
    },
    "recentErrors": [],
    "federationStatus": {
      "connectedServers": 2,
      "totalRemoteUsers": 10,
      "serverNames": ["Servidor-2", "Servidor-3"]
    }
  },
  "timestamp": 1700000000000
}
```

---

### 🌐 Federación

#### `GET /api/v1/server/federation`
Obtiene información sobre servidores federados conectados.

**Respuesta:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "connectedServers": 2,
    "servers": [
      {
        "serverName": "Servidor-2",
        "ipAddress": "192.168.1.101",
        "federationPort": 5002,
        "timestamp": 1700000000000
      }
    ],
    "remoteUsers": ["diana", "eve", "frank"]
  },
  "timestamp": 1700000000000
}
```

---

## Documentación Interactiva con Swagger

Cada servidor expone documentación interactiva de la API usando Swagger UI:

- **Servidor 1:** http://localhost:8080/swagger-ui.html
- **Servidor 2:** http://localhost:8081/swagger-ui.html
- **Servidor 3:** http://localhost:8082/swagger-ui.html
- **Servidor 4:** http://localhost:8083/swagger-ui.html

Desde Swagger UI puedes:
- Ver todos los endpoints disponibles
- Probar los endpoints directamente desde el navegador
- Ver esquemas de request/response
- Descargar especificación OpenAPI en JSON

**OpenAPI Spec (JSON):**
- http://localhost:8080/v3/api-docs

---

## Integración con API Gateway

### Ejemplo de Configuración para Kong API Gateway

```yaml
services:
  - name: chat-server-1
    url: http://localhost:8080
    routes:
      - name: server-1-api
        paths:
          - /server1
        strip_path: true
        
  - name: chat-server-2
    url: http://localhost:8081
    routes:
      - name: server-2-api
        paths:
          - /server2
        strip_path: true
        
  - name: chat-server-3
    url: http://localhost:8082
    routes:
      - name: server-3-api
        paths:
          - /server3
        strip_path: true
        
  - name: chat-server-4
    url: http://localhost:8083
    routes:
      - name: server-4-api
        paths:
          - /server4
        strip_path: true
```

### Ejemplo de Configuración para Spring Cloud Gateway

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: chat-server-1
          uri: http://localhost:8080
          predicates:
            - Path=/server1/**
          filters:
            - StripPrefix=1
            
        - id: chat-server-2
          uri: http://localhost:8081
          predicates:
            - Path=/server2/**
          filters:
            - StripPrefix=1
            
        - id: chat-server-3
          uri: http://localhost:8082
          predicates:
            - Path=/server3/**
          filters:
            - StripPrefix=1
            
        - id: chat-server-4
          uri: http://localhost:8083
          predicates:
            - Path=/server4/**
          filters:
            - StripPrefix=1
```

---

## CORS y Seguridad

### CORS
La API está configurada para aceptar llamadas desde cualquier origen (`*`) en desarrollo. Para producción, se recomienda:

1. Editar `CorsConfig.java`
2. Reemplazar `config.addAllowedOriginPattern("*")` con orígenes específicos:
```java
config.addAllowedOrigin("https://tu-webapp.com");
config.addAllowedOrigin("https://tu-api-gateway.com");
```

### Autenticación (Futura Implementación)
Se recomienda implementar:
- **JWT Tokens** para autenticación de aplicaciones cliente
- **API Keys** para servicios externos
- **Rate Limiting** en el API Gateway

---

## Testing de la API

### Con cURL:

```bash
# Health Check
curl http://localhost:8080/api/v1/server/health

# Información del servidor
curl http://localhost:8080/api/v1/server/info

# Usuarios conectados
curl http://localhost:8080/api/v1/server/users/connected

# Logs con filtro
curl "http://localhost:8080/api/v1/server/logs?level=ERROR&limit=10"

# Reporte completo
curl http://localhost:8080/api/v1/server/report
```

### Con Postman/Insomnia:
Importa la colección desde Swagger: `http://localhost:8080/v3/api-docs`

---

## Iniciar Servidores

```bash
# Servidor 1 (default)
cd servidor
mvn spring-boot:run

# Servidor 2
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=server2

# Servidor 3
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=server3

# Servidor 4
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=server4
```

---

## Monitoreo desde Aplicación Web Dockerizada

Tu aplicación web puede consultar todos los servidores a través del API Gateway:

```javascript
// Ejemplo en JavaScript
async function getAllServersHealth() {
  const servers = ['server1', 'server2', 'server3', 'server4'];
  const results = await Promise.all(
    servers.map(async (server) => {
      const response = await fetch(`http://api-gateway:8080/${server}/api/v1/server/health`);
      return response.json();
    })
  );
  return results;
}
```

---

## Próximos Pasos

1. ✅ Compilar el proyecto: `mvn clean install`
2. ✅ Iniciar los 4 servidores
3. ✅ Verificar Swagger UI en cada servidor
4. ✅ Configurar API Gateway (Kong, Nginx, Spring Cloud Gateway, etc.)
5. ✅ Conectar aplicación web dockerizada al API Gateway
6. ✅ Implementar autenticación y seguridad en producción

---

## Soporte

Para más información, consultar:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs
- Código fuente: `servidor/src/main/java/com/universidad/chat/servidor/controller/ServerApiController.java`
