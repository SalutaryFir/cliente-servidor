# 📊 OBSERVABILIDAD: GRAFANA, PROMETHEUS, LOKI, TEMPO

## 📚 Tabla de Contenidos
1. [Arquitectura de Observabilidad](#arquitectura-de-observabilidad)
2. [Prometheus - Métricas](#prometheus---métricas)
3. [Grafana - Visualización](#grafana---visualización)
4. [Loki - Logs](#loki---logs)
5. [Tempo - Trazas Distribuidas](#tempo---trazas-distribuidas)
6. [Configuración Completa](#configuración-completa)
7. [Dashboards](#dashboards)

---

## 🏛️ Arquitectura de Observabilidad

```
┌──────────────────────────────────────────────────────────────────────┐
│                    STACK DE OBSERVABILIDAD                           │
│                    (The Observability Stack)                         │
└──────────────────────────────────────────────────────────────────────┘

┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐
│  SERVIDORES     │  │  KONG GATEWAY   │  │   WEB ADMIN      │
│  (Spring Boot)  │  │   (Docker)      │  │   (React)        │
│                 │  │                 │  │                  │
│ - Actuator      │  │ - Logs Docker   │  │ - Console logs   │
│ - Prometheus    │  │ - Métricas      │  │ - Performance    │
│ - Zipkin        │  │                 │  │                  │
└────────┬────────┘  └────────┬────────┘  └──────────┬───────┘
         │                    │                      │
         │ (Exporta)          │ (Exporta)            │ (Envía)
         ▼                    ▼                      ▼
    ┌──────────────────────────────────┐
    │    PROMETHEUS (Puerto 9090)      │
    │  - Recolecta métricas            │
    │  - Almacena series temporales    │
    │  - Scrape interval: 15s          │
    └────────────┬─────────────────────┘
                 │
     ┌───────────┼────────────┬─────────────────┐
     │           │            │                 │
     ▼           ▼            ▼                 ▼
 ┌────────┐ ┌────────┐ ┌──────────┐ ┌──────────────┐
 │ LOKI   │ │ TEMPO  │ │ GRAFANA  │ │  ALERTAS     │
 │ Logs   │ │ Trazas │ │ Dashboard│ │ (opcional)   │
 └────────┘ └────────┘ └──────────┘ └──────────────┘
              │            │
              └────────┬───┘
                       │
                       ▼
            ┌──────────────────────┐
            │   USUARIO (TÚ)       │
            │ http://localhost:3001│
            │   (Grafana UI)       │
            └──────────────────────┘
```

---

## 📈 Prometheus - Métricas

### ¿Qué es Prometheus?

Prometheus es un **sistema de monitoreo** que recolecta métricas (números que cambien con el tiempo) de tus aplicaciones.

**Archivo de configuración:** `monitoring/prometheus.yml`

### Conceptos Clave

| Concepto | Definición | Ejemplo |
|----------|-----------|---------|
| **Job** | Conjunto de targets a monitorear | 'chat-server-1' |
| **Target** | Aplicación específica | localhost:8080 |
| **Metrics Path** | Endpoint donde están las métricas | /actuator/prometheus |
| **Scrape** | Lectura de métricas (cada 15s) | Recolecta datos |
| **Time Series** | Métrica con valor en el tiempo | cpu_usage[1,2,3,4,...] |

### Configuración

```yaml
# monitoring/prometheus.yml

global:
  scrape_interval: 15s          # Cada cuánto recolectar métricas
  evaluation_interval: 15s      # Cada cuánto evaluar reglas

scrape_configs:
  # Monitorear a Prometheus mismo
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Servidores de Chat (Spring Boot)
  - job_name: 'chat-server-1'
    metrics_path: '/actuator/prometheus'  # ⭐ Endpoint de métricas
    static_configs:
      - targets: ['host.docker.internal:8080']  # IP:Puerto
        labels:
          server: 'server1'
          type: 'chat-server'

  - job_name: 'chat-server-2'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8081']
        labels:
          server: 'server2'

  - job_name: 'chat-server-3'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8082']
        labels:
          server: 'server3'

  - job_name: 'chat-server-4'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8083']
        labels:
          server: 'server4'

  # Kong API Gateway
  - job_name: 'kong'
    static_configs:
      - targets: ['kong:8001']  # Admin API de Kong
        labels:
          service: 'kong-gateway'
```

### ¿Cómo Spring Boot Expone Métricas?

**Dependencias en pom.xml:**

```xml
<!-- Spring Boot Actuator: Endpoints de monitoreo -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus: Exporta métricas en formato Prometheus -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Configuración en application.properties:**

```properties
# Habilitar todos los endpoints de Actuator
management.endpoints.web.exposure.include=*

# Base path para endpoints
management.endpoints.web.base-path=/actuator

# Habilitar endpoint de Prometheus
management.endpoint.prometheus.enabled=true

# Mostrar detalles de health
management.endpoint.health.show-details=always

# Habilitar métricas HTTP
management.metrics.export.prometheus.enabled=true

# Tags para las métricas
management.metrics.tags.application=servidor
management.metrics.tags.server=${chat.server.name}
```

**Resultado:**

- Endpoint: `http://localhost:8080/actuator/prometheus`
- Formato: Texto con métricas tipo Prometheus

**Ejemplo de salida:**

```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 5.24288E8
jvm_memory_used_bytes{area="nonheap"} 4.128E7

# HELP http_requests_total Total HTTP Requests
# TYPE http_requests_total counter
http_requests_total{method="GET",status="200"} 125
http_requests_total{method="GET",status="404"} 3

# HELP spring_boot_application_name Application name from the properties file
# TYPE spring_boot_application_name gauge
spring_boot_application_name{name="servidor"} 1
```

### Métricas Típicas Recolectadas

```
Métricas de JVM:
- jvm_memory_used_bytes
- jvm_threads_live
- jvm_gc_memory_promoted_bytes

Métricas de HTTP:
- http_requests_total
- http_request_duration_seconds
- http_requests_pending

Métricas de Base de Datos:
- jdbc_connections_active
- jdbc_connections_max
```

---

## 📊 Grafana - Visualización

### ¿Qué es Grafana?

Grafana es una plataforma de **visualización** que conecta a Prometheus, Loki, Tempo y otras fuentes para crear dashboards interactivos.

### Configuración

**Archivo:** `docker-compose-api-gateway.yml`

```yaml
grafana:
  image: grafana/grafana:latest
  environment:
    - GF_SECURITY_ADMIN_USER=admin
    - GF_SECURITY_ADMIN_PASSWORD=admin
    - GF_USERS_ALLOW_SIGN_UP=false
    - GF_SERVER_ROOT_URL=http://localhost:3001
  volumes:
    # Provisionamiento automático de datasources
    - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
    # Dashboards predefinidos
    - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
  ports:
    - "3001:3000"  # Grafana UI
```

### Datasources Provisionados

**Archivo:** `monitoring/grafana/provisioning/datasources/datasources.yml`

```yaml
apiVersion: 1

datasources:
  # Prometheus - Métricas
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true  # Datasource por defecto
    editable: true
    jsonData:
      timeInterval: 15s  # Sincronizar con interval de Prometheus

  # Loki - Logs
  - name: Loki
    type: loki
    url: http://loki:3100
    editable: true
    jsonData:
      maxLines: 1000  # Máximo de líneas de log

  # Tempo - Trazas
  - name: Tempo
    type: tempo
    url: http://tempo:3200
    editable: true
    jsonData:
      tracesToLogs:
        datasourceUid: loki  # Correlacionar trazas con logs
      tracesToMetrics:
        datasourceUid: prometheus  # Correlacionar trazas con métricas
```

### Acceso a Grafana

1. **URL:** http://localhost:3001
2. **Usuario:** admin
3. **Contraseña:** admin

### Flujo de Uso

```
1. Ingresar a http://localhost:3001 con admin/admin
   
2. Grafana carga automáticamente los datasources:
   - Prometheus (métricas)
   - Loki (logs)
   - Tempo (trazas)

3. Crear dashboard:
   - Click en "+" → "Dashboard"
   - Agregar panel
   - Seleccionar datasource (Prometheus, Loki, etc.)
   - Configurar consulta (PromQL, LogQL, etc.)

4. Ver datos en tiempo real:
   - Grafana consulta Prometheus cada X segundos
   - Actualiza gráficos automáticamente
```

---

## 📋 Loki - Logs

### ¿Qué es Loki?

Loki es un sistema de **almacenamiento de logs** optimizado para grandes volúmenes. A diferencia de indexar todo el contenido, Loki indexa solo etiquetas (labels).

### Configuración

**Archivo:** `monitoring/loki.yml`

```yaml
server:
  http_listen_port: 3100

schema_config:
  configs:
    - from: 2020-10-24
      store: tsdb
      schema: v13

limits_config:
  retention_period: 168h     # Guardar logs 7 días
  ingestion_rate_mb: 10      # Max 10 MB/s ingestion
  per_stream_rate_limit: 5MB # Max 5 MB/s por stream
```

### Cómo Se Recolectan Logs

**Componente:** `Promtail` (Recolector de logs)

```yaml
# docker-compose-api-gateway.yml

promtail:
  image: grafana/promtail:latest
  volumes:
    - /var/log:/var/log:ro                          # Logs del sistema
    - /var/lib/docker/containers:/var/lib/docker/containers:ro  # Logs de Docker
  depends_on:
    - loki
```

### Logs en Spring Boot

Los logs se pueden enviar a Loki de dos formas:

**Opción 1: A través de Docker (Recomendado)**
- Docker Compose detecta logs del contenedor
- Promtail los recolecta
- Loki los almacena

**Opción 2: Desde la aplicación (Logback)**

Se requiere agregar en `pom.xml`:

```xml
<dependency>
  <groupId>com.grafana</groupId>
  <artifactId>loki-logback-appender</artifactId>
  <version>1.3.1</version>
</dependency>
```

---

## 🔍 Tempo - Trazas Distribuidas

### ¿Qué es Tempo?

Tempo es un backend de **trazas distribuidas** que permite seguir una petición a través de múltiples servicios.

**Ejemplo de Traza:**

```
Cliente solicita:  GET /api/v1/server/users
                   ↓ (TraceID: abc123)
Kong recibe
                   ↓ (Propaga TraceID)
Servidor 1 recibe
  ↓ Consulta BD
  ↓ Procesa datos
                   ↓ (TraceID persiste)
Retorna respuesta
                   ↓
Kong reenvía
                   ↓
Cliente recibe

→ Todo etiquetado con TraceID: abc123
→ Tempo correlaciona todo bajo ese ID
→ Grafana muestra la traza completa (desde cliente hasta BD)
```

### Configuración

**Archivo:** `monitoring/tempo.yml`

```yaml
server:
  http_listen_port: 3200

distributor:
  receivers:
    # OpenTelemetry Protocol (OTLP)
    otlp:
      protocols:
        http:
          endpoint: 0.0.0.0:4318  # OTLP HTTP
        grpc:
          endpoint: 0.0.0.0:4317  # OTLP gRPC
    
    # Zipkin (compatible con Spring Boot)
    zipkin:
      endpoint: 0.0.0.0:9411

storage:
  trace:
    backend: local
    local:
      path: /var/tempo/traces  # Almacena trazas localmente
```

### Cómo Spring Boot Envía Trazas

**Dependencias en pom.xml:**

```xml
<!-- Micrometer Tracing para distribuir trazas -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<!-- Cliente Zipkin para enviar trazas a Tempo -->
<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

**Configuración en application.properties:**

```properties
# Muestreo de trazas (1.0 = 100% de las trazas)
management.tracing.sampling.probability=1.0

# Endpoint de Zipkin/Tempo
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
```

---

## 🏗️ Configuración Completa (Docker Compose)

**Archivo:** `docker-compose-api-gateway.yml`

```yaml
version: '3.8'

services:
  # ============ PROMETHEUS ============
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.retention.time=15d'
    restart: always

  # ============ GRAFANA ============
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
    depends_on:
      - prometheus
      - loki
      - tempo
    restart: always

  # ============ LOKI ============
  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
    volumes:
      - ./monitoring/loki.yml:/etc/loki/local-config.yaml
      - loki_data:/loki
    command: -config.file=/etc/loki/local-config.yaml
    restart: always

  # ============ PROMTAIL (Recolector de Logs) ============
  promtail:
    image: grafana/promtail:latest
    volumes:
      - /var/log:/var/log:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
    command: -config.file=/etc/promtail/config.yml
    depends_on:
      - loki
    restart: always

  # ============ TEMPO (Trazas) ============
  tempo:
    image: grafana/tempo:latest
    ports:
      - "3200:3200"   # HTTP
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
      - "9411:9411"   # Zipkin
    volumes:
      - ./monitoring/tempo.yml:/etc/tempo.yaml
      - tempo_data:/var/tempo
    command: ["-config.file=/etc/tempo.yaml"]
    restart: always

volumes:
  prometheus_data:
  loki_data:
  tempo_data:
```

---

## 📈 Dashboards

### Dashboard Predefinido

**Archivo:** `monitoring/grafana/dashboards/chat-servers-dashboard.json`

Este dashboard contiene paneles para visualizar:

1. **CPU y Memoria de Servidores**
   - Gráfico: CPU usage (%)
   - Gráfico: Memory usage (MB)
   - Métrica: `process_cpu_usage{server="server1"}`

2. **Peticiones HTTP**
   - Gráfico: Request count (total)
   - Gráfico: Response time (ms)
   - Métrica: `http_requests_total{server="server1"}`

3. **Conexiones de Base de Datos**
   - Gráfico: Active connections
   - Métrica: `jdbc_connections_active{server="server1"}`

4. **Usuarios Conectados (Custom)**
   - Consulta a endpoint `/api/v1/server/stats`
   - Muestra: Total usuarios, usuarios conectados

5. **Logs en Tiempo Real**
   - Fuente: Loki
   - Consulta: `{server="server1"}`
   - Muestra últimos logs

6. **Trazas de Peticiones**
   - Fuente: Tempo
   - Correlaciona con Prometheus y Loki

### Cómo Crear un Panel Personalizado

1. **Ir a Grafana → Dashboard → New Panel**

2. **Query (Consulta) en Prometheus:**
   ```promql
   # Usuarios conectados
   http_requests_total{server="server1", status="200"}
   
   # Memoria usada (bytes)
   jvm_memory_used_bytes{server="server1", area="heap"}
   
   # Tasa de errores (%)
   (rate(http_requests_total{status=~"4..|5.."}[5m]) / 
    rate(http_requests_total[5m])) * 100
   ```

3. **Query en Loki (Logs):**
   ```logql
   {server="server1"} |= "ERROR"
   ```

4. **Query en Tempo (Trazas):**
   - Buscar por trace ID
   - Visualizar duración completa
   - Correlacionar con logs y métricas

---

## 🔄 Flujo Completo de Observabilidad

```
┌──────────────────────────────────────────────────────────────────┐
│ 1. Usuario hace petición desde Web Admin                         │
│    GET http://localhost:8000/server1/api/v1/server/users         │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ 2. Kong recibe y reenvía a Servidor 1                            │
│    - Kong registra métrica HTTP: http_requests_total++           │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ 3. Servidor 1 procesa                                            │
│    - Spring Boot inicia traza (TraceID: abc123)                  │
│    - Consulta BD: SELECT * FROM usuario                          │
│    - Log: "[INFO] Usuarios obtenidos: 15"                        │
│    - Métrica: http_request_duration_seconds{server="server1"}    │
│    - Retorna respuesta (200 OK)                                  │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ 4. Sistema de Observabilidad Recolecta                           │
│                                                                  │
│    Prometheus scrape (cada 15s):                                 │
│    - http_requests_total{status="200"} = 125                     │
│    - jvm_memory_used_bytes = 524,288,000                         │
│    - jdbc_connections_active = 2                                 │
│                                                                  │
│    Promtail recolecta logs de Docker:                            │
│    - timestamp: 2025-12-02T10:30:45.123Z                         │
│    - level: INFO                                                 │
│    - message: "[INFO] Usuarios obtenidos: 15"                    │
│    - server: server1                                             │
│                                                                  │
│    Spring Boot exporta traza a Tempo:                            │
│    - TraceID: abc123                                             │
│    - SpanID: span456                                             │
│    - Duration: 45ms                                              │
│    - Status: OK                                                  │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ 5. Almacenamiento                                                │
│    Prometheus: /prometheus/data/...                              │
│    Loki: /tmp/loki/chunks/...                                    │
│    Tempo: /var/tempo/traces/...                                  │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ 6. Visualización en Grafana (http://localhost:3001)              │
│                                                                  │
│    Panel 1 (Prometheus):                                         │
│    ╔════════════════════════════════════╗                        │
│    ║ Peticiones HTTP (últimas 24h)      ║                        │
│    ║      ╱╲                     ╱╲     ║                        │
│    ║     ╱  ╲__________________╱  ╲    ║                        │
│    ║    ╱                            ╲   ║                       │
│    ║   ╱                              ╲  ║                       │
│    ╚════════════════════════════════════╝                        │
│                                                                  │
│    Panel 2 (Loki):                                               │
│    ╔════════════════════════════════════╗                        │
│    ║ Logs Recientes                     ║                        │
│    ║ [INFO] Usuarios obtenidos: 15      ║                        │
│    ║ [INFO] Audios procesados: 3        ║                        │
│    ║ [ERROR] Conexión BD perdida        ║                        │
│    ╚════════════════════════════════════╝                        │
│                                                                  │
│    Panel 3 (Tempo):                                              │
│    ╔════════════════════════════════════╗                        │
│    ║ Trazas (TraceID: abc123)           ║                        │
│    ║ Kong → Servidor → BD = 45ms        ║                        │
│    ╚════════════════════════════════════╝                        │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📞 Resumen: URLs Importantes

| Servicio | URL | Puerto | Usuario | Contraseña |
|----------|-----|--------|---------|-----------|
| Grafana | http://localhost:3001 | 3001 | admin | admin |
| Prometheus | http://localhost:9090 | 9090 | - | - |
| Loki | http://localhost:3100 | 3100 | - | - |
| Tempo | http://localhost:3200 | 3200 | - | - |

---

## ✅ Beneficios

✅ **Métricas en Tiempo Real:** Ve el estado de tus servidores instantáneamente  
✅ **Análisis de Logs:** Busca y correlaciona eventos rápidamente  
✅ **Trazas Distribuidas:** Sigue una petición a través de todo el sistema  
✅ **Alertas:** Configura alertas automáticas ante anomalías  
✅ **Debugging:** Investiga problemas con datos históricos  

---

¡Ahora tienes observabilidad completa de tu sistema federado! 🎯📊
