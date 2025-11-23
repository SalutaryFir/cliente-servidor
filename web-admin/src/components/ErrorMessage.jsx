import { AlertCircle } from 'lucide-react';

function ErrorMessage({ message, onRetry }) {
  return (
    <div className="card bg-red-50 border border-red-200 fade-in">
      <div className="flex items-start space-x-3">
        <AlertCircle className="h-6 w-6 text-red-600 flex-shrink-0 mt-0.5" />
        <div className="flex-1">
          <h3 className="text-lg font-semibold text-red-800 mb-1">
            Error de Conexión
          </h3>
          <p className="text-red-700 text-sm mb-4">{message}</p>
          {onRetry && (
            <button onClick={onRetry} className="btn-primary text-sm">
              Reintentar
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default ErrorMessage;
