# ============================================
# GUÍA DE DESPLIEGUE DISTRIBUIDO
# ============================================

## 📋 Escenarios de Despliegue

### ESCENARIO 1: Todo en un computador (actual)
✅ **Configuración**: Ninguna adicional necesaria
- Servidores: localhost (127.0.0.1)
- Clientes: Se conectan a localhost
- Web Admin: localhost:3000
- API Gateway (Kong): localhost:8000

---

### ESCENARIO 2: Servidores distribuidos en múltiples PCs

#### **Ejemplo**: 
- PC1 (192.168.1.10): Servidor-1 + Servidor-2
- PC2 (192.168.1.20): Servidor-3 + Servidor-4
- PC3 (192.168.1.30): API Gateway + Web Admin

#### ⚙️ **Configuración necesaria**:

##### A) En cada servidor Java:

**PC1 - Servidor-1** (`application-server1.properties`):
```properties
# IP pública/LAN del servidor
server.address=192.168.1.10

# Puertos
chat.server.client-port=9999
chat.server.federation-port=5001
server.port=8080  # API REST

# MySQL local o remoto
spring.datasource.url=jdbc:mysql://192.168.1.10:3306/chat_db_server1
```

**PC1 - Servidor-2** (`application-server2.properties`):
```properties
server.address=192.168.1.10
chat.server.client-port=5002
chat.server.federation-port=5003
server.port=8081
spring.datasource.url=jdbc:mysql://192.168.1.10:3306/chat_db_server2
```

**PC2 - Servidor-3** (`application-server3.properties`):
```properties
server.address=192.168.1.20
chat.server.client-port=5004
chat.server.federation-port=5005
server.port=8082
spring.datasource.url=jdbc:mysql://192.168.1.20:3306/chat_db_server3
```

**PC2 - Servidor-4** (`application-server4.properties`):
```properties
server.address=192.168.1.20
chat.server.client-port=5006
chat.server.federation-port=5007
server.port=8083
spring.datasource.url=jdbc:mysql://192.168.1.20:3306/chat_db_server4
```

##### B) Federación entre servidores:

En la UI de Servidor-1 (PC1), conectar a:
- Servidor-2: 192.168.1.10:5003
- Servidor-3: 192.168.1.20:5005
- Servidor-4: 192.168.1.20:5007

O via API REST:
```bash
curl -X POST http://192.168.1.10:8080/api/v1/server/federation/connect?ip=192.168.1.10&port=5003
curl -X POST http://192.168.1.10:8080/api/v1/server/federation/connect?ip=192.168.1.20&port=5005
curl -X POST http://192.168.1.10:8080/api/v1/server/federation/connect?ip=192.168.1.20&port=5007
```

##### C) Clientes Java (Desktop):

Al iniciar cada cliente, ingresar en el diálogo:
- **IP**: 192.168.1.10 (o la IP del servidor deseado)
- **Puerto**: 9999, 5002, 5004, o 5006

Pueden conectarse desde cualquier PC de la red.

---

### ESCENARIO 3: Web Admin + API Gateway distribuidos

#### **¿Dónde desplegar la Web Admin?**

**Opción A: En el mismo servidor que API Gateway (RECOMENDADO)**
- PC3 ejecuta Kong (API Gateway) y Web Admin
- Ventaja: Comunicación local rápida
- Configuración Kong apunta a IPs remotas de servidores

**Opción B: En servidor separado**
- PC4 ejecuta solo Web Admin
- Web Admin apunta a Kong en PC3

**Opción C: En servidor coordinador**
- PC1 ejecuta Servidor-1 + Kong + Web Admin
- Todo centralizado en un nodo principal

#### ⚙️ **Configuración Kong (docker-compose-api-gateway.yml)**:

```yaml
services:
  kong:
    # ... configuración existente ...
    networks:
      - chat-network
    extra_hosts:
      - "server1:192.168.1.10"
      - "server2:192.168.1.10"
      - "server3:192.168.1.20"
      - "server4:192.168.1.20"
```

#### 📝 **Script de configuración Kong** (`configure-kong.bat`):

Actualizar las URLs a IPs reales:

```batch
REM Servidor 1
curl -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-1" ^
    --data "url=http://192.168.1.10:8080"

REM Servidor 2
curl -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-2" ^
    --data "url=http://192.168.1.10:8081"

REM Servidor 3
curl -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-3" ^
    --data "url=http://192.168.1.20:8082"

REM Servidor 4
curl -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-4" ^
    --data "url=http://192.168.1.20:8083"
```

#### 🌐 **Acceso a la Web Admin**:

Si Web Admin está en PC3 (192.168.1.30):
- Acceso: `http://192.168.1.30:3000`
- Cualquier usuario en la red puede acceder
- Kong debe aceptar conexiones: `0.0.0.0:8000` (ya configurado)

---

## 🔥 Firewall & Puertos a Abrir

