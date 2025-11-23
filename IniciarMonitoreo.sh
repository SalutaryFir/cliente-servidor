#!/bin/bash

echo "===================================="
echo " Iniciando Stack de Monitoreo"
echo " Grafana + Prometheus + Loki + Tempo"
echo "===================================="
echo ""

# Verificar si Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ ERROR: Docker no está corriendo"
    echo ""
    echo "Por favor, inicia Docker y vuelve a ejecutar este script."
    exit 1
fi

echo "✅ Docker está corriendo"
echo ""

# Verificar si ya hay servicios corriendo
if docker-compose -f docker-compose-api-gateway.yml ps | grep -q "Up"; then
    echo "⚠️  Los servicios ya están corriendo"
    echo ""
    read -p "¿Deseas reiniciar los servicios? (s/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        echo ""
        echo "🔄 Deteniendo servicios..."
        docker-compose -f docker-compose-api-gateway.yml down
        echo ""
    fi
fi

echo "🚀 Iniciando servicios Docker..."
echo "   - PostgreSQL (Kong)"
echo "   - Kong API Gateway"
echo "   - Web Admin"
echo "   - Prometheus"
echo "   - Grafana"
echo "   - Loki"
echo "   - Promtail"
echo "   - Tempo"
echo ""

docker-compose -f docker-compose-api-gateway.yml up -d

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ ERROR: Falló el inicio de los servicios"
    exit 1
fi

echo ""
echo "✅ Servicios iniciados correctamente"
echo ""
echo "===================================="
echo " Esperando a que los servicios estén listos..."
echo "===================================="

# Función para esperar a un servicio
wait_for_service() {
    local url=$1
    local name=$2
    echo "🔄 Esperando a $name..."
    until curl -sf "$url" > /dev/null 2>&1; do
        sleep 2
    done
    echo "✅ $name está listo"
}

# Esperar a los servicios
wait_for_service "http://localhost:8001/" "Kong API Gateway"
wait_for_service "http://localhost:9090/-/ready" "Prometheus"
wait_for_service "http://localhost:3001/api/health" "Grafana"
wait_for_service "http://localhost:3100/ready" "Loki"
wait_for_service "http://localhost:3200/ready" "Tempo"

echo ""
echo "===================================="
echo " ✅ Stack de Monitoreo Iniciado"
echo "===================================="
echo ""
echo "📊 URLs de Acceso:"
echo ""
echo "   🌐 Web Admin:    http://localhost:3000"
echo "   📈 Grafana:      http://localhost:3001"
echo "      Usuario: admin"
echo "      Contraseña: admin"
echo ""
echo "   📉 Prometheus:   http://localhost:9090"
echo "   📝 Loki:         http://localhost:3100"
echo "   🔍 Tempo:        http://localhost:3200"
echo "   🚪 Kong Admin:   http://localhost:8001"
echo ""
echo "===================================="
echo " Configurar Rutas de Kong"
echo "===================================="
echo ""
read -p "¿Deseas configurar las rutas de Kong ahora? (s/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo ""
    echo "🔧 Configurando Kong..."
    ./configure-kong.sh
else
    echo ""
    echo "⚠️  Recuerda ejecutar './configure-kong.sh' antes de usar el Web Admin"
    echo ""
fi

echo "===================================="
echo " Próximos Pasos"
echo "===================================="
echo ""
echo "1. Si no lo hiciste, ejecuta: ./configure-kong.sh"
echo "2. Inicia los servidores: ./IniciarServidores.sh"
echo "3. Accede a Grafana: http://localhost:3001"
echo "4. Revisa el dashboard 'Chat Federado'"
echo ""
echo "Para ver los logs de los servicios:"
echo "   docker-compose -f docker-compose-api-gateway.yml logs -f"
echo ""
echo "Para detener todos los servicios:"
echo "   docker-compose -f docker-compose-api-gateway.yml down"
echo ""
