# Chat Server Admin - Web Application

Aplicación web moderna para administración y monitoreo de servidores de chat federado.

## 🚀 Tecnologías

- **React 18** - Framework UI
- **Vite** - Build tool y dev server
- **Tailwind CSS** - Estilos utility-first
- **Axios** - Cliente HTTP
- **Lucide React** - Iconos
- **Recharts** - Gráficos (opcional)

## 📦 Instalación

### Desarrollo Local

```bash
# Instalar dependencias
npm install

# Copiar archivo de entorno
cp .env.example .env

# Iniciar servidor de desarrollo
npm run dev
```

La aplicación estará disponible en `http://localhost:3000`

### Docker

```bash
# Build de la imagen
docker build -t chat-admin-web .

# Ejecutar contenedor
docker run -p 80:80 chat-admin-web
```

### Docker Compose (con API Gateway)

```bash
# Desde la raíz del proyecto
docker-compose -f docker-compose-api-gateway.yml up -d
```

## ⚙️ Configuración

### Variables de Entorno

Crea un archivo `.env` basado en `.env.example`:

```env
# API Gateway URL
VITE_API_GATEWAY_URL=http://localhost:8000

# URLs directas de servidores (fallback)
VITE_SERVER1_URL=http://localhost:8080
VITE_SERVER2_URL=http://localhost:8081
VITE_SERVER3_URL=http://localhost:8082
VITE_SERVER4_URL=http://localhost:8083

# Intervalo de actualización (ms)
VITE_REFRESH_INTERVAL=5000
```

## 🎯 Características

### 📊 Dashboard Principal
- Selección de servidor mediante tabs
- Información en tiempo real
- Auto-actualización configurable

### 👥 Gestión de Usuarios
- Lista de usuarios conectados/desconectados
- Estado en tiempo real
- Información de contacto

### 📢 Canales
- Visualización de canales activos
- Información de miembros
- Estado de federación

### 📝 Logs del Sistema
- Filtrado por nivel (INFO, WARN, ERROR, DEBUG)
- Visualización en tiempo real
- Limpieza de logs

### 📈 Estadísticas
- Mensajes procesados
- Usuarios registrados
- Uso de memoria
- Conexiones activas

### 🌐 Estado de Federación
- Servidores conectados
- Usuarios remotos
- Topología de red

## 🏗️ Estructura del Proyecto

```
web-admin/
├── public/              # Archivos estáticos
├── src/
│   ├── components/      # Componentes React
│   │   ├── dashboard/   # Componentes del dashboard
│   │   ├── Header.jsx
│   │   ├── ServerTabs.jsx
│   │   └── ...
│   ├── services/        # Servicios API
│   │   └── api.js
│   ├── utils/          # Utilidades
│   │   └── helpers.js
│   ├── App.jsx         # Componente principal
│   ├── main.jsx        # Entry point
│   └── index.css       # Estilos globales
├── Dockerfile          # Configuración Docker
├── nginx.conf          # Configuración Nginx
├── package.json
└── vite.config.js
```

## 🎨 Componentes Principales

### Dashboard
Componente principal que orquesta todas las secciones:
- ServerInfo
- UsersSection
- ChannelsSection
- LogsSection
- StatsSection
- FederationSection

### ServerTabs
Selector de servidores con tabs interactivos.

### Header
Encabezado con logo y estado del sistema.

## 📱 Responsive Design

La aplicación está completamente optimizada para:
- 📱 Móviles (< 640px)
- 📱 Tablets (640px - 1024px)
- 💻 Desktop (> 1024px)

## 🔄 Auto-actualización

Los datos se actualizan automáticamente cada 5 segundos (configurable).
Puedes desactivar la actualización automática desde el toggle en el dashboard.

## 🐳 Despliegue en Docker

### Build multi-stage

El Dockerfile utiliza build multi-stage para optimizar el tamaño final:

1. **Builder**: Compila la aplicación con Node.js
2. **Production**: Sirve con Nginx (imagen final ~25MB)

### Nginx

Configurado con:
- Proxy reverso al API Gateway
- Compresión Gzip
- Headers de seguridad
- Cache de assets estáticos
- SPA routing

## 📊 API Gateway

La aplicación consume las APIs a través de Kong API Gateway:

```
Web App → Kong (8000) → Servidor 1 (8080)
                      → Servidor 2 (8081)
                      → Servidor 3 (8082)
                      → Servidor 4 (8083)
```

### Rutas del Gateway

- `/server1/*` → Servidor 1
- `/server2/*` → Servidor 2
- `/server3/*` → Servidor 3
- `/server4/*` → Servidor 4

## 🔧 Scripts Disponibles

```bash
# Desarrollo
npm run dev

# Build para producción
npm run build

# Preview del build
npm run preview

# Lint
npm run lint
```

## 🎯 Próximos Pasos

1. Iniciar los 4 servidores Java
2. Configurar y levantar Kong API Gateway
3. Build y deploy de la web app
4. Acceder al dashboard

## 📝 Notas

- La aplicación requiere que el API Gateway esté funcionando
- Los servidores deben exponer sus APIs REST
- Se recomienda HTTPS en producción

## 🐛 Troubleshooting

### Error de conexión al API Gateway

Verifica que Kong esté corriendo:
```bash
docker ps | grep kong
```

### Datos no se actualizan

1. Verifica que los servidores Java estén activos
2. Revisa las rutas configuradas en Kong
3. Verifica la consola del navegador para errores

## 📄 Licencia

MIT License - © 2025
