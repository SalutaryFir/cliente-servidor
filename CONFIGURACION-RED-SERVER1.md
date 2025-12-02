# 🌐 CONFIGURACIÓN DE RED - SERVIDOR 1

## IP Configurada

```
Servidor 1: 192.168.137.82
```

---

## 📋 Configuración por Archivo

### 1. **Prometheus** (`monitoring/prometheus.yml`)
```yaml
- job_name: 'chat-server-1'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['192.168.137.82:8080']
```

### 2. **Kong Routes** (`ConfigurarRutasKong.bat`)
```batch
curl -i -X POST http://localhost:8001/services/ ^
  --data "name=server1" ^
  --data "url=http://192.168.137.82:8080"
```

### 3. **Guía de Ejecución** (`GUIA-EJECUCION-4PCS.md`)
```bash
# Federar desde Servidor 1
curl -X POST "http://192.168.137.82:8080/api/v1/server/federation/connect?ip=IP_PC2&port=5003"
```

---

## 🔗 URLs de Acceso

### Desde la misma red (192.168.137.x):

| Servicio | URL |
|----------|-----|
| Web Admin | http://192.168.137.82:5173 |
| Servidor 1 API | http://192.168.137.82:8080 |
| Kong Gateway | http://192.168.137.82:8000 |
| Kong Admin | http://192.168.137.82:8001 |
| Grafana | http://192.168.137.82:3001 |
| Prometheus | http://192.168.137.82:9090 |

---

## ⚙️ Configurar Otros Servidores

### Server 2 (PC2)
```yaml
# prometheus.yml
targets: ['IP_PC2:8081']

# Kong
url=http://IP_PC2:8081
```

### Server 3 (PC3)
```yaml
# prometheus.yml
targets: ['IP_PC3:8082']

# Kong
url=http://IP_PC3:8082
```

### Server 4 (PC4)
```yaml
# prometheus.yml
targets: ['IP_PC4:8083']

# Kong
url=http://IP_PC4:8083
```

---

## 🔐 Firewall - Puertos a Abrir en PC1

```powershell
# En PC1 (192.168.137.82) - Como Administrador

# Servidor 1
New-NetFirewallRule -DisplayName "Server 1 Client" -Direction Inbound -LocalPort 5000 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Server 1 Federation" -Direction Inbound -LocalPort 5001 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Server 1 HTTP" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow

# Kong
New-NetFirewallRule -DisplayName "Kong Proxy" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Kong Admin" -Direction Inbound -LocalPort 8001 -Protocol TCP -Action Allow

# Web Admin
New-NetFirewallRule -DisplayName "Web Admin" -Direction Inbound -LocalPort 5173 -Protocol TCP -Action Allow

# Grafana
New-NetFirewallRule -DisplayName "Grafana" -Direction Inbound -LocalPort 3001 -Protocol TCP -Action Allow

# Prometheus
New-NetFirewallRule -DisplayName "Prometheus" -Direction Inbound -LocalPort 9090 -Protocol TCP -Action Allow
```

---

## 📝 Notas Importantes

- ✅ Servidor 1 está configurado en: **192.168.137.82**
- ✅ Web Admin accede a Kong que reenvía a todos los servidores
- ✅ Los demás servidores (2, 3, 4) deben actualizar `prometheus.yml` con sus IPs reales
- ✅ Todos los servidores deben tener abiertos los puertos correspondientes
- ✅ Los clientes (otros PCs) pueden conectarse a `192.168.137.82:5000` para Server 1

---

## 🧪 Verificar Conexión

```bash
# Desde cualquier PC en la red
ping 192.168.137.82

# Verificar que Kong está corriendo
curl http://192.168.137.82:8000/health

# Verificar Servidor 1
curl http://192.168.137.82:8080/api/v1/server/health

# Verificar Web Admin
curl http://192.168.137.82:5173
```

---

¡Servidor 1 está configurado correctamente en 192.168.137.82! 🎯
