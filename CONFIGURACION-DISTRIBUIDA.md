# Configuración de Arquitectura Distribuida en 4 PCs

## Resumen de la Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  PC 1: Web Admin                                                │
│  ├─ Web Admin (React + Vite) - Puerto 5173 / 8888             │
│  │  (Dashboard para monitorear todos los servidores)           │
│  │                                                              │
│  └─ Conecta a los servidores en otros PCs vía HTTP/REST       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  PC 2: Servidor + Cliente                                       │
│  ├─ Servidor (Java) - Puerto 8081 (API REST)                 │
│  │                  - Puerto 5002 (Federación)                 │
│  │                  - Puerto 9999 (Clientes)                   │
│  │                                                              │
│  └─ Cliente 1 (Aplicación Java)                               │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  PC 3: Servidor + 2 Clientes                                    │
│  ├─ Servidor (Java) - Puerto 8082 (API REST)                 │
│  │                  - Puerto 5003 (Federación)                 │
│  │                  - Puerto 9999 (Clientes)                   │
│  │                                                              │
│  ├─ Cliente 1 (Aplicación Java)                               │
│  └─ Cliente 2 (Aplicación Java)                               │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  PC 4: Servidor + 2 Clientes                                    │
│  ├─ Servidor (Java) - Puerto 8083 (API REST)                 │
│  │                  - Puerto 5004 (Federación)                 │
│  │                  - Puerto 9999 (Clientes)                   │
│  │                                                              │
│  ├─ Cliente 1 (Aplicación Java)                               │
│  └─ Cliente 2 (Aplicación Java)                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Paso 1: Obtener IPs de cada PC

Necesitas conocer la **IP real de cada PC** en la red local (no localhost).

### En cada PC, ejecuta:

**Windows:**
```powershell
ipconfig
```

Busca una IP como `192.168.x.x` o `10.x.x.x`

**Linux/Mac:**
```bash
ifconfig
```

---

## Paso 2: Configuración en PC 1 (Web Admin)

### En `web-admin/.env` (crear si no existe):

```env
# IP de los servidores en la red
VITE_SERVER_1_URL=http://<IP-PC-2>:8081/api/v1/server
VITE_SERVER_2_URL=http://<IP-PC-3>:8082/api/v1/server
VITE_SERVER_3_URL=http://<IP-PC-4>:8083/api/v1/server

# API Gateway (si lo usas, si no deja deshabilitado)
VITE_API_GATEWAY_URL=http://<IP-PC-1>:8000
```

### Reemplazar `<IP-PC-X>` con IPs reales:
- Ejemplo: `http://192.168.1.10:8081/api/v1/server`

---

## Paso 3: Configuración en PC 2, 3 y 4 (Servidores)

### Importante: Modificar los archivos de propiedades

Para que cada servidor **acepte conexiones desde otros PCs**, necesitas:

#### En `servidor/src/main/resources/application-server2.properties` (PC 2):

```properties
# Escuchar en todas las interfaces (no solo localhost)
server.address=0.0.0.0

# Puerto de la API REST
server.port=8081

# Información del servidor
chat.server.name=Servidor-2
chat.server.federation-port=5002

# Base de datos LOCAL en cada PC
spring.datasource.url=jdbc:mysql://localhost:3306/chat_db_server2?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=0000

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
chat.server.max-connections=10

# Puerto TCP para clientes
chat.server.client-port=9999
```

#### Lo mismo para `application-server3.properties` (PC 3):

```properties
server.address=0.0.0.0
server.port=8082
chat.server.name=Servidor-3
chat.server.federation-port=5003
chat.server.client-port=9999
# ... resto igual
```

#### Y para `application-server4.properties` (PC 4):

```properties
server.address=0.0.0.0
server.port=8083
chat.server.name=Servidor-4
chat.server.federation-port=5004
chat.server.client-port=9999
# ... resto igual
```

---

## Paso 4: Configuración de Federación entre Servidores

Una vez que los servidores estén ejecutándose, necesitas federarlos.

### Opción A: Manual (desde UI o curl)

Conectar Servidor-2 con Servidor-3 y Servidor-4:

