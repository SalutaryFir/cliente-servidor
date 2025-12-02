@echo off
REM ========================================
REM   5. INICIAR CLIENTE
REM ========================================
REM Inicia cliente de chat
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ========================================
echo   INICIAR CLIENTE
echo ========================================
echo.

:compilar
echo [1/2] Compilando cliente...
call mvn clean package -DskipTests -pl cliente,comun
if errorlevel 1 (
    echo \u274c ERROR: Fall\u00f3 compilaci\u00f3n
    pause
    exit /b 1
)

echo \u2705 Compilado
echo.
echo [2/2] Iniciando cliente...
echo.
echo \u2139\ufe0f  Ingresa IP y puerto del servidor
echo.
echo Ejemplos:
echo   - localhost:9999
echo   - 127.0.0.1:9999
echo   - 192.168.1.x:9999
echo.

cd cliente\target
java -jar cliente-0.0.1-SNAPSHOT.jar

echo.
echo \u00bfOtro cliente? (S/N)
choice /C SN /N /M "Respuesta: "
if errorlevel 2 goto :fin
if errorlevel 1 goto :compilar

:fin
echo.
echo Cliente cerrado
exit /b 0
