# 🎓 Sistema de Chat Universitario Federado

Sistema de mensajería instantánea con arquitectura Cliente-Servidor Federado, desarrollado en Java con Spring Boot.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Requisitos](#-requisitos)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Uso](#-uso)
- [Monitoreo y Observabilidad](#-monitoreo-y-observabilidad)
- [Despliegue](#-despliegue)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Tecnologías](#-tecnologías)

---

## ✨ Características

### Funcionalidades de Chat
- ✅ **Mensajería en tiempo real** entre clientes
- ✅ **Canales de chat** temáticos
- ✅ **Mensajes privados** entre usuarios
- ✅ **Registro de mensajes** con persistencia en base de datos
- ✅ **Reconocimiento de voz** con Vosk (transcripción de audio)

### Arquitectura Federada
- ✅ **4 servidores federados** que se comunican entre sí
- ✅ **Conexión dinámica** de clientes a cualquier servidor
- ✅ **Sincronización automática** de mensajes entre servidores
- ✅ **Alta disponibilidad** - Los usuarios pueden cambiar de servidor sin perder acceso

### API REST
- ✅ **Endpoints REST** para administración y monitoreo
- ✅ **API Gateway** con Kong para enrutamiento centralizado
- ✅ **Documentación Swagger** en `/swagger-ui.html`

### Interfaz Web Administrativa
- ✅ **Dashboard React** con visualización en tiempo real
- ✅ **Monitoreo de servidores** (estado, conexiones, canales)
- ✅ **Estadísticas de uso** con gráficos interactivos
- ✅ **Vista de federación** entre servidores

### Observabilidad Completa
- ✅ **Grafana** - Dashboards interactivos
- ✅ **Prometheus** - Métricas de rendimiento
- ✅ **Loki** - Agregación de logs
- ✅ **Tempo** - Trazabilidad distribuida

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                        CAPA DE USUARIOS                         │
│  Cliente GUI (Swing)  │  Cliente GUI  │  Web Admin (React)     │
└─────────────┬──────────────────┬────────────────────┬───────────┘
              │                   │                    │
              │ TCP (5000-5006)   │                    │ HTTP
              │                   │                    │
┌─────────────▼───────────────────▼────────────────────▼───────────┐
│                       API GATEWAY (Kong)                         │
│                        Puerto 8000/8001                          │
└─────────────┬──────────────────────────────────────────┬─────────┘
              │                                          │
    ┌─────────▼─────────┐                    ┌───────────▼─────────┐
    │  REST API (8080)  │                    │   Observabilidad    │
    │   Server 1        │                    │  - Grafana (3001)   │
    │   MySQL           │◄─────Federación───►│  - Prometheus (9090)│
    │   TCP: 5000/5001  │      (S2S)         │  - Loki (3100)      │
    └───────────────────┘                    │  - Tempo (3200)     │
            │                                 └─────────────────────┘
    ┌───────┼─────────┐
    │       │         │
┌───▼───┐ ┌─▼─────┐ ┌──▼────┐
│Server2│ │Server3│ │Server4│
│ 8081  │ │ 8082  │ │ 8083  │
│ 5002/3│ │ 5004/5│ │ 5006/7│
└───────┘ └───────┘ └───────┘
```

### Componentes

1. **Módulo Común (`comun/`)**: DTOs, modelos y utilidades compartidas
2. **Servidor (`servidor/`)**: 4 instancias independientes con:
   - Servidor TCP para clientes
   - API REST para administración
   - Base de datos MySQL independiente
   - Puerto de federación para comunicación S2S
3. **Cliente (`cliente/`)**: Aplicación GUI Java Swing
4. **Web Admin (`web-admin/`)**: Dashboard React con Vite
5. **API Gateway**: Kong con PostgreSQL
6. **Stack de Monitoreo**: Prometheus, Grafana, Loki, Tempo

---

## 🔧 Requisitos

### Software Necesario
- **Java 17+** (JDK)
- **Maven 3.8+**
- **MySQL 8.0+**
- **Docker** y **Docker Compose**
- **Node.js 18+** (opcional, para desarrollo del frontend)

### Puertos Utilizados

| Servicio | Puerto(s) | Descripción |
|----------|-----------|-------------|
| Server 1 API | 8080 | REST API |
| Server 1 Cliente | 5000 | Conexión TCP clientes |
| Server 1 Federación | 5001 | Comunicación S2S |
| Server 2 API | 8081 | REST API |
| Server 2 Cliente | 5002 | Conexión TCP clientes |
| Server 2 Federación | 5003 | Comunicación S2S |
| Server 3 API | 8082 | REST API |
| Server 3 Cliente | 5004 | Conexión TCP clientes |
| Server 3 Federación | 5005 | Comunicación S2S |
| Server 4 API | 8083 | REST API |
| Server 4 Cliente | 5006 | Conexión TCP clientes |
| Server 4 Federación | 5007 | Comunicación S2S |
| Kong Gateway | 8000/8001 | Proxy/Admin |
| Web Admin | 3000 | Dashboard React |
| Prometheus | 9090 | Métricas |
| Grafana | 3001 | Visualización |
| Loki | 3100 | Logs |
| Tempo | 3200 | Trazas |

---

## 📥 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd cliente-servidor
```

### 2. Configurar Bases de Datos MySQL

Crea las 4 bases de datos:

```sql
CREATE DATABASE chat_db_server1;
CREATE DATABASE chat_db_server2;
CREATE DATABASE chat_db_server3;
CREATE DATABASE chat_db_server4;
```

**Nota**: Si usas `createDatabaseIfNotExist=true` en la URL de conexión, Spring creará las bases automáticamente.

### 3. Configurar Credenciales de MySQL

Edita los archivos `application-server1.properties` hasta `application-server4.properties` en `servidor/src/main/resources/`:

```properties
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA
```

### 4. Compilar el Proyecto

```bash
# Compilar todos los módulos
mvn clean install -DskipTests
```

---

## 🚀 Uso

### Opción 1: Uso Rápido (Recomendado)

#### Iniciar Servidores

```bash
# Windows
IniciarServidores.bat

# Linux/Mac
./IniciarServidores.sh
```

Esto iniciará los 4 servidores automáticamente.

#### Iniciar Clientes

```bash
# Windows
IniciarClientes.bat

# Linux/Mac
./IniciarClientes.sh
```

Se te pedirá que ingreses la IP y puerto del servidor al que deseas conectarte.

#### Iniciar Web Admin y API Gateway

```bash
docker-compose -f docker-compose-api-gateway.yml up -d
```

Luego configura las rutas de Kong:

```bash
# Windows
configure-kong.bat

# Linux/Mac
./configure-kong.sh
```

Accede al dashboard en: **http://localhost:3000**

---

### Opción 2: Inicio Manual

#### Servidor Individual

```bash
cd servidor
mvn spring-boot:run -Dspring-boot.run.profiles=server1
```

Perfiles disponibles: `server1`, `server2`, `server3`, `server4`

#### Cliente Individual

```bash
cd cliente
mvn spring-boot:run
```

---

## 📊 Monitoreo y Observabilidad

El sistema incluye un stack completo de observabilidad con Grafana, Prometheus, Loki y Tempo.

### Acceso Rápido

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Grafana | http://localhost:3001 | admin / admin |
| Prometheus | http://localhost:9090 | N/A |
| Loki | http://localhost:3100 | N/A |
| Tempo | http://localhost:3200 | N/A |

### Características

- ✅ **Dashboard pre-configurado** con métricas clave
- ✅ **Métricas de rendimiento** (CPU, memoria, latencia)
- ✅ **Logs centralizados** de todos los servicios
- ✅ **Trazas distribuidas** para debugging
- ✅ **Correlación automática** entre logs, métricas y trazas

### Guía Completa

Lee la guía detallada en: **[MONITORING.md](./MONITORING.md)**

---

## 🌍 Despliegue

El sistema puede desplegarse en:

1. **Una sola máquina** (desarrollo/pruebas)
2. **Múltiples máquinas** en la misma red local
3. **Nube** (AWS, Azure, GCP)

### Despliegue Multi-Computador

Lee la guía de despliegue en: **[DEPLOYMENT.md](./DEPLOYMENT.md)**

**Pasos rápidos**:

1. Configura las IPs en los archivos `application-serverX.properties`
2. Configura el archivo `.env` para Kong
3. Levanta los servicios Docker
4. Inicia los servidores en cada máquina
5. Los clientes pueden conectarse desde cualquier máquina

---

## 📁 Estructura del Proyecto

```
cliente-servidor/
├── cliente/                      # Aplicación cliente GUI (Swing)
│   ├── src/main/java/           # Código fuente del cliente
│   └── pom.xml
├── servidor/                     # Aplicación servidor
│   ├── src/main/java/
│   │   └── com/universidad/chat/servidor/
│   │       ├── controller/       # API REST Controllers
│   │       ├── model/            # Entidades JPA
│   │       ├── repository/       # Repositorios JPA
│   │       ├── service/          # Lógica de negocio
│   │       ├── network/          # TCP Server y Federación
│   │       └── vista/            # GUI del servidor (opcional)
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-server[1-4].properties
│   └── pom.xml
├── comun/                        # Módulo compartido
│   ├── src/main/java/
│   │   └── com/universidad/chat/comun/
│   │       ├── dto/              # Data Transfer Objects
│   │       └── modelo/           # Modelos de dominio
│   └── pom.xml
├── web-admin/                    # Dashboard React
│   ├── src/
│   │   ├── components/           # Componentes React
│   │   └── services/             # API service layer
│   ├── Dockerfile
│   └── package.json
├── monitoring/                   # Configuraciones de monitoreo
│   ├── prometheus.yml            # Config Prometheus
│   ├── loki.yml                  # Config Loki
│   ├── tempo.yml                 # Config Tempo
│   ├── promtail.yml              # Config Promtail
│   └── grafana/
│       ├── provisioning/         # Datasources automáticos
│       └── dashboards/           # Dashboards pre-cargados
├── docker-compose-api-gateway.yml # Orquestación Docker
├── IniciarServidores.bat         # Script para iniciar servidores
├── IniciarClientes.bat           # Script para iniciar clientes
├── configure-kong.bat            # Script para configurar Kong
├── pom.xml                       # POM padre
├── MONITORING.md                 # Guía de monitoreo
├── DEPLOYMENT.md                 # Guía de despliegue
└── README.md                     # Este archivo
```

---

## 🛠️ Tecnologías

### Backend
- **Java 17** - Lenguaje de programación
- **Spring Boot 3.x** - Framework principal
- **Spring Data JPA** - ORM para persistencia
- **MySQL** - Base de datos relacional
- **Hibernate** - Implementación JPA
- **Vosk** - Reconocimiento de voz
- **Lombok** - Reducción de código boilerplate
- **Springdoc OpenAPI** - Documentación Swagger

### Frontend
- **React 18.2** - Librería UI
- **Vite 5.0** - Build tool
- **Tailwind CSS 3.4** - Framework CSS
- **Axios** - Cliente HTTP
- **Recharts** - Librería de gráficos
- **Lucide React** - Iconos

### DevOps
- **Docker** - Containerización
- **Docker Compose** - Orquestación de contenedores
- **Kong 3.4** - API Gateway
- **Nginx** - Servidor web para frontend

### Observabilidad
- **Prometheus** - Métricas de time series
- **Grafana** - Visualización y dashboards
- **Loki** - Agregación de logs
- **Tempo** - Trazabilidad distribuida
- **Promtail** - Recolector de logs
- **Spring Boot Actuator** - Endpoints de métricas
- **Micrometer** - Instrumentación de métricas

---

## 📖 Guías de Referencia

- **[MONITORING.md](./MONITORING.md)** - Guía completa de observabilidad
- **[DEPLOYMENT.md](./DEPLOYMENT.md)** - Guía de despliegue multi-computador
- **[FEDERACION.md](./FEDERACION.md)** - Arquitectura de federación

---

## 📝 API REST Endpoints

### Server Info
- `GET /api/v1/server/info` - Información del servidor
- `GET /api/v1/server/health` - Estado de salud

### Users
- `GET /api/v1/server/users` - Lista de usuarios conectados

### Channels
- `GET /api/v1/server/channels` - Lista de canales

### Logs
- `GET /api/v1/server/logs?limit=100` - Últimos N logs

### Stats
- `GET /api/v1/server/stats` - Estadísticas de uso

### Federation
- `GET /api/v1/server/federation` - Estado de la federación

### Documentación Swagger
- http://localhost:8080/swagger-ui.html (Server 1)
- http://localhost:8081/swagger-ui.html (Server 2)
- http://localhost:8082/swagger-ui.html (Server 3)
- http://localhost:8083/swagger-ui.html (Server 4)

---

## 🤝 Contribuciones

Este es un proyecto académico desarrollado para el curso de Arquitectura de Software.

---

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## 🎓 Autores

Desarrollado por estudiantes de la Universidad como parte del proyecto de Arquitectura de Software.

---

## 🐛 Reportar Problemas

Si encuentras algún problema, por favor crea un issue en el repositorio.

---

## ✅ Checklist de Verificación Rápida

- [ ] Java 17+ instalado
- [ ] Maven 3.8+ instalado
- [ ] MySQL corriendo con las 4 bases de datos creadas
- [ ] Docker y Docker Compose instalados
- [ ] Puertos 5000-5007, 8000-8001, 8080-8083, 3000-3001, 9090 disponibles
- [ ] Credenciales de MySQL configuradas
- [ ] Proyecto compilado con `mvn clean install`
- [ ] Servidores iniciados
- [ ] Docker Compose levantado
- [ ] Kong configurado con `configure-kong.bat`
- [ ] Grafana accesible en http://localhost:3001
- [ ] Web Admin accesible en http://localhost:3000

---

**¡Listo para chatear! 🚀**
