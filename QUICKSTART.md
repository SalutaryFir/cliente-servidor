# Chat Server - Guía de Inicio Rápido

## 🚀 Iniciar todo el sistema

### Paso 1: Levantar API Gateway y Web Admin

```bash
# Desde la raíz del proyecto
docker-compose -f docker-compose-api-gateway.yml up -d

# Esperar a que Kong esté listo (30 segundos aprox)
```

### Paso 2: Configurar rutas de Kong

**En Windows:**
```bash
configure-kong.bat
```

**En Linux/Mac:**
```bash
chmod +x configure-kong.sh
./configure-kong.sh
```

### Paso 3: Iniciar los 4 servidores Java

Opción A - Con el script de inicialización:
```bash
Inicializador.bat
```

Opción B - Manualmente (cada uno en una terminal diferente):
```bash
# Terminal 1 - Servidor Coordinador
cd servidor
mvn spring-boot:run -Dspring-boot.run.profiles=server1

# Terminal 2
cd servidor
mvn spring-boot:run -Dspring-boot.run.profiles=server2

# Terminal 3
cd servidor
mvn spring-boot:run -Dspring-boot.run.profiles=server3

# Terminal 4
cd servidor
mvn spring-boot:run -Dspring-boot.run.profiles=server4
```

### Paso 4: Acceder a la aplicación web

Abre tu navegador en: **http://localhost:3000**

## 📊 URLs importantes

- **Web Admin**: http://localhost:3000
- **Kong Admin API**: http://localhost:8001
- **Kong Proxy (API Gateway)**: http://localhost:8000
- **Servidor 1 (Coordinador)**: http://localhost:8080
- **Servidor 2**: http://localhost:8081
- **Servidor 3**: http://localhost:8082
- **Servidor 4**: http://localhost:8083

## 🔍 Verificar el sistema

### Verificar Kong
```bash
curl http://localhost:8001/services
```

### Verificar servidores a través del Gateway
```bash
curl http://localhost:8000/server1/api/v1/server/health
curl http://localhost:8000/server2/api/v1/server/health
curl http://localhost:8000/server3/api/v1/server/health
curl http://localhost:8000/server4/api/v1/server/health
```

### Verificar Web Admin
```bash
curl http://localhost:3000
```

## 🛠️ Comandos útiles

### Ver logs de Docker
```bash
docker-compose -f docker-compose-api-gateway.yml logs -f
```

### Reiniciar un servicio
```bash
docker-compose -f docker-compose-api-gateway.yml restart web-admin
docker-compose -f docker-compose-api-gateway.yml restart kong
```

### Detener todo
```bash
docker-compose -f docker-compose-api-gateway.yml down
```

### Limpiar todo (incluyendo volúmenes)
```bash
docker-compose -f docker-compose-api-gateway.yml down -v
```

## 🐛 Troubleshooting

### La web no carga
1. Verificar que el contenedor esté corriendo:
   ```bash
   docker ps | grep web-admin
   ```
2. Ver logs:
   ```bash
   docker logs web-admin
   ```

### Error de conexión a servidores
1. Verificar que los 4 servidores Java estén corriendo
2. Verificar las rutas de Kong:
   ```bash
   curl http://localhost:8001/routes
   ```
3. Re-ejecutar configure-kong.bat

### Kong no responde
1. Esperar más tiempo (puede tardar en inicializar)
2. Verificar logs:
   ```bash
   docker logs kong
   ```
3. Reiniciar Kong:
   ```bash
   docker-compose -f docker-compose-api-gateway.yml restart kong
   ```

## 📋 Orden de inicio recomendado

1. ✅ Docker Compose (Kong + PostgreSQL + Web Admin)
2. ✅ Configurar rutas de Kong (script)
3. ✅ Servidores Java (Inicializador.bat)
4. ✅ Abrir navegador en http://localhost:3000

## 🎯 Características de la Web Admin

- ✅ Selección de servidor mediante tabs
- ✅ Dashboard en tiempo real con auto-refresh (5 segundos)
- ✅ Información del servidor (IP, puertos, uptime)
- ✅ Lista de usuarios conectados/desconectados
- ✅ Canales activos con conteo de miembros
- ✅ Logs del sistema con filtrado por nivel
- ✅ Estadísticas (mensajes, usuarios, memoria, conexiones)
- ✅ Estado de federación (servidores conectados, usuarios remotos)
- ✅ Diseño responsive (móvil, tablet, desktop)

## 📱 Desarrollo de la Web Admin

Si quieres desarrollar/modificar la web admin:

```bash
cd web-admin
npm install
npm run dev
```

Esto iniciará el servidor de desarrollo en http://localhost:5173 con hot-reload.

## 🔄 Actualizar la Web Admin en Docker

Después de hacer cambios:

```bash
cd web-admin
docker build -t web-admin:latest .
docker-compose -f ../docker-compose-api-gateway.yml up -d web-admin
```

## 📄 Estructura del proyecto

```
cliente-servidor/
├── servidor/                    # Servidores Java Spring Boot
├── cliente/                     # Cliente Java
├── comun/                       # DTOs y clases comunes
├── web-admin/                   # Aplicación web React
│   ├── src/                     # Código fuente React
│   ├── Dockerfile               # Build multi-stage
│   ├── nginx.conf               # Configuración Nginx
│   └── package.json             # Dependencias npm
├── docker-compose-api-gateway.yml  # Orquestación Docker
├── configure-kong.bat/sh        # Scripts de configuración Kong
├── Inicializador.bat            # Script para iniciar servidores Java
└── QUICKSTART.md               # Esta guía
```

## ✅ Checklist de inicio

- [ ] Docker Desktop está corriendo
- [ ] Java 17+ instalado
- [ ] Maven instalado
- [ ] Ejecutar `docker-compose up -d`
- [ ] Ejecutar script de configuración Kong
- [ ] Iniciar los 4 servidores Java
- [ ] Abrir http://localhost:3000
- [ ] Seleccionar servidor en tabs
- [ ] Ver datos en tiempo real

¡Todo listo! 🎉