```bash
# Desde PC 2, conectar con Servidor-3 (PC 3)
curl -X POST "http://localhost:8081/api/v1/server/federation/connect?ip=<IP-PC-3>&port=5003"

# Desde PC 2, conectar con Servidor-4 (PC 4)
curl -X POST "http://localhost:8081/api/v1/server/federation/connect?ip=<IP-PC-4>&port=5004"
```

### Opción B: Automática (recomendado)

Modificar los archivos de propiedades para incluir hosts federados:

```properties
# En application-server2.properties
chat.server.federation.default-peers=<IP-PC-3>:5003,<IP-PC-4>:5004

# En application-server3.properties
chat.server.federation.default-peers=<IP-PC-2>:5002,<IP-PC-4>:5004

# En application-server4.properties
chat.server.federation.default-peers=<IP-PC-2>:5002,<IP-PC-3>:5003
```

---

## Paso 5: Configuración de Clientes

### En cada PC (2, 3, 4), cuando inicia un cliente:

Cuando el cliente pregunte por IP y puerto:
- **IP:** Usa `localhost` o `127.0.0.1` (porque está en el mismo PC)
- **Puerto:** El puerto del servidor en ese mismo PC:
  - PC 2: `9999` (o el puerto del servidor local)
  - PC 3: `9999` (o el puerto del servidor local)
  - PC 4: `9999` (o el puerto del servidor local)

---

## Paso 6: Inicialización

### PC 1 (Web Admin):
```batch
IniciarWebAdmin.bat
```
O para producción:
```batch
BuildWebAdmin.bat
ServirWebAdmin.bat
```

### PC 2, 3, 4 (Servidores + Clientes):

Compilar proyecto (solo primera vez):
```bash
mvn clean package -DskipTests -pl servidor,comun,cliente
```

Ejecutar servidor específico:
```bash
# PC 2
cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server2

# PC 3
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server3

# PC 4
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server4
```

Luego ejecutar clientes:
```bash
cd cliente\target
java -jar cliente-0.0.1-SNAPSHOT.jar
```

---

## Requisitos por PC

### PC 1 (Web Admin):
- ✅ Node.js 18+ instalado
- ✅ navegador web
- ✅ Acceso a la red local en puertos 8081, 8082, 8083

### PC 2, 3, 4 (Servidores + Clientes):
- ✅ Java 11+ instalado
- ✅ Maven 3.6+ instalado
- ✅ MySQL server corriendo localmente
- ✅ Base de datos creada: `chat_db_server2`, `chat_db_server3`, `chat_db_server4`
- ✅ Puertos 8081/8082/8083, 5002/5003/5004, 9999 disponibles

---

## Checklist de Configuración

### Antes de iniciar:

- [ ] Verificar IPs de cada PC
- [ ] Actualizar `.env` en PC 1 con IPs de servidores
- [ ] Actualizar `application-serverX.properties` con `server.address=0.0.0.0`
- [ ] Actualizar `application-serverX.properties` con IPs de federación
- [ ] MySQL corriendo en cada PC con base de datos creada
- [ ] Compilar proyecto con Maven
- [ ] Firewall permite tráfico en puertos 8081-8083, 5002-5004, 9999

---

## Troubleshooting

### Web Admin no puede conectarse a servidores:
- Verificar que servidores estén escuchando en `0.0.0.0` (no `127.0.0.1`)
- Verificar firewall permite conexiones entrantes en puerto 8081/8082/8083
- Verificar IP correcta en `.env`

### Servidores no se comunican (federación):
- Verificar puertos 5002, 5003, 5004 están abiertos
- Usar IP real de cada PC (no localhost) en configuración de federación

### Clientes no se conectan a servidor:
- Usar `localhost` o `127.0.0.1` (mismo PC)
- Usar puerto `9999` (o el configurado en `application-serverX.properties`)

---

## Diagrama de Puertos

```
PC 1 (Web Admin):
  - 5173 (dev) / 8888 (prod): Web Admin UI

PC 2 (Servidor-2):
  - 8081: API REST (para web admin)
  - 5002: Puerto de Federación
  - 9999: Puerto de Clientes
  
PC 3 (Servidor-3):
  - 8082: API REST (para web admin)
  - 5003: Puerto de Federación
  - 9999: Puerto de Clientes

PC 4 (Servidor-4):
  - 8083: API REST (para web admin)
  - 5004: Puerto de Federación
  - 9999: Puerto de Clientes
```

