# 🏗️ ARQUITECTURA: API GATEWAY Y COMUNICACIÓN WEB

## 📚 Tabla de Contenidos
1. [Arquitectura General](#arquitectura-general)
2. [API Gateway (Kong)](#api-gateway-kong)
3. [Servidores REST](#servidores-rest)
4. [Aplicativo Web](#aplicativo-web)
5. [Flujo de Comunicación](#flujo-de-comunicación)
6. [Endpoints Disponibles](#endpoints-disponibles)

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                     NAVEGADOR (Web Admin)                       │
│                   http://localhost:5173                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ Peticiones HTTP/REST
                         │ (axios con rutas Kong)
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              API GATEWAY KONG (Puerto 8000)                     │
│                 http://localhost:8000                           │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ /server1 ──→ │  │ /server2 ──→ │  │ /server3 ──→ │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└────────────────────────────────────────────────────────────────┘
         │                  │                  │
         │ Strip path       │ Strip path       │ Strip path
         │ HTTP forward     │ HTTP forward     │ HTTP forward
         ▼                  ▼                  ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │ SERVIDOR 1  │  │ SERVIDOR 2  │  │ SERVIDOR 3  │
    │ :8080       │  │ :8081       │  │ :8082       │
    │             │  │             │  │             │
    │ REST APIs   │  │ REST APIs   │  │ REST APIs   │
    └─────────────┘  └─────────────┘  └─────────────┘
         ▲                  ▲                  ▲
         │                  │                  │
         │ Peticiones       │ Peticiones       │ Peticiones
         │ REST             │ REST             │ REST
         │                  │                  │
         └──────────────────┴──────────────────┘
                    Web Admin (axios)
```

---

## 🚪 API Gateway (Kong)

### ¿Qué es Kong?

Kong es un **API Gateway** que actúa como un intermediario entre los clientes (Web Admin) y los servidores backend. Sus funciones principales:

1. **Enrutamiento (Routing):** Dirige las peticiones a los servidores correctos
2. **Balanceo de carga:** Distribuye tráfico entre múltiples backends
3. **Plugins:** Autenticación, rate limiting, logging, etc.
4. **Centralización:** Un único punto de entrada para todas las APIs

### Configuración de Kong

#### 1. **Docker Compose (Orquestación)**

Archivo: `docker-compose-api-gateway.yml`

```yaml
services:
  kong-database:
    image: postgres:13
    # Almacena la configuración de Kong
    # - Servicios
    - Rutas
    - Plugins
    - Credenciales

  kong:
    image: kong:3.4
    ports:
      - "8000:8000"   # Puerto de proxy (recibe peticiones)
      - "8001:8001"   # Puerto de administración (API Admin)
    environment:
      KONG_DATABASE: postgres
      KONG_PROXY_ACCESS_LOG: /dev/stdout
      KONG_ADMIN_LISTEN: 0.0.0.0:8001
```

#### 2. **Rutas Configuradas**

Archivo: `ConfigurarRutasKong.bat`

```batch
# Script que configura las rutas en Kong usando su API Admin

# Crear servicio server1
curl -X POST http://localhost:8001/services/
  --data "name=server1"
  --data "url=http://host.docker.internal:8080"

# Crear ruta para server1
curl -X POST http://localhost:8001/services/server1/routes
  --data "paths[]=/server1"
  --data "strip_path=true"
```

**Concepto Clave:**

| Término | Significa |
|---------|-----------|
| **Service** | Backend real (ej: http://localhost:8080) |
| **Route** | Punto de entrada en Kong (ej: /server1) |
| **strip_path** | Eliminar /server1 de la URL al reenviar |

**Ejemplo de Flujo:**

```
Cliente solicita:  GET http://localhost:8000/server1/api/v1/server/info
                                              ↓ Kong quita "/server1"
Kong reenvía a:    GET http://localhost:8080/api/v1/server/info
                                              ↓
Servidor responde
```

#### 3. **Mapeo de Rutas**

```
Kong (8000)                    Servidor Backend
─────────────────────────────────────────────────
/server1 ──────────────────→ localhost:8080
/server2 ──────────────────→ localhost:8081
/server3 ──────────────────→ localhost:8082
/server4 ──────────────────→ localhost:8083
```

---

## 🔌 Servidores REST

### Estructura de Respuestas

Cada servidor expone endpoints REST bajo `/api/v1/server`:

#### Clases Involucradas

```
src/main/java/com/universidad/chat/servidor/
├── controller/
│   └── ServerApiController.java          ← Controlador REST (define endpoints)
├── model/
│   ├── Usuario.java
│   ├── Canal.java
│   └── Mensaje.java
├── repository/
│   ├── UsuarioRepository.java           ← Acceso a BD
│   ├── CanalRepository.java
│   └── MensajeRepository.java
└── service/
    ├── ServerLogService.java             ← Logging
    └── TranscriptionService.java
```

### Endpoints Principales

#### **Información del Servidor**

```
GET /api/v1/server/info
GET /api/v1/server/health
GET /api/v1/server/stats
GET /api/v1/server/report
```

**Controlador:** `ServerApiController.java`

```java
@RestController
@RequestMapping("/api/v1/server")
@CrossOrigin(origins = "*")
public class ServerApiController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CanalRepository canalRepository;
    
    @Autowired
    private MensajeRepository mensajeRepository;
    
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDTO<ServerInfoDTO>> getInfo() {
        // Retorna información del servidor
        return ResponseEntity.ok(ApiResponseDTO.success(info));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<ServerStatsResponseDTO>> getStats() {
        // Retorna estadísticas (usuarios, canales, mensajes)
        return ResponseEntity.ok(ApiResponseDTO.success(stats));
    }
}
```

#### **Usuarios Conectados**

```
GET /api/v1/server/users          (Todos los usuarios)
GET /api/v1/server/users/connected (Solo conectados)
```

Flujo:
1. Controlador consulta `UsuarioRepository`
2. Filtra usuarios con estado "CONECTADO"
3. Retorna lista con DTOs

#### **Canales**

```
GET /api/v1/server/channels
```

#### **Logs en Tiempo Real**

```
GET /api/v1/server/logs           (Obtener logs históricos)
DELETE /api/v1/server/logs        (Limpiar logs)
```

#### **Audios Procesados**

```
GET /api/v1/server/audios
```

Retorna lista de mensajes de audio con transcripción:

```json
{
  "totalAudios": 5,
  "audios": [
    {
      "id": 1,
      "sender": "usuario1",
      "recipient": "usuario2",
      "audioFileName": "uuid-123.wav",
      "transcription": "Hola, esto es una prueba",
      "timestamp": "2025-12-02T10:30:00",
      "isChannelMessage": false
    }
  ]
}
```

#### **Federación**

```
GET /api/v1/server/federation     (Ver servidores conectados)
POST /api/v1/server/federation/connect  (Conectarse a otro servidor)
```

---

## 🌐 Aplicativo Web (React + Vite)

### Estructura

```
web-admin/
├── src/
│   ├── services/
│   │   └── api.js              ← ⭐ Cliente HTTP (Axios)
│   ├── components/
│   │   ├── Dashboard.jsx       ← Componente principal
│   │   ├── ServerInfo.jsx
│   │   ├── UsersSection.jsx
│   │   ├── ChannelsSection.jsx
│   │   ├── LogsSection.jsx
│   │   └── AudiosSection.jsx
│   └── App.jsx                 ← Punto de entrada
└── vite.config.js
```

### Cliente HTTP (api.js)

**Archivo:** `web-admin/src/services/api.js`

```javascript
// Configuración base
const API_GATEWAY_URL = 'http://localhost:8000';

const apiClient = axios.create({
  baseURL: API_GATEWAY_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Definición de servidores
export const SERVERS = [
  { id: 'server1', name: 'Servidor 1', route: '/server1' },
  { id: 'server2', name: 'Servidor 2', route: '/server2' },
  { id: 'server3', name: 'Servidor 3', route: '/server3' },
  { id: 'server4', name: 'Servidor 4', route: '/server4' }
];

// Clase de servicio
class ServerApiService {
  constructor(serverRoute) {
    this.baseRoute = serverRoute;  // Ej: '/server1'
  }

  async getInfo() {
    // Construye: http://localhost:8000/server1/api/v1/server/info
    const response = await apiClient.get(
      `${this.baseRoute}/api/v1/server/info`
    );
    return response.data;
  }

  async getStats() {
    const response = await apiClient.get(
      `${this.baseRoute}/api/v1/server/stats`
    );
    return response.data;
  }

  async getUsers() {
    const response = await apiClient.get(
      `${this.baseRoute}/api/v1/server/users`
    );
    return response.data;
  }

  async getAudios(limit = 50) {
    const response = await apiClient.get(
      `${this.baseRoute}/api/v1/server/audios?limit=${limit}`
    );
    return response.data;
  }
}

// Crear instancias para cada servidor
export const serverServices = SERVERS.reduce((acc, server) => {
  acc[server.id] = new ServerApiService(server.route);
  return acc;
}, {});
```

### Uso en Componentes (React)

**Archivo:** `web-admin/src/components/Dashboard.jsx`

```jsx
import { useEffect, useState } from 'react';
import { serverServices } from '../services/api';

function Dashboard() {
  const [serverInfo, setServerInfo] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Obtener información de Servidor 1
        const info = await serverServices.server1.getInfo();
        setServerInfo(info);
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return (
    <div>
      {loading ? (
        <p>Cargando...</p>
      ) : (
        <div>
          <h1>{serverInfo.serverName}</h1>
          <p>Usuarios: {serverInfo.totalUsers}</p>
        </div>
      )}
    </div>
  );
}

export default Dashboard;
```

**Sección de Audios:**

```jsx
import { serverServices } from '../services/api';

function AudiosSection({ serverId }) {
  const [audios, setAudios] = useState([]);
  const [autoRefresh, setAutoRefresh] = useState(true);

  useEffect(() => {
    if (!autoRefresh) return;

    const interval = setInterval(async () => {
      const response = await serverServices[serverId].getAudios(50);
      setAudios(response.data.audios);
    }, 5000); // Actualizar cada 5 segundos

    return () => clearInterval(interval);
  }, [autoRefresh, serverId]);

  return (
    <div>
      <button onClick={() => setAutoRefresh(!autoRefresh)}>
        {autoRefresh ? 'Pausar' : 'Reanudar'}
      </button>
      {audios.map(audio => (
        <div key={audio.id}>
          <strong>{audio.sender}</strong> → {audio.recipient}
          <p>Transcripción: {audio.transcription}</p>
        </div>
      ))}
    </div>
  );
}

export default AudiosSection;
```

---

## 📊 Flujo de Comunicación

### Caso 1: Obtener Usuarios del Servidor 1

```
┌──────────────────────────────────────────────────────────────┐
│ 1. Web Admin hace clic en "Usuarios"                         │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. Dashboard.jsx llama:                                      │
│    serverServices.server1.getUsers()                         │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│ 3. api.js construye URL:                                     │
│    GET http://localhost:8000/server1/api/v1/server/users     │
│                                                              │
│    Axios envía petición HTTP                                 │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼ (viaja por la red)
┌──────────────────────────────────────────────────────────────┐
│ 4. KONG (Gateway) recibe:                                    │
│    GET http://localhost:8000/server1/api/v1/server/users     │
│                                                              │
│    Kong identifica la ruta /server1                          │
│    Kong quita "/server1" de la URL                           │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. Kong reenvía a Backend:                                   │
│    GET http://localhost:8080/api/v1/server/users             │
│                                                              │
│    (Porque /server1 está mapeada a localhost:8080)          │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│ 6. SERVIDOR 1 procesa en ServerApiController:                │
│                                                              │
│    @GetMapping("/users")                                     │
│    public ResponseEntity getUsers() {                        │
│      List<UsuarioDTO> usuarios =                             │
│        usuarioRepository.findAll();                          │
│      return ResponseEntity.ok(usuarios);                     │
│    }                                                         │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│ 7. Servidor retorna JSON:                                    │
│                                                              │
│    {                                                         │
│      "success": true,                                        │
│      "data": [                                               │
│        { "id": 1, "username": "user1", "connected": true },  │
│        { "id": 2, "username": "user2", "connected": false }  │
│      ]                                                       │
│    }                                                         │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼ (viaja de vuelta por Kong)
┌──────────────────────────────────────────────────────────────┐
│ 8. Kong reenvía la respuesta al cliente                       │
└──────────────┬───────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│ 9. Web Admin recibe respuesta:                               │
│    - Axios parsea JSON                                       │
│    - React actualiza estado (setUsers)                       │
│    - Componente re-renderiza                                 │
│    - Usuario ve lista de usuarios en la pantalla             │
└──────────────────────────────────────────────────────────────┘
```

### Caso 2: Auto-refresh de Logs

```javascript
// LogsSection.jsx

useEffect(() => {
  if (!autoRefresh) return;

  // Configura un intervalo que se ejecuta cada 2 segundos
  const interval = setInterval(async () => {
    // Petición 1: GET /server1/api/v1/server/logs
    const response = await serverServices.server1.getLogs({ limit: 100 });
    
    // Actualiza estado local
    setLocalLogs(response.data.logs);
  }, 2000);

  return () => clearInterval(interval);
}, [autoRefresh]);

// Si autoRefresh = true:
//   - Primera petición: t=0ms
//   - Segunda petición: t=2000ms
//   - Tercera petición: t=4000ms
//   - ...
```

---

## 🔄 Flujo Completo: Multi-Servidor

```
Web Admin (Vite)
├─ Pestaña Server 1
│  └─ Llama a serverServices.server1.getInfo()
│     └─ GET http://localhost:8000/server1/api/v1/server/info
│        └─ Kong → localhost:8080/api/v1/server/info
│           └─ Spring Boot Controller → BD MySQL (chat_db_server1)
│
├─ Pestaña Server 2
│  └─ Llama a serverServices.server2.getInfo()
│     └─ GET http://localhost:8000/server2/api/v1/server/info
│        └─ Kong → localhost:8081/api/v1/server/info
│           └─ Spring Boot Controller → BD MySQL (chat_db_server2)
│
├─ Pestaña Server 3
│  └─ Llama a serverServices.server3.getInfo()
│     └─ GET http://localhost:8000/server3/api/v1/server/info
│        └─ Kong → localhost:8082/api/v1/server/info
│           └─ Spring Boot Controller → BD MySQL (chat_db_server3)
│
└─ Pestaña Server 4
   └─ Llama a serverServices.server4.getInfo()
      └─ GET http://localhost:8000/server4/api/v1/server/info
         └─ Kong → localhost:8083/api/v1/server/info
            └─ Spring Boot Controller → BD MySQL (chat_db_server4)
```

---

## 📝 Resumen de Archivos Clave

| Archivo | Ubicación | Función |
|---------|-----------|---------|
| **api.js** | web-admin/src/services/ | Cliente Axios, configuración de rutas |
| **ServerApiController.java** | servidor/src/main/java/.../controller/ | Define todos los endpoints REST |
| **Dashboard.jsx** | web-admin/src/components/ | Interfaz principal, integra secciones |
| **docker-compose-api-gateway.yml** | Raíz | Configuración de Kong con Docker |
| **ConfigurarRutasKong.bat** | Raíz | Script que configura rutas en Kong |

---

## 🚀 Diagrama de Despliegue

```
MÁQUINA 1 (PC1)
├── Kong (Puerto 8000-8001)
└── Servidor 1 (Puerto 8080)

MÁQUINA 2 (PC2)
├── Servidor 2 (Puerto 8081)

MÁQUINA 3 (PC3)
├── Servidor 3 (Puerto 8082)

MÁQUINA 4 (PC4)
├── Servidor 4 (Puerto 8083)

MÁQUINA 1 (PC1)
└── Web Admin (Puerto 5173)
    └─ Accede a Kong (8000)
       └─ Kong enruta a Servidores 1-4
```

---

## ✅ Ventajas de esta Arquitectura

✅ **Punto de entrada único:** Web Admin no necesita conocer IPs de todos los servidores  
✅ **Escalabilidad:** Agregar nuevos servidores es solo configurar una nueva ruta en Kong  
✅ **Mantenibilidad:** Cambios en backend no afectan el frontend  
✅ **Monitoreo centralizado:** Kong registra todas las peticiones  
✅ **Balanceo de carga:** Kong puede distribuir tráfico automáticamente  

---

¡Con esta arquitectura, el sistema es robusto, escalable y fácil de mantener! 🎯
