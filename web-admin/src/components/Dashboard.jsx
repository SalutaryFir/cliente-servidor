import { useState, useEffect } from 'react';
import { serverServices } from '../services/api';
import ServerInfo from './dashboard/ServerInfo';
import UsersSection from './dashboard/UsersSection';
import ChannelsSection from './dashboard/ChannelsSection';
import LogsSection from './dashboard/LogsSection';
import StatsSection from './dashboard/StatsSection';
import FederationSection from './dashboard/FederationSection';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';

function Dashboard({ selectedServer }) {
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const service = serverServices[selectedServer.id];
      
      const [info, users, channels, logs, stats, federation] = await Promise.all([
        service.getInfo().catch(() => null),
        service.getUsers().catch(() => null),
        service.getChannels().catch(() => null),
        service.getLogs({ limit: 50 }).catch(() => null),
        service.getStats().catch(() => null),
        service.getFederation().catch(() => null),
      ]);

      setData({
        info: info?.data,
        users: users?.data,
        channels: channels?.data,
        logs: logs?.data,
        stats: stats?.data,
        federation: federation?.data,
      });
    } catch (err) {
      setError(`Error al cargar datos del ${selectedServer.name}: ${err.message}`);
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [selectedServer]);

  useEffect(() => {
    if (!autoRefresh) return;
    
    const interval = setInterval(() => {
      fetchData();
    }, 5000); // Refrescar cada 5 segundos

    return () => clearInterval(interval);
  }, [autoRefresh, selectedServer]);

  if (loading && !data.info) {
    return <LoadingSpinner message={`Cargando datos de ${selectedServer.name}...`} />;
  }

  if (error && !data.info) {
    return (
      <ErrorMessage 
        message={error} 
        onRetry={fetchData}
      />
    );
  }

  return (
    <div className="space-y-6 fade-in">
      {/* Header con controles */}
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800">
          {selectedServer.icon} {selectedServer.name}
        </h2>
        
        <div className="flex items-center space-x-3">
          <label className="flex items-center space-x-2 cursor-pointer">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="w-4 h-4 text-primary-600 rounded focus:ring-2 focus:ring-primary-500"
            />
            <span className="text-sm text-gray-700">Auto-actualizar</span>
          </label>
          
          <button
            onClick={fetchData}
            disabled={loading}
            className="btn-secondary text-sm"
          >
            🔄 Actualizar
          </button>
        </div>
      </div>

      {/* Información del servidor */}
      {data.info && <ServerInfo info={data.info} />}

      {/* Grid de secciones principales */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {data.users && <UsersSection users={data.users} />}
        {data.channels && <ChannelsSection channels={data.channels} />}
      </div>

      {/* Estadísticas */}
      {data.stats && <StatsSection stats={data.stats} />}

      {/* Federación */}
      {data.federation && <FederationSection federation={data.federation} />}

      {/* Logs */}
      {data.logs && (
        <LogsSection 
          logs={data.logs} 
          serverId={selectedServer.id}
        />
      )}
    </div>
  );
}

export default Dashboard;
