# 🚀 Guía Rápida - Stack de Monitoreo

## ⚡ Inicio Rápido (3 pasos)

### 1️⃣ Iniciar Stack de Monitoreo
```bash
# Windows
IniciarMonitoreo.bat

# Linux/Mac
./IniciarMonitoreo.sh
```

### 2️⃣ Iniciar Servidores de Chat
```bash
# Windows
IniciarServidores.bat

# Linux/Mac
./IniciarServidores.sh
```

### 3️⃣ Acceder a Grafana
Abre tu navegador en: **http://localhost:3001**
- Usuario: `admin`
- Contraseña: `admin`

---

## 📊 Dashboards y URLs

| Servicio | URL | Propósito |
|----------|-----|-----------|
| 📈 **Grafana** | http://localhost:3001 | Visualización de métricas, logs y trazas |
| 📉 **Prometheus** | http://localhost:9090 | Explorador de métricas en tiempo real |
| 📝 **Loki** | http://localhost:3100 | API de logs (usar desde Grafana) |
| 🔍 **Tempo** | http://localhost:3200 | API de trazas (usar desde Grafana) |
| 🌐 **Web Admin** | http://localhost:3000 | Dashboard administrativo React |
| 🚪 **Kong Admin** | http://localhost:8001 | API de administración de Kong |

---

## 🎯 Tareas Comunes

### Ver Métricas de CPU
1. Ve a Grafana: http://localhost:3001
2. Dashboards → Chat Federado
3. Panel "Uso de CPU por Servidor"

### Buscar Errores en Logs
1. Ve a Grafana → Explore
2. Selecciona datasource: **Loki**
3. Query: `{container=~"chat-server.*"} |= "ERROR"`

### Ver Trazas de una Request
1. Ve a Grafana → Explore
2. Selecciona datasource: **Tempo**
3. Search → Service: `servidor`

### Consultar Métricas en Prometheus
1. Ve a Prometheus: http://localhost:9090
2. Graph → Query:
```promql
# Servidores activos
count(up{job=~"chat-server-.*"} == 1)

# Memoria usada
sum(jvm_memory_used_bytes{job=~"chat-server-.*",area="heap"})

# Requests por segundo
rate(http_server_requests_seconds_count{job=~"chat-server-.*"}[5m])
```

---

## 🔧 Comandos Docker

### Ver servicios corriendo
```bash
docker-compose -f docker-compose-api-gateway.yml ps
```

### Ver logs en tiempo real
```bash
# Todos los servicios
docker-compose -f docker-compose-api-gateway.yml logs -f

# Un servicio específico
docker-compose -f docker-compose-api-gateway.yml logs -f grafana
docker-compose -f docker-compose-api-gateway.yml logs -f prometheus
docker-compose -f docker-compose-api-gateway.yml logs -f loki
docker-compose -f docker-compose-api-gateway.yml logs -f tempo
```

### Reiniciar un servicio
```bash
docker-compose -f docker-compose-api-gateway.yml restart grafana
docker-compose -f docker-compose-api-gateway.yml restart prometheus
```

### Detener todo
```bash
docker-compose -f docker-compose-api-gateway.yml down
```

### Detener y limpiar volúmenes (⚠️ Borra todos los datos)
```bash
docker-compose -f docker-compose-api-gateway.yml down -v
```

---

## 🩺 Verificación de Salud

### Verificar que los endpoints de Actuator funcionan
```bash
# Server 1
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus

# Server 2
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus

# Server 3
curl http://localhost:8082/actuator/health
curl http://localhost:8082/actuator/prometheus

# Server 4
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/prometheus
```

### Verificar que Prometheus está scrapeando
1. Ve a http://localhost:9090/targets
2. Busca `chat-server-1`, `chat-server-2`, `chat-server-3`, `chat-server-4`
3. Estado debe ser **UP** (verde)

### Verificar que Grafana tiene datasources
1. Ve a http://localhost:3001
2. Configuration → Data sources
3. Deberías ver: Prometheus, Loki, Tempo (todos en verde)

---

## 🐛 Troubleshooting Rápido

### Los servidores no aparecen en Prometheus

**Problema**: Prometheus no puede alcanzar `host.docker.internal`

**Solución (Linux)**:
```bash
# Edita monitoring/prometheus.yml
# Cambia host.docker.internal por 172.17.0.1
- targets: ['172.17.0.1:8080']
```

### No veo logs en Loki

**Problema**: Promtail no tiene acceso al socket de Docker

**Solución (Linux)**:
```bash
# Agrega tu usuario al grupo docker
sudo usermod -aG docker $USER
# Reinicia la sesión o ejecuta
newgrp docker
```

### Grafana no carga dashboards

**Problema**: Permisos de carpetas

**Solución**:
```bash
# Dale permisos a la carpeta de dashboards
chmod -R 755 monitoring/grafana/
```

### Puerto ya en uso

**Problema**: Algún servicio ya está usando los puertos

**Solución**:
```bash
# Ver qué está usando el puerto
# Windows
netstat -ano | findstr :3001
netstat -ano | findstr :9090

# Linux/Mac
lsof -i :3001
lsof -i :9090

# Detén el proceso o cambia el puerto en docker-compose
```

---

## 📈 Métricas Disponibles

### JVM
- `jvm_memory_used_bytes` - Memoria usada
- `jvm_memory_max_bytes` - Memoria máxima
- `jvm_threads_live_threads` - Threads activos
- `jvm_gc_pause_seconds` - Pausas del GC

### HTTP
- `http_server_requests_seconds_count` - Contador de requests
- `http_server_requests_seconds_sum` - Suma de tiempos de respuesta
- `http_server_requests_active_count` - Requests activas

### Sistema
- `process_cpu_usage` - Uso de CPU del proceso
- `system_cpu_usage` - Uso de CPU del sistema
- `process_uptime_seconds` - Tiempo de actividad

---

## 🎨 Personalización

### Agregar métrica personalizada en Java
```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
public class MiServicio {
    private final Counter miMetrica;
    
    public MiServicio(MeterRegistry registry) {
        this.miMetrica = Counter.builder("mi.metrica.custom")
            .description("Descripción de mi métrica")
            .tag("tipo", "negocio")
            .register(registry);
    }
    
    public void miMetodo() {
        miMetrica.increment();
        // tu lógica
    }
}
```

### Crear dashboard personalizado en Grafana
1. Dashboards → New → New Dashboard
2. Add visualization
3. Selecciona datasource (Prometheus, Loki o Tempo)
4. Escribe tu query
5. Ajusta visualización
6. Save dashboard

---

## 📚 Recursos

- **Guía completa**: [MONITORING.md](./MONITORING.md)
- **Documentación Grafana**: https://grafana.com/docs/
- **Documentación Prometheus**: https://prometheus.io/docs/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

---

## ✅ Checklist

- [ ] Docker Compose levantado
- [ ] Kong configurado (`configure-kong.bat`)
- [ ] 4 servidores corriendo
- [ ] Actuator endpoints respondiendo (`/actuator/health`)
- [ ] Prometheus scrapeando (targets en UP)
- [ ] Grafana accesible con login
- [ ] Dashboard "Chat Federado" visible
- [ ] Logs aparecen en Loki (Explore → Loki)
- [ ] Métricas aparecen en Prometheus
- [ ] Trazas aparecen en Tempo (después de hacer requests)

---

**¡Monitoreo completo configurado! 🎉**
