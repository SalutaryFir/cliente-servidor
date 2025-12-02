# 🚀 Guía de Ejecución: PC1 (Web Admin + API Gateway) + PC2 (Servidor-2)

## 📋 Resumen de Configuración

```
┌─────────────────────────────────────────────┐
│  PC 1: Web Admin + API Gateway              │
│  ├─ Kong (API Gateway) - Puerto 8000        │
│  └─ React Dashboard - Puerto 5173/8888      │
└────────────────┬────────────────────────────┘
                 │ HTTP (Kong enruta)
                 ↓
┌─────────────────────────────────────────────┐
│  PC 2: Servidor-2                           │
│  ├─ API REST - Puerto 8081                  │
│  ├─ Federación - Puerto 5002                │
│  ├─ Clientes TCP - Puerto 9999              │
│  └─ MySQL local - chat_db_server2           │
└─────────────────────────────────────────────┘
```

---

## ⚙️ PREPARACIÓN INICIAL (Una sola vez)

### 📍 Paso 0: Obtener IPs

**En PC 1:**
```powershell
ipconfig
# Anota la IP (ej: 192.168.1.100)
```

**En PC 2:**
```powershell
ipconfig
# Anota la IP (ej: 192.168.1.101)
```

---

## 🖥️ CONFIGURACIÓN EN PC 2 (Servidor)

### Paso 1: Verificar MySQL

```powershell
# Abrir servicios de Windows
services.msc
# Buscar "MySQL" y verificar que esté iniciado

# O desde terminal:
net start MySQL
```

### Paso 2: Crear Base de Datos

```sql
# Conectar a MySQL
mysql -u root -p

# Crear base de datos
CREATE DATABASE IF NOT EXISTS chat_db_server2;
exit;
```

### Paso 3: Verificar Configuración del Servidor

El archivo `servidor/src/main/resources/application-server2.properties` debe tener:

```properties
server.address=0.0.0.0  # ← IMPORTANTE: Permite conexiones externas
server.port=8081
chat.server.name=Servidor-2
chat.server.federation-port=5002
chat.server.client-port=9999
spring.datasource.password=TU_PASSWORD  # ← Actualiza con tu password
```

### Paso 4: Compilar Proyecto

```powershell
cd "C:\Users\USER\Documents\PROYECTO ARQUI\cliente-servidor"
mvn clean package -DskipTests -pl servidor,comun
```

### Paso 5: Iniciar Servidor-2

```powershell
cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server2
```

**Espera a ver:**
```
Started ServidorApplication in X.XXX seconds
```

### Paso 6: Verificar Servidor está Corriendo

En otra terminal en PC 2:
```powershell
curl http://localhost:8081/api/v1/server/health
# Debe responder: {"status":"UP"}
```

---

## 🖥️ CONFIGURACIÓN EN PC 1 (Web Admin + API Gateway)

### Paso 1: Verificar Docker Desktop

```powershell
# Verificar Docker está instalado y corriendo
docker --version
docker info
```

Si no tienes Docker, descarga de: https://www.docker.com/products/docker-desktop

### Paso 2: Iniciar Kong (API Gateway)

```powershell
cd "C:\Users\USER\Documents\PROYECTO ARQUI\cliente-servidor"

# Iniciar Kong
docker-compose -f docker-compose-api-gateway.yml up -d kong

# Esperar ~30 segundos
timeout /t 30
```

### Paso 3: Verificar Kong está Corriendo

```powershell
curl http://localhost:8001
# Debe responder con información de Kong
```

### Paso 4: Configurar Ruta en Kong para Servidor-2

**IMPORTANTE:** Reemplaza `<IP-PC-2>` con la IP real del PC 2 (ej: 192.168.1.101)

```powershell
# Crear servicio para Servidor-2
curl -X POST http://localhost:8001/services ^
    --data "name=chat-server-2" ^
    --data "url=http://<IP-PC-2>:8081" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000"

# Crear ruta
curl -X POST http://localhost:8001/services/chat-server-2/routes ^
    --data "name=route-server-2" ^
    --data "paths[]=/server2" ^
    --data "strip_path=true"

# Habilitar CORS
curl -X POST http://localhost:8001/services/chat-server-2/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true"
```

### Paso 5: Probar Ruta de Kong

```powershell
# Probar a través del API Gateway
curl http://localhost:8000/server2/api/v1/server/health
# Debe responder: {"status":"UP"}
```

Si funciona, **Kong está enrutando correctamente** a PC 2.

### Paso 6: Configurar Web Admin

Editar `web-admin\.env`:

```env
# API Gateway URL (Kong en PC 1)
VITE_API_GATEWAY_URL=http://localhost:8000

# NO necesitas cambiar nada más
# El web admin usará Kong para llegar a los servidores
```

### Paso 7: Iniciar Web Admin

**Opción A: Modo Desarrollo (recomendado para pruebas)**

```powershell
cd web-admin
npm install
npm run dev
```

Abre automáticamente: `http://localhost:5173`

**Opción B: Modo Producción**

```powershell
cd web-admin
npm install
npm run build

# Servir con http-server
npm install -g http-server
cd dist
http-server -p 8888
```

Abre: `http://localhost:8888`

---

## ✅ VERIFICACIÓN COMPLETA

### 1. Verificar Kong está enrutando

En PC 1:
```powershell
curl http://localhost:8000/server2/api/v1/server/info
```

Debe mostrar información del Servidor-2.

### 2. Verificar Web Admin

1. Abre navegador: `http://localhost:5173`
2. Deberías ver el dashboard
3. Debe mostrar **Servidor 2** con estado **Conectado**
4. Debe mostrar usuarios: 0

