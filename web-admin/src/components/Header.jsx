import { Server, Activity } from 'lucide-react';

function Header() {
  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="container mx-auto px-4 py-4 max-w-7xl">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="bg-primary-500 p-2 rounded-lg">
              <Server className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-800">
                Chat Server Admin
              </h1>
              <p className="text-xs text-gray-500">Dashboard de Monitoreo</p>
            </div>
          </div>
          
          <div className="flex items-center space-x-2">
            <Activity className="h-5 w-5 text-green-500 animate-pulse" />
            <span className="text-sm font-medium text-gray-700">
              Sistema Activo
            </span>
          </div>
        </div>
      </div>
    </header>
  );
}

export default Header;
