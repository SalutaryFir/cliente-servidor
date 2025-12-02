@echo off
REM ========================================
REM   2. CONFIGURAR API GATEWAY
REM ========================================
REM Inicia monitoreo y configura Kong
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ========================================
echo   CONFIGURAR API GATEWAY
echo ========================================
echo.
echo Selecciona acci\u00f3n:
echo.
echo   1 - Iniciar Stack Completo (Docker, Kong, Grafana, etc)
echo   2 - Solo configurar rutas de Kong
echo   3 - Ver estado de servicios
echo   4 - Detener servicios
echo.
choice /C 1234 /N /M "Opci\u00f3n: "

if errorlevel 4 goto :detener
if errorlevel 3 goto :estado
if errorlevel 2 goto :configurar
if errorlevel 1 goto :iniciar
goto :fin

:iniciar
echo.
echo [1/2] Verificando Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo \u274c ERROR: Docker no est\u00e1 corriendo
    echo Abre Docker Desktop e intenta de nuevo
    pause
    exit /b 1
)

echo \u2705 Docker est\u00e1 corriendo
echo.
echo [2/2] Iniciando stack (Kong, Prometheus, Grafana, Loki, Tempo)...
echo.
docker-compose -f docker-compose-api-gateway.yml up -d

echo.
echo Esperando servicios...
timeout /t 10 >nul

echo.
echo \u2705 Stack iniciado
echo.
echo \ud83d\udd10 URLs de acceso:
echo   \ud83c\udf10 Kong Admin:     http://localhost:8001
echo   \ud83c\udf10 Web Admin:      http://localhost:3000
echo   \ud83d\udcc4 Grafana:        http://localhost:3001 (admin/admin)
echo   \ud83d\udcc4 Prometheus:     http://localhost:9090
echo   \ud83d\udcc4 Loki:           http://localhost:3100
echo   \ud83d\udcc4 Tempo:          http://localhost:3200
echo.
echo \u00bfConfiguraciones rutas de Kong ahora? (S/N)
choice /C SN /N /M "Respuesta: "
if errorlevel 2 goto :fin
if errorlevel 1 call :configurar

goto :fin

:configurar
echo.
echo [NOTA] Los servidores deben estar ejecut\u00e1ndose
echo Configurando rutas en Kong...
echo.
call configure-kong.bat
goto :fin

:estado
echo.
docker-compose -f docker-compose-api-gateway.yml ps
echo.
pause
goto :fin

:detener
echo.
echo Deteniendo servicios...
docker-compose -f docker-compose-api-gateway.yml down
echo \u2705 Servicios detenidos
echo.
pause

:fin
exit /b 0
