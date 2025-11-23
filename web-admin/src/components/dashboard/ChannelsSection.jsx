import { Hash, Users } from 'lucide-react';

function ChannelsSection({ channels }) {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-xl font-bold text-gray-800 flex items-center">
          <Hash className="h-6 w-6 mr-2 text-primary-600" />
          Canales
        </h3>
        <span className="badge-info">
          {channels.totalChannels} canales
        </span>
      </div>

      <div className="space-y-2 max-h-96 overflow-y-auto">
        {channels.channels && channels.channels.length > 0 ? (
          channels.channels.map((channel, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-purple-50 border border-blue-200 rounded-lg hover:shadow-md transition-shadow"
            >
              <div className="flex-1">
                <div className="flex items-center space-x-2 mb-1">
                  <Hash className="h-4 w-4 text-blue-600" />
                  <p className="font-bold text-gray-800">{channel.channelName}</p>
                  {channel.federated && (
                    <span className="badge bg-purple-100 text-purple-800 text-xs">
                      Federado
                    </span>
                  )}
                </div>
                <p className="text-xs text-gray-600">
                  Creado por: <span className="font-medium">{channel.creator}</span>
                </p>
                {channel.createdAt && (
                  <p className="text-xs text-gray-500 mt-1">
                    {new Date(channel.createdAt).toLocaleDateString('es-ES')}
                  </p>
                )}
              </div>
              
              <div className="text-right">
                <div className="flex items-center space-x-1 text-gray-700">
                  <Users className="h-4 w-4" />
                  <span className="font-bold">{channel.memberCount}</span>
                </div>
                <p className="text-xs text-gray-500">miembros</p>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8 text-gray-500">
            <Hash className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No hay canales creados</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default ChannelsSection;
