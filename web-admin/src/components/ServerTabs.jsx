function ServerTabs({ servers, selectedServer, onSelectServer }) {
  return (
    <div className="bg-white rounded-lg shadow-md mb-6 overflow-hidden fade-in">
      <div className="flex overflow-x-auto">
        {servers.map((server) => {
          const isSelected = selectedServer.id === server.id;
          const colorClasses = {
            blue: 'border-blue-500 bg-blue-50 text-blue-700',
            green: 'border-green-500 bg-green-50 text-green-700',
            purple: 'border-purple-500 bg-purple-50 text-purple-700',
            orange: 'border-orange-500 bg-orange-50 text-orange-700',
          };
          
          return (
            <button
              key={server.id}
              onClick={() => onSelectServer(server)}
              className={`
                flex-1 min-w-[140px] px-6 py-4 text-center font-medium
                transition-all duration-200 border-b-4
                ${
                  isSelected
                    ? colorClasses[server.color]
                    : 'border-transparent text-gray-600 hover:bg-gray-50 hover:text-gray-800'
                }
              `}
            >
              <div className="flex items-center justify-center space-x-2">
                <span className="text-2xl">{server.icon}</span>
                <span className="font-semibold">{server.name}</span>
              </div>
              {isSelected && (
                <div className="mt-2 text-xs opacity-75">Seleccionado</div>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default ServerTabs;
