# 📊 Guía de Observabilidad - Chat Federado

Este documento explica cómo usar el stack de observabilidad integrado con Grafana, Prometheus, Loki y Tempo para monitorear el sistema de chat federado.

## 🎯 Componentes del Stack

### 1. **Prometheus** - Recolección de Métricas
- **Puerto**: `9090`
- **URL**: http://localhost:9090
- **Función**: Recolecta y almacena métricas de tiempo series de los 4 servidores de chat

### 2. **Grafana** - Visualización
- **Puerto**: `3001`
- **URL**: http://localhost:3001
- **Credenciales por defecto**:
  - Usuario: `admin`
  - Contraseña: `admin`
- **Función**: Dashboards interactivos para visualizar métricas, logs y trazas

### 3. **Loki** - Agregación de Logs
- **Puerto**: `3100`
- **URL**: http://localhost:3100
- **Función**: Almacena y consulta logs de todos los servicios

### 4. **Promtail** - Recolector de Logs
- **Función**: Recolecta logs de contenedores Docker y los envía a Loki

### 5. **Tempo** - Trazabilidad Distribuida
- **Puerto**: `3200`
- **URL**: http://localhost:3200
- **Puertos adicionales**:
  - `4317`: OTLP gRPC (OpenTelemetry)
  - `4318`: OTLP HTTP
  - `9411`: Zipkin (usado por Spring Boot)
- **Función**: Trazas distribuidas para seguir requests a través de múltiples servicios

---

## 🚀 Inicio Rápido

### Paso 1: Levantar el Stack de Monitoreo

```bash
# Desde el directorio raíz del proyecto
docker-compose -f docker-compose-api-gateway.yml up -d
```

Esto iniciará:
- PostgreSQL (base de datos de Kong)
- Kong API Gateway
- Web Admin (frontend)
- Prometheus
- Grafana
- Loki
- Promtail
- Tempo

### Paso 2: Iniciar los Servidores de Chat

```bash
# Windows
IniciarServidores.bat

# Linux/Mac
./IniciarServidores.sh
```

Esto compilará e iniciará los 4 servidores en los puertos:
- Server 1: `8080` (API), `5000` (Cliente), `5001` (Federación)
- Server 2: `8081` (API), `5002` (Cliente), `5003` (Federación)
- Server 3: `8082` (API), `5004` (Cliente), `5005` (Federación)
- Server 4: `8083` (API), `5006` (Cliente), `5007` (Federación)

### Paso 3: Verificar que los Servidores Exponen Métricas

Visita los siguientes endpoints para confirmar que Actuator está funcionando:

- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/prometheus
- http://localhost:8081/actuator/prometheus
- http://localhost:8082/actuator/prometheus
- http://localhost:8083/actuator/prometheus

Deberías ver métricas en formato Prometheus.

---

## 📈 Acceso a los Servicios de Monitoreo

### Grafana - Dashboard Principal

1. Abre tu navegador en: **http://localhost:3001**
2. Inicia sesión con:
   - Usuario: `admin`
   - Contraseña: `admin`
3. (Opcional) Cambia la contraseña cuando se te solicite

#### Datasources Pre-configurados

Grafana viene con 3 datasources ya configurados:

- **Prometheus**: Métricas de los servidores
- **Loki**: Logs de contenedores y aplicaciones
- **Tempo**: Trazas distribuidas

#### Dashboard Pre-cargado

Navega a **Dashboards → Chat Federado** para ver:

- **Servidores Activos**: Conteo de servidores en línea
- **Uso de CPU por Servidor**: Gráfico de líneas de tiempo
- **Conexiones Activas Totales**: Clientes conectados
- **Uso de Memoria Heap**: Consumo de memoria JVM
- **Rate de Requests HTTP**: Tráfico de API REST
- **Latencia Promedio**: Tiempo de respuesta de endpoints
- **Distribución de Códigos de Respuesta**: Gráfico de pastel de HTTP status codes

---

### Prometheus - Explorador de Métricas

1. Abre: **http://localhost:9090**
2. Ve a **Graph** o **Status → Targets**

#### Métricas Clave Disponibles

```promql
# Estado de los servidores
up{job=~"chat-server-.*"}

# Uso de CPU
process_cpu_usage{job=~"chat-server-.*"}

# Memoria Heap
jvm_memory_used_bytes{job=~"chat-server-.*",area="heap"}

# Rate de requests HTTP
rate(http_server_requests_seconds_count{job=~"chat-server-.*"}[5m])

# Latencia de requests
rate(http_server_requests_seconds_sum{job=~"chat-server-.*"}[5m]) 
/ rate(http_server_requests_seconds_count{job=~"chat-server-.*"}[5m])

# Conexiones activas
http_server_requests_active_count{job=~"chat-server-.*"}

# Códigos de respuesta HTTP
http_server_requests_seconds_count{job=~"chat-server-.*",status=~"2.*"}
http_server_requests_seconds_count{job=~"chat-server-.*",status=~"4.*"}
http_server_requests_seconds_count{job=~"chat-server-.*",status=~"5.*"}
```

