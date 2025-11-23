export const formatUptime = (milliseconds) => {
  const seconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 0) return `${days}d ${hours % 24}h`;
  if (hours > 0) return `${hours}h ${minutes % 60}m`;
  if (minutes > 0) return `${minutes}m ${seconds % 60}s`;
  return `${seconds}s`;
};

export const formatTimestamp = (timestamp) => {
  return new Date(timestamp).toLocaleString('es-ES', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

export const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
};

export const getStatusColor = (status) => {
  const colors = {
    RUNNING: 'text-green-600 bg-green-100',
    DEGRADED: 'text-yellow-600 bg-yellow-100',
    MAINTENANCE: 'text-gray-600 bg-gray-100',
    DOWN: 'text-red-600 bg-red-100'
  };
  return colors[status] || colors.DOWN;
};

export const getLogLevelColor = (level) => {
  const colors = {
    INFO: 'badge-info',
    WARN: 'badge-warning',
    ERROR: 'badge-error',
    DEBUG: 'badge bg-gray-100 text-gray-800'
  };
  return colors[level] || 'badge';
};
