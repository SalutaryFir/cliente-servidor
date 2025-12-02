# ⚡ INICIO RÁPIDO: 2 PCs con API Gateway

## 🎯 Objetivo
- **PC 1:** Web Admin + Kong API Gateway
- **PC 2:** Servidor-2 de Chat

---

## 📋 Pasos Rápidos

### 1️⃣ EN PC 2 (Servidor)

```powershell
# Verificar MySQL
net start MySQL

# Crear BD
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS chat_db_server2;"

# Ejecutar script
PC2-IniciarServidor.bat
→ Selecciona: 2 (Servidor 2)
→ Anota la IP que te muestra
→ Espera ver: "Started ServidorApplication"
```

**Deja corriendo** ✅

---

### 2️⃣ EN PC 1 (Web Admin + Gateway)

#### Parte A: Iniciar Kong y Web Admin

```powershell
PC1-IniciarTodo.bat
→ Espera que Kong inicie (~30 seg)
→ Selecciona modo: 1 (Desarrollo)
→ Abre: http://localhost:5173
```

**Deja corriendo en segundo plano** ✅

#### Parte B: Configurar Kong (en otra terminal)

```powershell
PC1-ConfigurarKong.bat
→ Número de servidor: 2
→ IP del PC: <IP-que-anotaste-de-PC2>
→ Ejemplo: 192.168.1.101
→ Confirma
→ Espera [OK] Conexión exitosa
```

---

## ✅ Verificación

### En PC 1:

1. **Abrir navegador:** `http://localhost:5173`
2. **Debe mostrar:** Dashboard con Servidor 2 conectado
3. **Probar health:** 
   ```powershell
   curl http://localhost:8000/server2/api/v1/server/health
   ```

---

## 🎨 Vista del Dashboard

```
┌────────────────────────────────────────┐
│  Chat Server Admin                     │
├────────────────────────────────────────┤
│                                        │
│  🟢 Servidor 2                         │
│     Puerto: 8081                       │
│     Usuarios: 0                        │
│     Estado: Conectado                  │
│                                        │
└────────────────────────────────────────┘
```

---

## 🔧 Scripts Creados

| Script | PC | Función |
|--------|----|---------| 
| `PC2-IniciarServidor.bat` | 2 | Inicia servidor de chat |
| `PC1-IniciarTodo.bat` | 1 | Inicia Kong + Web Admin |
| `PC1-ConfigurarKong.bat` | 1 | Configura ruta en Kong |

---

## 📖 Documentación Completa

Ver: **[GUIA-EJECUCION-2PCS.md](GUIA-EJECUCION-2PCS.md)**

---

## ❌ Si algo falla

### PC 2: Servidor no inicia
```powershell
# Verificar MySQL
mysql -u root -p -e "SHOW DATABASES;"

# Ver logs en la terminal del servidor
```

### PC 1: Web Admin no ve servidor
```powershell
# Probar Kong
curl http://localhost:8001

# Probar ruta
curl http://localhost:8000/server2/api/v1/server/health

# Ver logs Kong
docker-compose -f docker-compose-api-gateway.yml logs kong
```

### No hay conexión entre PCs
```powershell
# En PC 1, hacer ping a PC 2
ping <IP-PC-2>

# Probar conexión directa
curl http://<IP-PC-2>:8081/api/v1/server/health

# Abrir firewall en PC 2 (ejecutar como Admin)
New-NetFirewallRule -DisplayName "Chat API" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow
```

---

## 🚀 Todo funciona? Siguiente paso:

Agrega PC 3 y PC 4 repitiendo el proceso:
- Ejecuta `PC2-IniciarServidor.bat` en cada PC (selecciona 3 o 4)
- Ejecuta `PC1-ConfigurarKong.bat` para cada servidor nuevo

---

**Tiempo total:** ~5 minutos  
**¿Dudas?** Lee [GUIA-EJECUCION-2PCS.md](GUIA-EJECUCION-2PCS.md)

