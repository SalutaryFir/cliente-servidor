import { useState } from 'react';
import { FileText, Trash2, Filter } from 'lucide-react';
import { formatTimestamp, getLogLevelColor } from '../../utils/helpers';
import { serverServices } from '../../services/api';

function LogsSection({ logs, serverId }) {
  const [filter, setFilter] = useState('ALL');
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  const filteredLogs = logs.logs?.filter(log => 
    filter === 'ALL' || log.level === filter
  ) || [];

  const handleClearLogs = async () => {
    try {
      await serverServices[serverId].clearLogs();
      setShowClearConfirm(false);
      window.location.reload(); // Recargar para ver cambios
    } catch (error) {
      console.error('Error clearing logs:', error);
      alert('Error al limpiar logs');
    }
  };

  const logLevels = ['ALL', 'INFO', 'WARN', 'ERROR', 'DEBUG'];
  const levelCounts = {
    INFO: logs.logs?.filter(l => l.level === 'INFO').length || 0,
    WARN: logs.logs?.filter(l => l.level === 'WARN').length || 0,
    ERROR: logs.logs?.filter(l => l.level === 'ERROR').length || 0,
    DEBUG: logs.logs?.filter(l => l.level === 'DEBUG').length || 0,
  };

  return (
    <div className="card">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-4 gap-4">
        <div>
          <h3 className="text-xl font-bold text-gray-800 flex items-center">
            <FileText className="h-6 w-6 mr-2 text-primary-600" />
            Logs del Sistema
          </h3>
          <p className="text-sm text-gray-600 mt-1">
            {filteredLogs.length} de {logs.totalLogs} logs
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
          {/* Filtros */}
          <div className="flex items-center space-x-2">
            <Filter className="h-4 w-4 text-gray-600" />
            {logLevels.map(level => (
              <button
                key={level}
                onClick={() => setFilter(level)}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-all ${
                  filter === level
                    ? 'bg-primary-600 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                {level}
                {level !== 'ALL' && ` (${levelCounts[level]})`}
              </button>
            ))}
          </div>

          {/* Botón limpiar */}
          {!showClearConfirm ? (
            <button
              onClick={() => setShowClearConfirm(true)}
              className="btn-danger text-sm flex items-center space-x-1"
            >
              <Trash2 className="h-4 w-4" />
              <span>Limpiar</span>
            </button>
          ) : (
            <div className="flex items-center space-x-2">
              <span className="text-sm text-red-600 font-medium">¿Confirmar?</span>
              <button
                onClick={handleClearLogs}
                className="btn-danger text-xs"
              >
                Sí
              </button>
              <button
                onClick={() => setShowClearConfirm(false)}
                className="btn-secondary text-xs"
              >
                No
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="space-y-2 max-h-96 overflow-y-auto bg-gray-900 rounded-lg p-4">
        {filteredLogs.length > 0 ? (
          filteredLogs.map((log, index) => (
            <div
              key={index}
              className="font-mono text-xs p-3 bg-gray-800 rounded border-l-4 hover:bg-gray-750 transition-colors"
              style={{
                borderLeftColor: {
                  INFO: '#3b82f6',
                  WARN: '#f59e0b',
                  ERROR: '#ef4444',
                  DEBUG: '#6b7280'
                }[log.level] || '#6b7280'
              }}
            >
              <div className="flex items-start justify-between mb-1">
                <span className={`${getLogLevelColor(log.level)} text-xs font-bold`}>
                  [{log.level}]
                </span>
                <span className="text-gray-400 text-xs">
                  {formatTimestamp(log.timestamp)}
                </span>
              </div>
              <div className="text-gray-300 mb-1">{log.message}</div>
              <div className="text-gray-500 text-xs">
                Fuente: <span className="text-blue-400">{log.source}</span>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8 text-gray-400">
            <FileText className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No hay logs disponibles</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default LogsSection;
