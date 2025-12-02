# Scripts de Ejecución - Guía Rápida

## 📋 Orden de Ejecución

### En PC 1 (Web Admin + Monitoreo)

```bash
# 1. Aplicativo Web (elige modo: desarrollo, compilar o servir)
1-IniciarWebAdmin.bat

# 2. API Gateway + Monitoreo (opcional, si usas Docker)
2-ConfigurarAPIGateway.bat

# 3. Monitoreo (Grafana, Prometheus, Loki)
3-IniciarMonitoreo.bat
```

### En PC 2, 3, 4 (Servidores + Clientes)

```bash
# 1. Inicia el servidor que te corresponda
4-IniciarServidor.bat
   → Selecciona servidor 2, 3 o 4

# 2. En otra terminal, inicia cliente(s)
5-IniciarCliente.bat
   → Conecta a localhost:9999
```

---

## 🎯 Los 5 Scripts Principales

| Script | Función | Puerto | Ubicación |
|--------|---------|--------|-----------|
| **1-IniciarWebAdmin.bat** | Dashboard web (desarrollo/producción) | 5173 / 8888 | PC 1 |
| **2-ConfigurarAPIGateway.bat** | Inicia Kong + configura rutas | 8000 / 8001 | PC 1 |
| **3-IniciarMonitoreo.bat** | Grafana, Prometheus, Loki, Tempo | 3001 | PC 1 |
| **4-IniciarServidor.bat** | Inicia servidor específico | 8080-8083 | PC 2, 3, 4 |
| **5-IniciarCliente.bat** | Cliente de chat | Remoto | PC 2, 3, 4 |

---

## 🚀 Ejemplo Completo

### Escenario: 4 PCs, prueba local

#### PC 1:
```bash
# Terminal 1
1-IniciarWebAdmin.bat
→ Selecciona opción 1 (desarrollo)
→ Abre http://localhost:5173

# Terminal 2
3-IniciarMonitoreo.bat
→ Abre http://localhost:3001 (Grafana)
```

#### PC 2:
```bash
# Terminal 1
4-IniciarServidor.bat
→ Selecciona opción 2 (Servidor 2)
→ API: http://localhost:8081

# Terminal 2
5-IniciarCliente.bat
→ Conecta a: localhost:9999
```

#### PC 3:
```bash
# Terminal 1
4-IniciarServidor.bat
→ Selecciona opción 3 (Servidor 3)

# Terminal 2-3
5-IniciarCliente.bat (x2)
```

#### PC 4:
```bash
# Terminal 1
4-IniciarServidor.bat
→ Selecciona opción 4 (Servidor 4)

# Terminal 2-3
5-IniciarCliente.bat (x2)
```

---

## 🔧 URLs de Acceso

### Desarrollo Local
```
Web Admin:     http://localhost:5173   (desarrollo)
Web Admin:     http://localhost:8888   (producción)
API Server 1:  http://localhost:8080
API Server 2:  http://localhost:8081
API Server 3:  http://localhost:8082
API Server 4:  http://localhost:8083
```

### Monitoreo
```
Grafana:       http://localhost:3001   (admin/admin)
Prometheus:    http://localhost:9090
Loki:          http://localhost:3100
Tempo:         http://localhost:3200
```

### Kong Gateway
```
API Gateway:   http://localhost:8000/server1/*
Kong Admin:    http://localhost:8001
```

---

## 🔌 Puertos

| Rango | Propósito |
|-------|-----------|
| 8080-8083 | API REST de servidores |
| 5001-5004 | Federación entre servidores |
| 9999 | Clientes TCP |
| 5173 | Web Admin (desarrollo) |
| 8888 | Web Admin (producción) |
| 3000-3200 | Monitoreo (Kong, Grafana, Prometheus, Loki, Tempo) |

---

## 💾 Scripts Antiguos

Los scripts anteriores han sido archivados en la carpeta `scripts-legacy/` para evitar confusión:
- IniciarServidores.bat (antigua)
- IniciarClientes.bat (antigua)
- IniciarWebAdmin.bat (antigua)
- BuildWebAdmin.bat (antigua)
- ServirWebAdmin.bat (antigua)
- IniciarMonitoreo.bat (antigua)
- IniciarServidorDistribuido.bat (antigua)
- ConfigurarRed.bat (antigua)
- Inicializador.bat (antigua)

Para volver a usarlos, están en `scripts-legacy/`

---

## ⚡ Comandos Útiles

### Docker
```bash
# Ver servicios en ejecución
docker-compose -f docker-compose-api-gateway.yml ps

# Ver logs
docker-compose -f docker-compose-api-gateway.yml logs -f

# Detener servicios
docker-compose -f docker-compose-api-gateway.yml down
```

### Maven
```bash
# Compilar sin tests
mvn clean package -DskipTests -pl servidor,comun

# Compilar todo
mvn clean package -DskipTests
```

### Curl (Pruebas)
```bash
# Health check
curl http://localhost:8080/api/v1/server/health

# Federación
curl -X POST "http://localhost:8080/api/v1/server/federation/connect?ip=192.168.1.x&port=5002"
```

---

## 📱 Requisitos

### Todos los PCs
- Java 11+
- Maven 3.6+
- MySQL Server (localmente en cada PC)

### PC 1 (adicional)
- Node.js 18+
- Docker Desktop (si usas monitoreo)

### PC 2, 3, 4
- Base de datos MySQL: `chat_db_server2`, `chat_db_server3`, `chat_db_server4`

---

## ❓ Troubleshooting

### "No se puede conectar a servidor"
- Verifica que servidor esté ejecutándose en mismo PC
- Usa `localhost:9999` (no IP externa)

### "Web Admin no ve servidores"
- Verifica firewall permita puerto 8080-8083
- Comprueba IP correcta en `.env`

### "Docker no inicia"
- Abre Docker Desktop
- Verifica puertos 8000-8001, 3000-3200 disponibles

### "Maven no compila"
- Verifica Java instalado: `java -version`
- Verifica Maven instalado: `mvn -version`

---

Generated: 2025-12-01
