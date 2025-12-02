@echo off
echo ========================================
echo   INICIAR SERVIDOR DISTRIBUIDO
echo ========================================
echo.
echo Este script se ejecuta EN CADA PC (2, 3 o 4)
echo.

REM Detectar cual servidor ejecutar
setlocal enabledelayedexpansion

:menu
echo.
echo Cual servidor deseas iniciar?
echo.
echo   1 - Servidor 2 (PC 2)  - Puerto 8081
echo   2 - Servidor 3 (PC 3)  - Puerto 8082
echo   3 - Servidor 4 (PC 4)  - Puerto 8083
echo.
choice /C 123 /N /M "Selecciona una opcion: "

if errorlevel 3 goto :server4
if errorlevel 2 goto :server3
if errorlevel 1 goto :server2
goto :menu

:server2
echo.
echo Iniciando Servidor-2...
cd /d "%~dp0"
call mvn clean package -DskipTests -pl servidor,comun
if errorlevel 1 (
    echo ERROR: Fallo compilacion
    pause
    exit /b 1
)
echo.
echo Iniciando JAR...
cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server2
goto :fin

:server3
echo.
echo Iniciando Servidor-3...
cd /d "%~dp0"
call mvn clean package -DskipTests -pl servidor,comun
if errorlevel 1 (
    echo ERROR: Fallo compilacion
    pause
    exit /b 1
)
echo.
echo Iniciando JAR...
cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server3
goto :fin

:server4
echo.
echo Iniciando Servidor-4...
cd /d "%~dp0"
call mvn clean package -DskipTests -pl servidor,comun
if errorlevel 1 (
    echo ERROR: Fallo compilacion
    pause
    exit /b 1
)
echo.
echo Iniciando JAR...
cd servidor\target
java -jar servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server4
goto :fin

:fin
echo.
pause

