@echo off
chcp 65001 >nul
echo ========================================
echo   CONFIGURAR KONG API GATEWAY
echo ========================================
echo.

echo REQUISITO: Docker Desktop debe estar instalado y ejecutándose
echo.
pause

echo [1/3] Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Docker no está instalado o no está en PATH
    echo.
    echo Instala Docker Desktop desde: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo [2/3] Iniciando Kong con Docker Compose...
docker-compose -f docker-compose-api-gateway.yml up -d

if errorlevel 1 (
    echo ❌ Error al iniciar Kong
    pause
    exit /b 1
)

echo.
echo [3/3] Esperando a que Kong esté listo...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   KONG INICIADO CORRECTAMENTE
echo ========================================
echo.
echo Kong Admin:    http://localhost:8001
echo Kong Gateway:  http://localhost:8000
echo.
echo Verifica el estado con: docker ps
echo.
pause