#### Ejemplo de Consultas

**Ver todos los servidores activos:**
```promql
count(up{job=~"chat-server-.*"} == 1)
```

**CPU promedio de todos los servidores:**
```promql
avg(process_cpu_usage{job=~"chat-server-.*"}) * 100
```

**Memoria total usada:**
```promql
sum(jvm_memory_used_bytes{job=~"chat-server-.*",area="heap"})
```

---

### Loki - Búsqueda de Logs

#### Opción 1: Desde Grafana (Recomendado)

1. En Grafana, ve a **Explore** (icono de brújula)
2. Selecciona **Loki** como datasource
3. Usa LogQL para consultar logs

**Ejemplos de consultas LogQL:**

```logql
# Ver logs de todos los servidores de chat
{container=~"chat-server.*"}

# Logs con errores
{container=~"chat-server.*"} |= "ERROR"

# Logs de un servidor específico
{service="servidor"} | json

# Logs del API Gateway Kong
{container="kong"}

# Logs de web-admin
{container="web-admin"}

# Filtrar por nivel de log
{container=~"chat-server.*"} | json | level="ERROR"
```

#### Opción 2: API Directa de Loki

Consulta logs usando curl:

```bash
# Últimos 100 logs
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={container=~"chat-server.*"}' \
  --data-urlencode 'limit=100'
```

---

### Tempo - Trazas Distribuidas

#### Visualización desde Grafana

1. Ve a **Explore** en Grafana
2. Selecciona **Tempo** como datasource
3. Busca trazas por:
   - **Trace ID**: ID único de la traza
   - **Service Name**: Nombre del servicio
   - **Operation Name**: Operación específica

#### Correlación Logs → Trazas → Métricas

Grafana está configurado para correlacionar automáticamente:

- **Desde Logs**: Si un log contiene `traceID=<id>`, puedes hacer clic para ver la traza completa
- **Desde Trazas**: Puedes saltar a los logs relacionados
- **Desde Métricas**: Navega a trazas para debugging profundo

---

## 🔧 Configuración Avanzada

### Ajustar Retención de Datos

#### Prometheus (en `monitoring/prometheus.yml`)

```yaml
global:
  scrape_interval: 15s # Frecuencia de scraping

# En comando de docker-compose:
# --storage.tsdb.retention.time=15d
# --storage.tsdb.retention.size=10GB
```

#### Loki (en `monitoring/loki.yml`)

```yaml
limits_config:
  retention_period: 168h # 7 días
```

#### Tempo (en `monitoring/tempo.yml`)

```yaml
compactor:
  compaction:
    block_retention: 168h # 7 días
```

---

### Configurar Alertas en Grafana

1. Ve a **Alerting → Alert rules** en Grafana
2. Crea una nueva regla, por ejemplo:

**Alerta: Servidor Caído**

```promql
up{job=~"chat-server-.*"} == 0
```

Condiciones:
- Cuando el valor es `0` durante `1 minuto`
- Severidad: `Critical`
- Notificaciones: Email, Slack, etc.

**Alerta: Alta Latencia**

```promql
rate(http_server_requests_seconds_sum[5m]) 
/ rate(http_server_requests_seconds_count[5m]) > 1
```

Condiciones:
- Cuando la latencia promedio supera `1 segundo`
- Severidad: `Warning`

**Alerta: Memoria Alta**

```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.8
```

Condiciones:
- Cuando el uso de heap supera el `80%`
- Severidad: `Warning`

---

## 📊 Métricas Personalizadas en Spring Boot

### Agregar Métricas Personalizadas

En tu código Java, puedes agregar métricas personalizadas usando Micrometer:

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

@Service
public class MensajeService {
    
    private final Counter mensajesEnviadosCounter;
    private final Timer mensajeLatencyTimer;
    
    public MensajeService(MeterRegistry registry) {
        this.mensajesEnviadosCounter = Counter.builder("chat.mensajes.enviados")
            .description("Total de mensajes enviados")
            .tag("tipo", "chat")
            .register(registry);
            
        this.mensajeLatencyTimer = Timer.builder("chat.mensaje.latency")
            .description("Latencia de envío de mensajes")
            .register(registry);
    }
    
    public void enviarMensaje(Mensaje mensaje) {
        mensajeLatencyTimer.record(() -> {
            // Lógica de envío
            mensajesEnviadosCounter.increment();
        });
    }
}
```

Luego podrás consultar en Prometheus:

```promql
# Total de mensajes enviados
chat_mensajes_enviados_total

