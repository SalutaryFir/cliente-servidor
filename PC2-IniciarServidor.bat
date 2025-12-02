@echo off
REM ========================================
REM   INICIAR SERVIDOR EN PC 2/3/4
REM ========================================
echo.
echo ========================================
echo   INICIAR SERVIDOR DE CHAT
echo ========================================
echo.

cd /d "%~dp0"

REM ========================================
REM SELECCIONAR SERVIDOR
REM ========================================
echo Selecciona el servidor a iniciar:
echo.
echo   2 - Servidor 2 (Puerto 8081)
echo   3 - Servidor 3 (Puerto 8082)
echo   4 - Servidor 4 (Puerto 8083)
echo.
choice /C 234 /N /M "Servidor: "

if errorlevel 3 (
    set PROFILE=server4
    set PORT=8083
    set FED_PORT=5004
    goto :compile
)
if errorlevel 2 (
    set PROFILE=server3
    set PORT=8082
    set FED_PORT=5003
    goto :compile
)
if errorlevel 1 (
    set PROFILE=server2
    set PORT=8081
    set FED_PORT=5002
    goto :compile
)

:compile
echo.
echo ========================================
echo   CONFIGURACION
echo ========================================
echo.
echo Perfil: %PROFILE%
echo Puerto API: %PORT%
echo Puerto Federacion: %FED_PORT%
echo Puerto Clientes: 9999
echo.

REM ========================================
REM VERIFICAR MYSQL
REM ========================================
echo [1/4] Verificando MySQL...
sc query MySQL >nul 2>&1
if errorlevel 1 (
    echo [ADVERTENCIA] MySQL puede no estar corriendo
    echo Intenta: net start MySQL
    echo.
    choice /C SN /N /M "Continuar de todos modos? (S/N): "
    if errorlevel 2 goto :fin
)
echo [OK] MySQL verificado

REM ========================================
REM COMPILAR
REM ========================================
echo.
echo [2/4] Compilando proyecto...
call mvn clean package -DskipTests -pl servidor,comun
if errorlevel 1 (
    echo [ERROR] Fallo compilacion
    pause
    exit /b 1
)
echo [OK] Compilado

REM ========================================
REM VERIFICAR IP
REM ========================================
echo.
echo [3/4] Verificando configuracion de red...
echo.
echo IMPORTANTE: Para que PC 1 pueda conectarse,
echo el archivo application-%PROFILE%.properties debe tener:
echo   server.address=0.0.0.0
echo.
echo Tu IP actual:
ipconfig | findstr /i "IPv4"
echo.
echo Anota esta IP para configurar Kong en PC 1
echo.
pause

REM ========================================
REM INICIAR SERVIDOR
REM ========================================
echo.
echo [4/4] Iniciando %PROFILE%...
echo.
echo ========================================
echo   SERVIDOR CORRIENDO
echo ========================================
echo.
echo API REST: http://localhost:%PORT%/api/v1/server
echo Federacion: localhost:%FED_PORT%
echo Clientes TCP: localhost:9999
echo.
echo Presiona Ctrl+C para detener
echo.

cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%

:fin
echo.
echo Servidor detenido
pause
