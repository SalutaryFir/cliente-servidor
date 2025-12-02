@echo off
echo ========================================
echo   INICIAR API GATEWAY ^(Kong^)
echo ========================================
echo.

REM Verificar Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker no esta instalado o no esta en PATH
    echo Por favor instala Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo [1/3] Verificando Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker no esta corriendo
    echo Por favor inicia Docker Desktop
    pause
    exit /b 1
)
echo [OK] Docker esta corriendo
echo.

echo [2/3] Iniciando Kong API Gateway...
docker-compose -f docker-compose-api-gateway.yml up -d kong
if errorlevel 1 (
    echo [ERROR] Fallo al iniciar Kong
    pause
    exit /b 1
)
echo.

echo [3/3] Esperando a que Kong este listo...
timeout /t 10 /nobreak >nul

REM Verificar que Kong este respondiendo
curl -s http://localhost:8001 >nul 2>&1
if errorlevel 1 (
    echo [ADVERTENCIA] Kong puede tardar un poco mas en iniciar
    echo Espera 30 segundos e intenta de nuevo
) else (
    echo [OK] Kong esta listo!
)

echo.
echo ========================================
echo   API GATEWAY INICIADO
echo ========================================
echo.
echo Kong Admin API: http://localhost:8001
echo Kong Proxy:     http://localhost:8000
echo.
echo SIGUIENTE PASO:
echo   Ejecuta: ConfigurarAPIGateway.bat
echo   Para configurar las rutas a tus servidores
echo.
pause
