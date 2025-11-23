import { Network, Server, Users } from 'lucide-react';

function FederationSection({ federation }) {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-xl font-bold text-gray-800 flex items-center">
          <Network className="h-6 w-6 mr-2 text-primary-600" />
          Estado de Federación
        </h3>
        <span className="badge-success">
          {federation.connectedServers} servidores
        </span>
      </div>

      {federation.connectedServers > 0 ? (
        <div className="space-y-4">
          {/* Servidores conectados */}
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-3 flex items-center">
              <Server className="h-4 w-4 mr-1" />
              Servidores Federados ({federation.connectedServers})
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {federation.servers?.map((server, index) => (
                <div
                  key={index}
                  className="bg-gradient-to-r from-purple-50 to-blue-50 border border-purple-200 rounded-lg p-4"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-bold text-gray-800 mb-1">
                        {server.serverName}
                      </p>
                      <p className="text-xs text-gray-600">
                        {server.ipAddress}:{server.federationPort}
                      </p>
                    </div>
                    <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Usuarios remotos */}
          {federation.remoteUsers && federation.remoteUsers.length > 0 && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <h4 className="text-sm font-semibold text-gray-700 mb-3 flex items-center">
                <Users className="h-4 w-4 mr-1" />
                Usuarios Remotos ({federation.remoteUsers.length})
              </h4>
              <div className="flex flex-wrap gap-2">
                {federation.remoteUsers.map((user, index) => (
                  <span key={index} className="badge bg-purple-100 text-purple-800">
                    {user}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      ) : (
        <div className="text-center py-8 text-gray-500">
          <Network className="h-12 w-12 mx-auto mb-2 opacity-50" />
          <p>No hay servidores federados conectados</p>
          <p className="text-sm mt-1">Este servidor opera de forma independiente</p>
        </div>
      )}
    </div>
  );
}

export default FederationSection;
