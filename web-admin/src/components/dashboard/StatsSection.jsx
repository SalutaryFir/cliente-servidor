import { BarChart, Database, MessageSquare, Activity, Cpu } from 'lucide-react';

function StatsSection({ stats }) {
  const memoryPercent = stats.memoryStats?.memoryUsagePercent || 0;
  
  const statCards = [
    {
      label: 'Mensajes Procesados',
      value: stats.totalMessagesProcessed?.toLocaleString() || '0',
      icon: <MessageSquare className="h-6 w-6" />,
      color: 'from-blue-500 to-blue-600',
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600'
    },
    {
      label: 'Usuarios Registrados',
      value: stats.totalRegisteredUsers?.toLocaleString() || '0',
      icon: <Database className="h-6 w-6" />,
      color: 'from-green-500 to-green-600',
      iconBg: 'bg-green-100',
      iconColor: 'text-green-600'
    },
    {
      label: 'Canales Activos',
      value: stats.totalChannels?.toLocaleString() || '0',
      icon: <Activity className="h-6 w-6" />,
      color: 'from-purple-500 to-purple-600',
      iconBg: 'bg-purple-100',
      iconColor: 'text-purple-600'
    },
    {
      label: 'Conexiones Actuales',
      value: stats.currentConnections?.toLocaleString() || '0',
      icon: <Cpu className="h-6 w-6" />,
      color: 'from-orange-500 to-orange-600',
      iconBg: 'bg-orange-100',
      iconColor: 'text-orange-600'
    }
  ];

  return (
    <div className="space-y-6">
      <div className="card">
        <h3 className="text-xl font-bold text-gray-800 flex items-center mb-6">
          <BarChart className="h-6 w-6 mr-2 text-primary-600" />
          Estadísticas del Servidor
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {statCards.map((stat, index) => (
            <div
              key={index}
              className={`bg-gradient-to-br ${stat.color} rounded-lg p-6 text-white shadow-lg hover:shadow-xl transition-shadow`}
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`${stat.iconBg} ${stat.iconColor} p-3 rounded-lg`}>
                  {stat.icon}
                </div>
              </div>
              <p className="text-3xl font-bold mb-2">{stat.value}</p>
              <p className="text-sm opacity-90">{stat.label}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Memoria */}
      <div className="card">
        <h4 className="text-lg font-bold text-gray-800 mb-4 flex items-center">
          <Cpu className="h-5 w-5 mr-2 text-primary-600" />
          Uso de Memoria
        </h4>
        
        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-sm mb-2">
              <span className="text-gray-600">Memoria Utilizada</span>
              <span className="font-bold text-gray-800">
                {stats.memoryStats?.usedMemoryMB || 0} MB / {stats.memoryStats?.totalMemoryMB || 0} MB
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  memoryPercent > 80 ? 'bg-red-500' : 
                  memoryPercent > 60 ? 'bg-yellow-500' : 
                  'bg-green-500'
                }`}
                style={{ width: `${memoryPercent}%` }}
              >
                <div className="h-full w-full bg-gradient-to-r from-transparent to-white opacity-20"></div>
              </div>
            </div>
            <div className="flex justify-between text-xs text-gray-500 mt-1">
              <span>0%</span>
              <span className="font-medium">{memoryPercent}%</span>
              <span>100%</span>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4 mt-4 pt-4 border-t border-gray-200">
            <div className="text-center">
              <p className="text-xs text-gray-600 mb-1">Total</p>
              <p className="text-lg font-bold text-gray-800">
                {stats.memoryStats?.totalMemoryMB || 0} MB
              </p>
            </div>
            <div className="text-center">
              <p className="text-xs text-gray-600 mb-1">Usado</p>
              <p className="text-lg font-bold text-blue-600">
                {stats.memoryStats?.usedMemoryMB || 0} MB
              </p>
            </div>
            <div className="text-center">
              <p className="text-xs text-gray-600 mb-1">Libre</p>
              <p className="text-lg font-bold text-green-600">
                {stats.memoryStats?.freeMemoryMB || 0} MB
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StatsSection;