# Latencia de envío
chat_mensaje_latency_seconds
```

---

## 🛠️ Troubleshooting

### Problema: Los servidores no aparecen en Prometheus

**Diagnóstico:**

1. Verifica que los servidores estén corriendo:
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   curl http://localhost:8083/actuator/health
   ```

2. Verifica que Prometheus pueda alcanzar los servidores:
   - Ve a http://localhost:9090/targets
   - Busca `chat-server-1`, `chat-server-2`, etc.
   - Estado debe ser **UP** (verde)

**Solución:**

- Si los servidores están en `host.docker.internal` y no funcionan (Linux), cambia en `monitoring/prometheus.yml`:
  ```yaml
  - targets: ['172.17.0.1:8080']  # IP del host Docker en Linux
  ```

- En Windows/Mac, `host.docker.internal` debería funcionar correctamente

---

### Problema: No veo logs en Loki

**Diagnóstico:**

1. Verifica que Promtail esté corriendo:
   ```bash
   docker ps | grep promtail
   ```

2. Verifica los logs de Promtail:
   ```bash
   docker logs promtail
   ```

**Solución:**

- Asegúrate de que Promtail tenga acceso a `/var/run/docker.sock` (puede requerir permisos en Linux)

---

### Problema: No aparecen trazas en Tempo

**Diagnóstico:**

1. Verifica que Tempo esté escuchando en el puerto Zipkin:
   ```bash
   curl http://localhost:9411/api/v2/spans
   ```

2. Verifica que los servidores estén configurados para enviar trazas:
   ```bash
   grep "management.zipkin.tracing.endpoint" servidor/src/main/resources/application.properties
   ```

**Solución:**

- Si los servidores están en otra máquina, actualiza en `application.properties`:
  ```properties
  management.zipkin.tracing.endpoint=http://<IP_DOCKER_HOST>:9411/api/v2/spans
  ```

---

## 🧹 Limpieza y Mantenimiento

### Detener todos los servicios

```bash
# Detener Docker
docker-compose -f docker-compose-api-gateway.yml down

# Detener servidores de chat (Ctrl+C en cada ventana)
```

### Limpiar datos de monitoreo

```bash
# Eliminar volúmenes de Docker (CUIDADO: Borra todos los datos)
docker-compose -f docker-compose-api-gateway.yml down -v

# Esto eliminará:
# - Datos de Prometheus
# - Datos de Grafana (dashboards personalizados)
# - Datos de Loki (logs)
# - Datos de Tempo (trazas)
```

### Ver uso de disco

```bash
docker system df -v
```

---

## 📚 Recursos Adicionales

- **Documentación de Prometheus**: https://prometheus.io/docs/
- **Documentación de Grafana**: https://grafana.com/docs/
- **Documentación de Loki**: https://grafana.com/docs/loki/
- **Documentación de Tempo**: https://grafana.com/docs/tempo/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **Micrometer**: https://micrometer.io/docs

---

## 🎓 Casos de Uso Prácticos

### Caso 1: Debugging de Alta Latencia

1. Ve al dashboard de Grafana
2. Identifica el servidor con alta latencia en el gráfico "Latencia Promedio"
3. Ve a **Explore → Loki**
4. Busca logs de ese servidor: `{container="chat-server-X"} |= "ERROR"`
5. Si hay un `traceID` en el log, haz clic para ver la traza completa en Tempo
6. Analiza qué operación está tardando más tiempo

### Caso 2: Monitoreo de Carga

1. Ve a Prometheus: http://localhost:9090
2. Consulta: `rate(http_server_requests_seconds_count{job=~"chat-server-.*"}[5m])`
3. Identifica el servidor con más tráfico
4. Considera rebalancear la carga o escalar ese servidor

### Caso 3: Análisis Post-Incidente

1. Ve a Grafana y selecciona el rango de tiempo del incidente
2. Revisa los gráficos de CPU, memoria y latencia
3. Ve a Loki y busca errores en ese período
4. Correlaciona con trazas en Tempo para identificar la causa raíz

---

## ✅ Checklist de Verificación

- [ ] Docker Compose levantado correctamente
- [ ] 4 servidores de chat corriendo
- [ ] Prometheus scrapeando métricas (http://localhost:9090/targets)
- [ ] Grafana accesible (http://localhost:3001)
- [ ] Dashboard "Chat Federado" visible en Grafana
- [ ] Logs aparecen en Loki
- [ ] Trazas aparecen en Tempo (después de hacer algunas peticiones HTTP)

---

**¡Tu sistema de chat federado ahora tiene observabilidad completa!** 🎉
