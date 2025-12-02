# 🚀 GUÍA DE EJECUCIÓN - CHAT FEDERADO (4 COMPUTADORES)

## 📋 **Requisitos Previos**

Cada computador debe tener instalado:
- ✅ Java JDK 21
- ✅ Maven 3.8+
- ✅ Node.js 18+ (solo para Web Admin)
- ✅ MySQL 8.0+
- ✅ Docker Desktop (solo para Kong/API Gateway)

---

## 🗂️ **Distribución por Computador**

### **Computador Gateway + Web Admin (192.168.137.253)**
- Kong API Gateway
- Web Admin
- Cliente de prueba

### **Computador 1 (192.168.137.82)**
- Servidor 1 (Coordinador)
- Cliente de prueba

### **Computador 2**
- Servidor 2
- Cliente de prueba

### **Computador 3**
- Servidor 3
- Cliente de prueba

### **Computador 4**
- Servidor 4
- Cliente de prueba

---

## 🔧 **1. CONFIGURACIÓN INICIAL (Una sola vez)**

### **En TODOS los computadores:**

#### 1.1 Configurar MySQL
```sql
CREATE DATABASE chat_db_server1;  -- En PC1
CREATE DATABASE chat_db_server2;  -- En PC2
CREATE DATABASE chat_db_server3;  -- En PC3
CREATE DATABASE chat_db_server4;  -- En PC4
```

#### 1.2 Editar archivo de configuración
Abre `servidor/src/main/resources/application-serverX.properties` y ajusta:
```properties
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

#### 1.3 Compilar el proyecto
```bash
.\Compilar.bat
```

---

## 🚀 **2. INICIAR SERVICIOS (En orden)**

### **PASO 1: PC Gateway (192.168.137.253) - API Gateway y Web Admin**

```bash
# 1. Iniciar Kong
.\IniciarKong.bat

# 2. Configurar rutas (después de que Kong esté listo)
.\ConfigurarRutasKong.bat
```

---

### **PASO 2: Iniciar Servidores en sus respectivos computadores**

#### En **Computador 1 (192.168.137.82):**
```bash
.\IniciarServidor1.bat
```

#### En **Computador 2:**
```bash
.\IniciarServidor2.bat
```

#### En **Computador 3:**
```bash
.\IniciarServidor3.bat
```

#### En **Computador 4:**
```bash
.\IniciarServidor4.bat
```

**Espera** a que todos los servidores estén completamente iniciados (verás "🚀 Servidor iniciado").

---

### **PASO 3: Federar Servidores**

Desde **Computador 1 (192.168.137.82)**, conecta todos los servidores:

```bash
# Conectar Servidor 1 con Servidor 2
curl -X POST "http://192.168.137.82:8080/api/v1/server/federation/connect?ip=IP_PC2&port=5003"

# Conectar Servidor 1 con Servidor 3
curl -X POST "http://192.168.137.82:8080/api/v1/server/federation/connect?ip=IP_PC3&port=5005"

# Conectar Servidor 1 con Servidor 4
curl -X POST "http://192.168.137.82:8080/api/v1/server/federation/connect?ip=IP_PC4&port=5007"
```

**Reemplaza** `IP_PC2`, `IP_PC3`, `IP_PC4` con las IPs reales de cada computador.

---

### **PASO 4: Iniciar Web Admin (PC Gateway 192.168.137.253)**

```bash
.\IniciarWebAdmin.bat
```

Accede desde el navegador: **http://192.168.137.253:5173**

---

### **PASO 5: Iniciar Clientes de Prueba**

En **cualquier computador**:

```bash
.\IniciarCliente.bat
```

Cuando se solicite:
- **IP del servidor:** `localhost` (si es local) o `IP_DEL_SERVIDOR`
- **Puerto:** `5000`, `5002`, `5004` o `5006`

---

## 📊 **3. VERIFICAR FUNCIONAMIENTO**

### **3.1 Web Admin**
Abre: http://localhost:5173
- Verás 4 pestañas (Servidor 1, 2, 3, 4)
- Cada pestaña muestra:
  - ℹ️ Información del servidor
  - 👥 Usuarios conectados
  - 📢 Canales
  - 📊 Estadísticas
  - 🌐 Federación
  - 🎙️ Audios procesados
  - 📋 Logs en tiempo real

### **3.2 APIs REST**
```bash
# Verificar Servidor 1
curl http://localhost:8080/api/v1/server/health

