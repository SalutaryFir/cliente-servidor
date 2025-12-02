import { useState, useEffect } from 'react';
import { Mic, Play, Pause, User, Hash, Clock } from 'lucide-react';
import { formatTimestamp } from '../../utils/helpers';
import { serverServices } from '../../services/api';

function AudiosSection({ serverId }) {
  const [audios, setAudios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(false);

  useEffect(() => {
    fetchAudios();
    
    if (autoRefresh) {
      const interval = setInterval(fetchAudios, 5000);
      return () => clearInterval(interval);
    }
  }, [autoRefresh, serverId]);

  const fetchAudios = async () => {
    try {
      setLoading(true);
      const response = await serverServices[serverId].getAudios({ limit: 50 });
      setAudios(response?.data?.audios || []);
    } catch (error) {
      console.error('Error fetching audios:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-4 gap-4">
        <div>
          <h3 className="text-xl font-bold text-gray-800 flex items-center">
            <Mic className="h-6 w-6 mr-2 text-purple-600" />
            Audios Procesados/Transcritos
            {autoRefresh && (
              <span className="ml-3 flex items-center text-sm font-normal text-green-600">
                <span className="h-2 w-2 bg-green-500 rounded-full mr-1 animate-pulse"></span>
                Actualizando
              </span>
            )}
          </h3>
          <p className="text-sm text-gray-600 mt-1">
            {audios.length} audios procesados
          </p>
        </div>

        <button
          onClick={() => setAutoRefresh(!autoRefresh)}
          className={`flex items-center space-x-1 px-3 py-1 rounded-full text-xs font-medium transition-all ${
            autoRefresh
              ? 'bg-green-100 text-green-700 hover:bg-green-200'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          {autoRefresh ? (
            <>
              <Pause className="h-3 w-3" />
              <span>Pausar</span>
            </>
          ) : (
            <>
              <Play className="h-3 w-3" />
              <span>Auto-actualizar</span>
            </>
          )}
        </button>
      </div>

      <div className="space-y-3 max-h-96 overflow-y-auto">
        {loading && audios.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            <div className="animate-spin h-8 w-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-2"></div>
            <p>Cargando audios...</p>
          </div>
        ) : audios.length > 0 ? (
          audios.map((audio) => (
            <div
              key={audio.id}
              className="bg-gradient-to-r from-purple-50 to-blue-50 rounded-lg p-4 border border-purple-200 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-2">
                <div className="flex items-center space-x-2">
                  <User className="h-4 w-4 text-purple-600" />
                  <span className="font-semibold text-gray-800">{audio.sender}</span>
                  <span className="text-gray-400">→</span>
                  {audio.isChannelMessage ? (
                    <>
                      <Hash className="h-4 w-4 text-blue-600" />
                      <span className="text-blue-600 font-medium">{audio.recipient}</span>
                    </>
                  ) : (
                    <>
                      <User className="h-4 w-4 text-gray-600" />
                      <span className="text-gray-600">{audio.recipient}</span>
                    </>
                  )}
                </div>
                <div className="flex items-center text-xs text-gray-500">
                  <Clock className="h-3 w-3 mr-1" />
                  {formatTimestamp(new Date(audio.timestamp).getTime())}
                </div>
              </div>

              <div className="bg-white rounded p-3 border border-gray-200">
                <div className="flex items-center space-x-2 mb-2">
                  <Mic className="h-4 w-4 text-purple-600" />
                  <span className="text-xs font-mono text-gray-500">{audio.audioFileName}</span>
                </div>
                {audio.transcription ? (
                  <div className="mt-2 p-2 bg-gray-50 rounded border-l-4 border-purple-500">
                    <p className="text-sm text-gray-700 italic">"{audio.transcription}"</p>
                  </div>
                ) : (
                  <div className="mt-2 p-2 bg-yellow-50 rounded border-l-4 border-yellow-500">
                    <p className="text-xs text-yellow-700">⚠️ Sin transcripción disponible</p>
                  </div>
                )}
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8 text-gray-400">
            <Mic className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No hay audios procesados aún</p>
            <p className="text-xs mt-2">Los audios aparecerán aquí cuando los usuarios envíen mensajes de voz</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default AudiosSection;
