import { useState } from 'react';
import ServerTabs from './components/ServerTabs';
import Dashboard from './components/Dashboard';
import Header from './components/Header';
import { SERVERS } from './services/api';

function App() {
  const [selectedServer, setSelectedServer] = useState(SERVERS[0]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <Header />
      
      <main className="container mx-auto px-4 py-6 max-w-7xl">
        <div className="mb-6 fade-in">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            Panel de Administración
          </h1>
          <p className="text-gray-600">
            Monitoreo y gestión de servidores de chat federado
          </p>
        </div>

        <ServerTabs
          servers={SERVERS}
          selectedServer={selectedServer}
          onSelectServer={setSelectedServer}
        />

        <Dashboard selectedServer={selectedServer} />
      </main>

      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="container mx-auto px-4 py-6 max-w-7xl">
          <div className="text-center text-gray-600 text-sm">
            <p>Chat Federado - Sistema de Administración v1.0.0</p>
            <p className="mt-1">© 2025 - Todos los derechos reservados</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
