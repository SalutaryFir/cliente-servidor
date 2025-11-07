@echo off
echo ========================================
echo   INICIANDO RED COMPLETA DE CHAT
echo ========================================
echo.

REM Matar procesos Java previos
echo [1/8] Cerrando procesos Java anteriores...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 >nul

REM Compilar proyecto
echo [2/8] Compilando proyecto...
cd /d "%~dp0"
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO SERVIDORES
echo ========================================

REM Servidor Principal (Puerto 5000)
echo [3/8] Iniciando Servidor Principal (puerto 5000)...
start "Servidor-Principal" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar"
timeout /t 8

REM Servidor 2 (Puerto 5002)
echo [4/8] Iniciando Servidor-Secundario (puerto 5002)...
start "Servidor-Secundario" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server2"
timeout /t 8

REM Servidor 3 (Puerto 5004)
echo [5/8] Iniciando Servidor-Terciario (puerto 5004)...
start "Servidor-Terciario" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server3"
timeout /t 8

echo.
echo ========================================
echo   INICIANDO CLIENTES
echo ========================================

REM Cliente 1 -> Servidor Principal
echo [6/8] Iniciando Cliente-1 (conecta a Servidor Principal)...
start "Cliente-1-ServerPrincipal" cmd /k "cd cliente\target && java -jar cliente-0.0.1-SNAPSHOT.jar"
timeout /t 5

REM Cliente 2 -> Servidor 2
echo [7/8] Iniciando Cliente-2 (conecta a Servidor Secundario)...
start "Cliente-2-ServerSecundario" cmd /k "cd cliente\target && java -jar cliente-0.0.1-SNAPSHOT.jar --chat.client.server-port=5002"
timeout /t 5

REM Cliente 3 -> Servidor 3
echo [8/8] Iniciando Cliente-3 (conecta a Servidor Terciario)...
start "Cliente-3-ServerTerciario" cmd /k "cd cliente\target && java -jar cliente-0.0.1-SNAPSHOT.jar --chat.client.server-port=5004"

echo.
echo ========================================
echo   TODOS LOS PROCESOS INICIADOS
echo ========================================
echo.
echo Servidores:
echo   - Servidor Principal:   http://localhost:8080
echo   - Servidor Secundario:  http://localhost:8081
echo   - Servidor Terciario:   http://localhost:8082
echo.
echo IMPORTANTE: Ahora debes federar los servidores manualmente:
echo   1. En UI del Servidor Principal:
echo      IP: 127.0.0.1, Puerto: 5003, Click "Conectar"
echo   2. En UI del Servidor Principal:
echo      IP: 127.0.0.1, Puerto: 5005, Click "Conectar"
echo.
echo Luego registra usuarios en cada cliente y prueba el chat.
echo.
pause