import { Server, Network, Users, Clock } from 'lucide-react';
import { formatUptime, getStatusColor } from '../../utils/helpers';

function ServerInfo({ info }) {
  const stats = [
    {
      label: 'IP del Servidor',
      value: info.serverIp || 'N/A',
      icon: <Network className="h-5 w-5" />,
      color: 'text-blue-600'
    },
    {
      label: 'Puerto Cliente',
      value: info.clientPort,
      icon: <Server className="h-5 w-5" />,
      color: 'text-green-600'
    },
    {
      label: 'Puerto Federación',
      value: info.federationPort,
      icon: <Network className="h-5 w-5" />,
      color: 'text-purple-600'
    },
    {
      label: 'Uptime',
      value: formatUptime(info.uptimeMillis),
      icon: <Clock className="h-5 w-5" />,
      color: 'text-orange-600'
    }
  ];

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-xl font-bold text-gray-800 flex items-center">
          <Server className="h-6 w-6 mr-2 text-primary-600" />
          Información del Servidor
        </h3>
        <span className={`badge ${getStatusColor(info.status)}`}>
          {info.status || 'RUNNING'}
        </span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
        {stats.map((stat, index) => (
          <div key={index} className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center space-x-2 mb-2">
              <span className={stat.color}>{stat.icon}</span>
              <span className="text-sm text-gray-600 font-medium">
                {stat.label}
              </span>
            </div>
            <p className="text-2xl font-bold text-gray-800">{stat.value}</p>
          </div>
        ))}
      </div>

      <div className="mt-4 pt-4 border-t border-gray-200">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600">Conexiones Activas</p>
            <p className="text-3xl font-bold text-gray-800">
              {info.connectedClients}
              <span className="text-lg text-gray-500">
                / {info.maxConnections}
              </span>
            </p>
          </div>
          
          <div className="text-right">
            <p className="text-sm text-gray-600 mb-1">Servidores Federados</p>
            <div className="flex flex-wrap gap-1 justify-end">
              {info.federatedServers && info.federatedServers.length > 0 ? (
                info.federatedServers.map((server, index) => (
                  <span key={index} className="badge-info text-xs">
                    {server}
                  </span>
                ))
              ) : (
                <span className="text-sm text-gray-500">Sin federación</span>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ServerInfo;
