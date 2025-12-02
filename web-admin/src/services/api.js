import axios from 'axios';

const API_GATEWAY_URL = 'http://192.168.137.253:8000';

// Configuración de servidores
export const SERVERS = [
  {
    id: 'server1',
    name: 'Servidor 1',
    route: '/server1',
    color: 'blue',
    icon: '🔵'
  },
  {
    id: 'server2',
    name: 'Servidor 2',
    route: '/server2',
    color: 'green',
    icon: '🟢'
  },
  {
    id: 'server3',
    name: 'Servidor 3',
    route: '/server3',
    color: 'purple',
    icon: '🟣'
  },
  {
    id: 'server4',
    name: 'Servidor 4',
    route: '/server4',
    color: 'orange',
    icon: '🟠'
  }
];

// Crear instancia de axios
const apiClient = axios.create({
  baseURL: API_GATEWAY_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para manejo de errores
apiClient.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

// Servicios API para cada servidor
class ServerApiService {
  constructor(serverRoute) {
    this.baseRoute = serverRoute;
  }

  // Health Check
  async getHealth() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/health`);
    return response.data;
  }

  // Información del servidor
  async getInfo() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/info`);
    return response.data;
  }

  // Usuarios
  async getUsers() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/users`);
    return response.data;
  }

  async getConnectedUsers() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/users/connected`);
    return response.data;
  }

  // Canales
  async getChannels() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/channels`);
    return response.data;
  }

  // Logs
  async getLogs(params = {}) {
    const { limit = 100, level, source } = params;
    const queryParams = new URLSearchParams({ limit });
    if (level) queryParams.append('level', level);
    if (source) queryParams.append('source', source);
    
    const response = await apiClient.get(
      `${this.baseRoute}/api/v1/server/logs?${queryParams}`
    );
    return response.data;
  }

  async clearLogs() {
    const response = await apiClient.delete(`${this.baseRoute}/api/v1/server/logs`);
    return response.data;
  }

  // Estadísticas
  async getStats() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/stats`);
    return response.data;
  }

  // Reporte completo
  async getReport() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/report`);
    return response.data;
  }

  // Federación
  async getFederation() {
    const response = await apiClient.get(`${this.baseRoute}/api/v1/server/federation`);
    return response.data;
  }

  // Audios
  async getAudios(params = {}) {
    const { limit = 50 } = params;
    const queryParams = new URLSearchParams({ limit });
    
    const response = await apiClient.get(
      `${this.baseRoute}/api/v1/server/audios?${queryParams}`
    );
    return response.data;
  }
}

// Crear instancias de servicio para cada servidor
export const serverServices = SERVERS.reduce((acc, server) => {
  acc[server.id] = new ServerApiService(server.route);
  return acc;
}, {});

export default ServerApiService;
