function LoadingSpinner({ message = 'Cargando...' }) {
  return (
    <div className="flex flex-col items-center justify-center py-12">
      <div className="loading-spinner"></div>
      <p className="mt-4 text-gray-600 font-medium">{message}</p>
    </div>
  );
}

export default LoadingSpinner;
