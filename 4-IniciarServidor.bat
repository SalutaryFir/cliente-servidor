@echo off
REM ========================================
REM   4. INICIAR SERVIDOR
REM ========================================
REM Selecciona y inicia el servidor deseado
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ========================================
echo   INICIAR SERVIDOR
echo ========================================
echo.
echo Selecciona el servidor:
echo.
echo   1 - Servidor 1 (Puerto 8080, Federaci\u00f3n 5001)
echo   2 - Servidor 2 (Puerto 8081, Federaci\u00f3n 5002)
echo   3 - Servidor 3 (Puerto 8082, Federaci\u00f3n 5003)
echo   4 - Servidor 4 (Puerto 8083, Federaci\u00f3n 5004)
echo.
choice /C 1234 /N /M "Servidor: "

if errorlevel 4 goto :srv4
if errorlevel 3 goto :srv3
if errorlevel 2 goto :srv2
if errorlevel 1 goto :srv1
goto :fin

:srv1
set PROFILE=server1
set PORT=8080
set FED_PORT=5001
goto :compile

:srv2
set PROFILE=server2
set PORT=8081
set FED_PORT=5002
goto :compile

:srv3
set PROFILE=server3
set PORT=8082
set FED_PORT=5003
goto :compile

:srv4
set PROFILE=server4
set PORT=8083
set FED_PORT=5004
goto :compile

:compile
echo.
echo [1/3] Compilando..
call mvn clean package -DskipTests -pl servidor,comun
if errorlevel 1 (
    echo \u274c ERROR: Fall\u00f3 compilaci\u00f3n
    pause
    exit /b 1
)

echo \u2705 Compilado
echo.
echo [2/3] Iniciando !PROFILE!...
echo.
echo \ud83d\ude80 API REST:    http://localhost:!PORT!/api/v1/server
echo \ud83d\udd0d Federaci\u00f3n:  localhost:!FED_PORT!
echo.

echo [3/3] Ejecutando JAR...
echo Presiona Ctrl+C para detener
echo.

cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=!PROFILE!

:fin
exit /b 0
