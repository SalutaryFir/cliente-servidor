@echo off
echo ========================================
echo   INICIANDO SERVIDORES DE CHAT
echo ========================================
echo.

REM Matar procesos Java previos de servidores
echo [1/5] Cerrando servidores anteriores...
for /f "tokens=2" %%i in ('tasklist /FI "WINDOWTITLE eq Servidor*" /NH') do taskkill /F /PID %%i >nul 2>&1
timeout /t 2 >nul

REM Compilar proyecto
echo [2/5] Compilando proyecto...
cd /d "%~dp0"
call mvn clean package -DskipTests -pl servidor,comun
if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO SERVIDORES
echo ========================================

REM Servidor 1 - Principal/Coordinador (Puerto 5000, API 8080)
echo [3/5] Iniciando Servidor-1 (Coordinador - puerto 5000, API 8080)...
start "Servidor-1-Coordinador" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server1"
timeout /t 8

REM Servidor 2 (Puerto 5002, API 8081)
echo [4/5] Iniciando Servidor-2 (puerto 5002, API 8081)...
start "Servidor-2" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server2"
timeout /t 8

REM Servidor 3 (Puerto 5004, API 8082)
echo [5/5] Iniciando Servidor-3 (puerto 5004, API 8082)...
start "Servidor-3" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server3"
timeout /t 8

REM Servidor 4 (Puerto 5006, API 8083)
echo [6/6] Iniciando Servidor-4 (puerto 5006, API 8083)...
start "Servidor-4" cmd /k "cd servidor\target && java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server4"

echo.
echo ========================================
echo   SERVIDORES INICIADOS
echo ========================================
echo.
echo Servidores de Chat (Clientes):
echo   - Servidor-1 (Coordinador): Puerto 5000  - API REST: http://localhost:8080
echo   - Servidor-2:               Puerto 5002  - API REST: http://localhost:8081
echo   - Servidor-3:               Puerto 5004  - API REST: http://localhost:8082
echo   - Servidor-4:               Puerto 5006  - API REST: http://localhost:8083
echo.
echo Puertos de Federacion:
echo   - Servidor-1: 5001
echo   - Servidor-2: 5003
echo   - Servidor-3: 5005
echo   - Servidor-4: 5007
echo.
echo ========================================
echo   SIGUIENTE PASO: FEDERAR SERVIDORES
echo ========================================
echo.
echo Opcion 1 - Federacion Manual (recomendado para aprendizaje):
echo   En la UI del Servidor-1 (Coordinador):
echo     1. IP: 127.0.0.1, Puerto: 5003 [Servidor-2]
echo     2. IP: 127.0.0.1, Puerto: 5005 [Servidor-3]
echo     3. IP: 127.0.0.1, Puerto: 5007 [Servidor-4]
echo.
echo Opcion 2 - API REST (desde consola):
echo   curl -X POST http://localhost:8080/api/v1/server/federation/connect?ip=127.0.0.1^&port=5003
echo   curl -X POST http://localhost:8080/api/v1/server/federation/connect?ip=127.0.0.1^&port=5005
echo   curl -X POST http://localhost:8080/api/v1/server/federation/connect?ip=127.0.0.1^&port=5007
echo.
echo ========================================
echo.
echo Para iniciar clientes, ejecuta: IniciarClientes.bat
echo.
pause