### En cada PC servidor:

**Puertos TCP a permitir**:

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Cliente Chat | 9999, 5002, 5004, 5006 | Conexión clientes desktop |
| Federación | 5001, 5003, 5005, 5007 | Comunicación entre servidores |
| API REST | 8080, 8081, 8082, 8083 | APIs para Web Admin |
| MySQL | 3306 | Si base de datos remota |

**Windows Firewall** (ejecutar como admin):
```powershell
# Permitir rango de puertos
New-NetFirewallRule -DisplayName "Chat Servers" -Direction Inbound -LocalPort 5000-9999 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Chat APIs" -Direction Inbound -LocalPort 8080-8083 -Protocol TCP -Action Allow
```

**Linux (ufw)**:
```bash
sudo ufw allow 5000:9999/tcp
sudo ufw allow 8080:8083/tcp
```

### En PC con Kong + Web Admin:

| Servicio | Puerto |
|----------|--------|
| Kong Proxy | 8000 |
| Kong Admin | 8001 |
| Web Admin | 3000 (puerto 80 en Docker) |

---

## 📱 Arquitectura Recomendada

### **Para Producción/Demostración**:

```
┌─────────────────────────────────────────┐
│  PC1 (192.168.1.10)                     │
│  ├── Servidor-1 (Coordinador)           │
│  │   ├── Chat: 9999                     │
│  │   ├── Federación: 5001               │
│  │   └── API: 8080                      │
│  └── Servidor-2                         │
│      ├── Chat: 5002                     │
│      ├── Federación: 5003               │
│      └── API: 8081                      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  PC2 (192.168.1.20)                     │
│  ├── Servidor-3                         │
│  │   ├── Chat: 5004                     │
│  │   ├── Federación: 5005               │
│  │   └── API: 8082                      │
│  └── Servidor-4                         │
│      ├── Chat: 5006                     │
│      ├── Federación: 5007               │
│      └── API: 8083                      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  PC3 (192.168.1.30) - Gateway          │
│  ├── Kong API Gateway :8000             │
│  ├── PostgreSQL (Kong DB) :5432         │
│  └── Web Admin :3000                    │
│      └── Accesible desde cualquier PC   │
└─────────────────────────────────────────┘

        ↓ Clientes pueden estar en cualquier PC
        
┌─────────────────────────────────────────┐
│  PC4, PC5, PC6... (Usuarios)           │
│  ├── Cliente Desktop (Java Swing)       │
│  │   └── Dialogo: IP + Puerto           │
│  └── Navegador Web                      │
│      └── http://192.168.1.30:3000      │
└─────────────────────────────────────────┘
```

---

## ✅ Checklist de Configuración

### Antes de desplegar:

- [ ] Configurar `server.address` en cada `application-serverX.properties`
- [ ] Actualizar URLs de MySQL si es remoto
- [ ] Abrir puertos en firewall de cada servidor
- [ ] Configurar IPs en `configure-kong.bat`
- [ ] Actualizar `docker-compose-api-gateway.yml` con IPs
- [ ] Probar conectividad: `ping` entre PCs
- [ ] Probar puertos: `telnet 192.168.1.10 8080`

### Al iniciar:

1. **Iniciar servidores Java** en cada PC
2. **Federar servidores** desde Servidor-1
3. **Iniciar Kong + Web Admin** en PC Gateway
4. **Configurar rutas Kong** con `configure-kong.bat`
5. **Probar Web Admin**: Abrir navegador
6. **Iniciar clientes**: Ingresar IP del servidor deseado

---

## 🎯 Recomendación para tu caso:

### **Setup Simple (2-3 PCs)**:

**PC1 (Servidor Principal)**:
- Todos los servidores Java (1, 2, 3, 4)
- MySQL local
- Ejecutar: `IniciarServidores.bat`

**PC2 (Gateway & Web)**:
- Docker con Kong + PostgreSQL + Web Admin
- Ejecutar: `docker-compose up -d`
- Configurar: `configure-kong.bat` (cambiar localhost por IP de PC1)

**PC3, PC4... (Usuarios)**:
- Clientes desktop: `IniciarClientes.bat`
  - Al conectar: IP de PC1, puerto deseado
- Navegador: `http://IP-PC2:3000`

### **Setup Completo (4+ PCs)**:

- **PC1-PC2**: Servidores distribuidos (2 por PC)
- **PC3**: Kong + Web Admin + PostgreSQL
- **PC4+**: Solo clientes

---

## 🔧 Archivos que debes modificar:

1. `servidor/src/main/resources/application-serverX.properties` → Agregar `server.address`
2. `configure-kong.bat` → Cambiar localhost por IPs reales
3. `docker-compose-api-gateway.yml` → Actualizar hosts/IPs
4. Documentación `QUICKSTART.md` → Actualizar con IPs

¿Quieres que genere los archivos de configuración para un despliegue distribuido específico?
