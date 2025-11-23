@echo off
echo ========================================
echo   INICIANDO CLIENTES DE CHAT
echo ========================================
echo.

REM Verificar que al menos un servidor este corriendo
echo [1/4] Verificando servidores...
tasklist /FI "WINDOWTITLE eq Servidor*" | find "java.exe" >nul
if errorlevel 1 (
    echo.
    echo ERROR: No se detectaron servidores en ejecucion
    echo Por favor ejecuta primero: IniciarServidores.bat
    echo.
    pause
    exit /b 1
)
echo OK - Servidores detectados

REM Compilar modulo cliente
echo [2/4] Compilando cliente...
cd /d "%~dp0"
call mvn clean package -DskipTests -pl cliente,comun
if errorlevel 1 (
    echo ERROR: Fallo la compilacion del cliente
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO CLIENTES
echo ========================================
echo.
echo NOTA: Cada cliente te pedira la IP y puerto del servidor
echo       al que deseas conectarte.
echo.
echo Servidores disponibles:
echo   - Servidor-1: localhost:5000
echo   - Servidor-2: localhost:5002
echo   - Servidor-3: localhost:5004
echo   - Servidor-4: localhost:5006
echo.

REM Cliente 1
echo [3/4] Iniciando Cliente-1...
start "Cliente-1" cmd /k "cd cliente\target && java -jar cliente-0.0.1-SNAPSHOT.jar"
timeout /t 3

REM Cliente 2
echo [4/4] Iniciando Cliente-2...
start "Cliente-2" cmd /k "cd cliente\target && java -jar cliente-0.0.1-SNAPSHOT.jar"
timeout /t 3

REM Cliente 3 (opcional)
echo.
echo Deseas iniciar un tercer cliente? (S/N)
choice /C SN /N /M "Presiona S para Si o N para No: "
if errorlevel 2 goto :fin
if errorlevel 1 (
    echo Iniciando Cliente-3...
    start "Cliente-3" cmd /k "cd cliente\target && java -jar cliente-0.0.1-SNAPSHOT.jar"
)

:fin
echo.
echo ========================================
echo   CLIENTES INICIADOS
echo ========================================
echo.
echo Cada cliente mostrara un dialogo para conectarse.
echo Ingresa la IP y puerto del servidor deseado:
echo.
echo Ejemplos de conexion:
echo   - IP: localhost    Puerto: 5000  (Servidor-1)
echo   - IP: 127.0.0.1    Puerto: 5002  (Servidor-2)
echo   - IP: localhost    Puerto: 5004  (Servidor-3)
echo   - IP: 127.0.0.1    Puerto: 5006  (Servidor-4)
echo.
echo Para iniciar mas clientes, ejecuta este script nuevamente.
echo.
pause
