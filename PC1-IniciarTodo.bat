@echo off
REM ========================================
REM   INICIAR API GATEWAY + WEB ADMIN
REM   Ejecutar en PC 1
REM ========================================
echo.
echo ========================================
echo   PC 1: INICIAR SISTEMA COMPLETO
echo ========================================
echo.

cd /d "%~dp0"

REM ========================================
REM PASO 1: VERIFICAR DOCKER
REM ========================================
echo [1/5] Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker no instalado
    echo Descarga: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker no esta corriendo
    echo Abre Docker Desktop
    pause
    exit /b 1
)
echo [OK] Docker corriendo

REM ========================================
REM PASO 2: INICIAR KONG
REM ========================================
echo.
echo [2/5] Iniciando Kong API Gateway...
docker-compose -f docker-compose-api-gateway.yml up -d kong
if errorlevel 1 (
    echo [ERROR] Fallo al iniciar Kong
    pause
    exit /b 1
)

echo [OK] Kong iniciado
echo Esperando 15 segundos...
timeout /t 15 /nobreak >nul

REM Verificar Kong
curl -s http://localhost:8001 >nul 2>&1
if errorlevel 1 (
    echo [ADVERTENCIA] Kong puede tardar mas
    echo Esperando 15 segundos adicionales...
    timeout /t 15 /nobreak >nul
)

REM ========================================
REM PASO 3: CONFIGURAR SERVIDORES
REM ========================================
echo.
echo [3/5] Configuracion de servidores
echo.
echo Ahora debes configurar cada servidor en Kong.
echo.
choice /C SN /N /M "Configurar servidor ahora? (S/N): "
if errorlevel 2 goto :skip_config
if errorlevel 1 (
    start cmd /k PC1-ConfigurarKong.bat
    echo.
    echo Configuracion de Kong en otra ventana...
    echo Presiona una tecla cuando termines
    pause >nul
)

:skip_config

REM ========================================
REM PASO 4: VERIFICAR NODE.JS
REM ========================================
echo.
echo [4/5] Verificando Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js no instalado
    echo Descarga: https://nodejs.org/
    pause
    exit /b 1
)
echo [OK] Node.js disponible

REM ========================================
REM PASO 5: INICIAR WEB ADMIN
REM ========================================
echo.
echo [5/5] Iniciando Web Admin...
echo.
echo Modo:
echo   1 - Desarrollo (npm run dev - puerto 5173)
echo   2 - Produccion compilada (puerto 8888)
echo.
choice /C 12 /N /M "Selecciona: "

if errorlevel 2 goto :produccion
if errorlevel 1 goto :desarrollo

:desarrollo
echo.
echo Iniciando en modo desarrollo...
cd web-admin
call npm install
echo.
echo ========================================
echo   WEB ADMIN CORRIENDO
echo ========================================
echo.
echo URL: http://localhost:5173
echo Kong Gateway: http://localhost:8000
echo Kong Admin: http://localhost:8001
echo.
call npm run dev
goto :fin

:produccion
echo.
echo Verificando build...
if not exist "web-admin\dist" (
    echo Compilando por primera vez...
    cd web-admin
    call npm install
    call npm run build
    cd ..
)

echo.
echo Instalando http-server...
call npm install -g http-server >nul 2>&1

echo.
echo ========================================
echo   WEB ADMIN CORRIENDO (Produccion)
echo ========================================
echo.
echo URL: http://localhost:8888
echo Kong Gateway: http://localhost:8000
echo Kong Admin: http://localhost:8001
echo.
cd web-admin\dist
http-server -p 8888

:fin
echo.
echo Sistema detenido
pause
