@echo off
chcp 65001 > nul
echo ====================================
echo  Iniciando Stack de Monitoreo
echo  Grafana + Prometheus + Loki + Tempo
echo ====================================
echo.

REM Verificar si Docker está corriendo
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ ERROR: Docker no está corriendo
    echo.
    echo Por favor, inicia Docker Desktop y vuelve a ejecutar este script.
    pause
    exit /b 1
)

echo ✅ Docker está corriendo
echo.

REM Verificar si ya hay servicios corriendo
docker-compose -f docker-compose-api-gateway.yml ps | findstr "Up" >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  Los servicios ya están corriendo
    echo.
    choice /C SN /M "¿Deseas reiniciar los servicios (S/N)?"
    if errorlevel 2 goto skip_restart
    
    echo.
    echo 🔄 Deteniendo servicios...
    docker-compose -f docker-compose-api-gateway.yml down
    echo.
)

:skip_restart
echo 🚀 Iniciando servicios Docker...
echo    - PostgreSQL (Kong)
echo    - Kong API Gateway
echo    - Web Admin
echo    - Prometheus
echo    - Grafana
echo    - Loki
echo    - Promtail
echo    - Tempo
echo.

docker-compose -f docker-compose-api-gateway.yml up -d

if errorlevel 1 (
    echo.
    echo ❌ ERROR: Falló el inicio de los servicios
    pause
    exit /b 1
)

echo.
echo ✅ Servicios iniciados correctamente
echo.
echo ====================================
echo  Esperando a que los servicios estén listos...
echo ====================================

REM Esperar a Kong
echo 🔄 Esperando a Kong API Gateway...
:wait_kong
timeout /t 2 /nobreak >nul
curl -s http://localhost:8001/ >nul 2>&1
if errorlevel 1 goto wait_kong
echo ✅ Kong está listo

REM Esperar a Prometheus
echo 🔄 Esperando a Prometheus...
:wait_prometheus
timeout /t 2 /nobreak >nul
curl -s http://localhost:9090/-/ready >nul 2>&1
if errorlevel 1 goto wait_prometheus
echo ✅ Prometheus está listo

REM Esperar a Grafana
echo 🔄 Esperando a Grafana...
:wait_grafana
timeout /t 2 /nobreak >nul
curl -s http://localhost:3001/api/health >nul 2>&1
if errorlevel 1 goto wait_grafana
echo ✅ Grafana está listo

REM Esperar a Loki
echo 🔄 Esperando a Loki...
:wait_loki
timeout /t 2 /nobreak >nul
curl -s http://localhost:3100/ready >nul 2>&1
if errorlevel 1 goto wait_loki
echo ✅ Loki está listo

REM Esperar a Tempo
echo 🔄 Esperando a Tempo...
:wait_tempo
timeout /t 2 /nobreak >nul
curl -s http://localhost:3200/ready >nul 2>&1
if errorlevel 1 goto wait_tempo
echo ✅ Tempo está listo

echo.
echo ====================================
echo  ✅ Stack de Monitoreo Iniciado
echo ====================================
echo.
echo 📊 URLs de Acceso:
echo.
echo    🌐 Web Admin:    http://localhost:3000
echo    📈 Grafana:      http://localhost:3001
echo       Usuario: admin
echo       Contraseña: admin
echo.
echo    📉 Prometheus:   http://localhost:9090
echo    📝 Loki:         http://localhost:3100
echo    🔍 Tempo:        http://localhost:3200
echo    🚪 Kong Admin:   http://localhost:8001
echo.
echo ====================================
echo  Configurar Rutas de Kong
echo ====================================
echo.
choice /C SN /M "¿Deseas configurar las rutas de Kong ahora (S/N)?"
if errorlevel 2 goto skip_kong_config

echo.
echo 🔧 Configurando Kong...
call configure-kong.bat
goto end

:skip_kong_config
echo.
echo ⚠️  Recuerda ejecutar 'configure-kong.bat' antes de usar el Web Admin
echo.

:end
echo ====================================
echo  Próximos Pasos
echo ====================================
echo.
echo 1. Si no lo hiciste, ejecuta: configure-kong.bat
echo 2. Inicia los servidores: IniciarServidores.bat
echo 3. Accede a Grafana: http://localhost:3001
echo 4. Revisa el dashboard "Chat Federado"
echo.
echo Para ver los logs de los servicios:
echo    docker-compose -f docker-compose-api-gateway.yml logs -f
echo.
echo Para detener todos los servicios:
echo    docker-compose -f docker-compose-api-gateway.yml down
echo.
pause
