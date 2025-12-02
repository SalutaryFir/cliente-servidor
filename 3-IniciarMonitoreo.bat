@echo off
REM ========================================
REM   3. INICIAR MONITOREO
REM ========================================
REM Inicia stack completo de monitoreo
REM (Grafana, Prometheus, Loki, Tempo)
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ========================================
echo   INICIAR MONITOREO
echo ========================================
echo.
echo [1/3] Verificando Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo \u274c ERROR: Docker no est\u00e1 corriendo
    echo Abre Docker Desktop e intenta de nuevo
    pause
    exit /b 1
)

echo \u2705 Docker corriendo
echo.
echo [2/3] Iniciando stack...
docker-compose -f docker-compose-api-gateway.yml up -d

if errorlevel 1 (
    echo \u274c ERROR al iniciar servicios
    pause
    exit /b 1
)

echo \u2705 Stack iniciado
echo.
echo [3/3] Esperando que est\u00e9n listos...
timeout /t 5 >nul

echo.
echo ========================================
echo   MONITOREO INICIADO
echo ========================================
echo.
echo \ud83d\udcc4 URLs de acceso:
echo.
echo   Grafana:      http://localhost:3001
echo   Usuario:      admin
echo   Contrase\u00f1a:   admin
echo.
echo   Prometheus:   http://localhost:9090
echo   Loki:         http://localhost:3100
echo   Tempo:        http://localhost:3200
echo.
echo \ud83d\udea8 Pr\u00f3ximos pasos:
echo.
echo   1. Abre Grafana: http://localhost:3001
echo   2. Ve a Dashboards
echo   3. Busca "Chat Servers Dashboard"
echo   4. Monitorea m\u00e9tricas en tiempo real
echo.
echo \ud83d\udce8 Logs:
echo   docker-compose -f docker-compose-api-gateway.yml logs -f
echo.
echo \ud83d\udeab Para detener:
echo   docker-compose -f docker-compose-api-gateway.yml down
echo.
pause

exit /b 0