### 3. Verificar desde PC 1 a PC 2 directamente

En PC 1:
```powershell
curl http://<IP-PC-2>:8081/api/v1/server/health
```

Si no funciona, verificar:
- Firewall de Windows en PC 2
- Puerto 8081 está abierto

---

## 🔥 ABRIR FIREWALL EN PC 2 (Si es necesario)

```powershell
# Ejecutar como Administrador en PC 2
New-NetFirewallRule -DisplayName "Chat Server API" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Chat Server Federation" -Direction Inbound -LocalPort 5002 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Chat Server Clients" -Direction Inbound -LocalPort 9999 -Protocol TCP -Action Allow
```

---

## 🎯 FLUJO DE DATOS

```
Usuario → Web Admin (PC 1, puerto 5173)
              ↓
        Kong (PC 1, puerto 8000)
              ↓
   Ruta /server2 → http://<IP-PC-2>:8081
              ↓
        Servidor-2 (PC 2)
              ↓
        MySQL (PC 2)
              ↓
        Respuesta al usuario
```

---

## 🔧 COMANDOS ÚTILES

### Ver logs de Kong
```powershell
docker-compose -f docker-compose-api-gateway.yml logs -f kong
```

### Ver servicios configurados en Kong
```powershell
curl http://localhost:8001/services
```

### Ver rutas configuradas en Kong
```powershell
curl http://localhost:8001/routes
```

### Detener Kong
```powershell
docker-compose -f docker-compose-api-gateway.yml down
```

### Reiniciar Kong
```powershell
docker-compose -f docker-compose-api-gateway.yml restart kong
```

---

## ❌ PROBLEMAS COMUNES

### ❌ "Web Admin no ve Servidor-2"

**Solución:**
1. Verificar Kong está corriendo: `curl http://localhost:8001`
2. Verificar ruta en Kong: `curl http://localhost:8000/server2/api/v1/server/health`
3. Verificar Servidor-2 está corriendo en PC 2
4. Revisar logs de Kong: `docker-compose -f docker-compose-api-gateway.yml logs kong`

### ❌ "Kong no puede conectar a PC 2"

**Solución:**
1. Ping desde PC 1 a PC 2: `ping <IP-PC-2>`
2. Verificar firewall en PC 2 permite puerto 8081
3. Verificar `application-server2.properties` tiene `server.address=0.0.0.0`
4. Curl directo desde PC 1: `curl http://<IP-PC-2>:8081/api/v1/server/health`

### ❌ "MySQL connection refused"

**Solución:**
1. Verificar MySQL está corriendo: `net start MySQL`
2. Verificar base de datos existe: `mysql -u root -p -e "SHOW DATABASES;"`
3. Verificar password en `application-server2.properties`

### ❌ "Docker no inicia"

**Solución:**
1. Abrir Docker Desktop manualmente
2. Verificar Hyper-V está habilitado (Windows)
3. Reiniciar computadora

---

## 📊 URLs de Acceso

### En PC 1:
- **Web Admin:** `http://localhost:5173` (desarrollo) o `http://localhost:8888` (producción)
- **Kong Proxy:** `http://localhost:8000`
- **Kong Admin API:** `http://localhost:8001`
- **Servidor-2 vía Kong:** `http://localhost:8000/server2/api/v1/server/*`

### En PC 2:
- **API REST Local:** `http://localhost:8081/api/v1/server/*`
- **Health Check:** `http://localhost:8081/api/v1/server/health`

### Desde PC 1 a PC 2 (directo):
- **API REST:** `http://<IP-PC-2>:8081/api/v1/server/*`

---

## 🎓 ORDEN DE EJECUCIÓN RESUMIDO

```
┌─────────────────────────────────────────────┐
│  PC 2: Servidor                             │
├─────────────────────────────────────────────┤
│  1. MySQL iniciado                          │
│  2. Base de datos creada                    │
│  3. mvn clean package -DskipTests           │
│  4. java -jar servidor...jar (server2)      │
│  ✅ Esperar "Started ServidorApplication"   │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  PC 1: Web Admin + Gateway                  │
├─────────────────────────────────────────────┤
│  1. Docker Desktop iniciado                 │
│  2. docker-compose up -d kong               │
│  3. Configurar ruta en Kong (curl)          │
│  4. Probar ruta: curl localhost:8000/...   │
│  5. cd web-admin && npm run dev             │
│  ✅ Abrir: http://localhost:5173            │
└─────────────────────────────────────────────┘
```

---

## 📝 CHECKLIST ANTES DE EMPEZAR

### PC 2:
- [ ] Java 11+ instalado
- [ ] Maven 3.6+ instalado
- [ ] MySQL corriendo
- [ ] Base de datos `chat_db_server2` creada
- [ ] Password MySQL actualizado en properties
- [ ] IP anotada

### PC 1:
- [ ] Docker Desktop instalado y corriendo
- [ ] Node.js 18+ instalado
- [ ] npm instalado
- [ ] IP de PC 2 conocida
- [ ] Ambos PCs en la misma red

---

## 🚀 SIGUIENTE PASO

Una vez funcionando con PC 1 y PC 2, replicar para PC 3 y PC 4:

1. Crear servicio `chat-server-3` y `chat-server-4` en Kong
2. Actualizar rutas `/server3` y `/server4`
3. Iniciar Servidor-3 y Servidor-4 en sus respectivos PCs

---

¿Todo claro? Empieza con PC 2, luego PC 1, y prueba paso a paso.