# A través de Kong (si está configurado)
curl http://localhost:8000/server1/api/v1/server/health
curl http://localhost:8000/server2/api/v1/server/health
curl http://localhost:8000/server3/api/v1/server/health
curl http://localhost:8000/server4/api/v1/server/health
```

---

## 🔥 **4. FIREWALL (Windows)**

**En cada computador**, abre PowerShell como **Administrador** y ejecuta:

```powershell
# Permitir puertos de servidor
New-NetFirewallRule -DisplayName "Chat Server 1 Client" -Direction Inbound -LocalPort 5000 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Chat Server 1 Federation" -Direction Inbound -LocalPort 5001 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Chat Server 1 HTTP" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow

# Para Server 2, usa puertos: 5002, 5003, 8081
# Para Server 3, usa puertos: 5004, 5005, 8082
# Para Server 4, usa puertos: 5006, 5007, 8083

# Permitir ping
New-NetFirewallRule -DisplayName "Allow ICMPv4 Ping" -Direction Inbound -Protocol ICMPv4 -IcmpType 8 -Action Allow
```

---

## 📝 **5. RESUMEN DE PUERTOS**

| Servidor | Cliente | Federación | API REST | Computador |
|----------|---------|------------|----------|------------|
| Server 1 | 5000    | 5001       | 8080     | PC 1       |
| Server 2 | 5002    | 5003       | 8081     | PC 2       |
| Server 3 | 5004    | 5005       | 8082     | PC 3       |
| Server 4 | 5006    | 5007       | 8083     | PC 4       |

**Adicionales:**
- Kong Gateway: 8000
- Kong Admin: 8001
- Web Admin: 5173
- MySQL: 3306

---

## 🛠️ **6. SCRIPTS DISPONIBLES**

| Script | Función |
|--------|---------|
| `Compilar.bat` | Compila todo el proyecto con Maven |
| `IniciarServidor1.bat` | Inicia Servidor 1 (Coordinador) |
| `IniciarServidor2.bat` | Inicia Servidor 2 |
| `IniciarServidor3.bat` | Inicia Servidor 3 |
| `IniciarServidor4.bat` | Inicia Servidor 4 |
| `IniciarCliente.bat` | Inicia cliente de chat (GUI) |
| `IniciarWebAdmin.bat` | Inicia aplicación web de administración |
| `IniciarKong.bat` | Inicia Kong API Gateway con Docker |
| `ConfigurarRutasKong.bat` | Configura rutas en Kong |

---

## 🔍 **7. SOLUCIÓN DE PROBLEMAS**

### **No puedo hacer ping entre computadores**
```powershell
# Ejecutar como Administrador
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled False
```

### **Puerto ya en uso**
```bash
# Ver qué proceso usa el puerto
netstat -ano | findstr :8080

# Cerrar el proceso
taskkill /PID <PID> /F
```

### **Error de compilación**
```bash
# Limpiar y recompilar
mvn clean
mvn package -DskipTests
```

### **Logs no aparecen en Web Admin**
- Activa el botón **"En Vivo"** en la sección de Logs
- Verifica que el servidor esté ejecutándose
- Revisa la consola del navegador (F12)

---

## ✅ **8. ORDEN DE EJECUCIÓN COMPLETO**

```
1. Compilar.bat                    (En todos los PCs)
2. IniciarKong.bat                 (Solo PC1, opcional)
3. ConfigurarRutasKong.bat         (Solo PC1, después de Kong)
4. IniciarServidor1.bat            (PC1)
5. IniciarServidor2.bat            (PC2)
6. IniciarServidor3.bat            (PC3)
7. IniciarServidor4.bat            (PC4)
8. Federar servidores con curl     (Desde PC1)
9. IniciarWebAdmin.bat             (PC1)
10. IniciarCliente.bat             (Cualquier PC, múltiples instancias)
```

---

## 📞 **9. URLs IMPORTANTES**

### **Computador 1 (192.168.137.82):**
- Web Admin: http://192.168.137.82:5173
- Servidor 1 API: http://192.168.137.82:8080/api/v1/server/info
- Kong Gateway: http://192.168.137.82:8000
- Kong Admin: http://192.168.137.82:8001

### **Otros Computadores:**
- Servidor X API: http://IP_PCX:808X/api/v1/server/info

---

¡Listo! Con esta guía puedes ejecutar el sistema completo en 4 computadores. 🎉
