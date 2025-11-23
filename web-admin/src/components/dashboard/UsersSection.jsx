import { Users, UserCheck, UserX } from 'lucide-react';

function UsersSection({ users }) {
  const onlineUsers = users.users?.filter(u => u.online) || [];
  const offlineUsers = users.users?.filter(u => !u.online) || [];

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-xl font-bold text-gray-800 flex items-center">
          <Users className="h-6 w-6 mr-2 text-primary-600" />
          Usuarios
        </h3>
        <div className="flex space-x-2">
          <span className="badge-success">
            {users.connectedUsers} conectados
          </span>
          <span className="badge bg-gray-100 text-gray-800">
            {users.totalUsers} total
          </span>
        </div>
      </div>

      <div className="space-y-2 max-h-96 overflow-y-auto">
        {/* Usuarios online */}
        {onlineUsers.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-green-700 mb-2 flex items-center">
              <UserCheck className="h-4 w-4 mr-1" />
              En línea ({onlineUsers.length})
            </h4>
            {onlineUsers.map((user, index) => (
              <div
                key={`online-${index}`}
                className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg mb-2"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
                  <div>
                    <p className="font-medium text-gray-800">{user.username}</p>
                    <p className="text-xs text-gray-600">{user.email}</p>
                  </div>
                </div>
                <span className="badge-success text-xs">Online</span>
              </div>
            ))}
          </div>
        )}

        {/* Usuarios offline */}
        {offlineUsers.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-2 flex items-center">
              <UserX className="h-4 w-4 mr-1" />
              Desconectados ({offlineUsers.length})
            </h4>
            {offlineUsers.slice(0, 10).map((user, index) => (
              <div
                key={`offline-${index}`}
                className="flex items-center justify-between p-3 bg-gray-50 border border-gray-200 rounded-lg mb-2"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-3 h-3 bg-gray-400 rounded-full"></div>
                  <div>
                    <p className="font-medium text-gray-600">{user.username}</p>
                    <p className="text-xs text-gray-500">{user.email}</p>
                  </div>
                </div>
                <span className="badge bg-gray-100 text-gray-600 text-xs">
                  Offline
                </span>
              </div>
            ))}
            {offlineUsers.length > 10 && (
              <p className="text-sm text-gray-500 text-center mt-2">
                ... y {offlineUsers.length - 10} más
              </p>
            )}
          </div>
        )}

        {users.users?.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            <Users className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No hay usuarios registrados</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default UsersSection;
