@echo off
REM ========================================
REM   1. INICIAR APLICATIVO WEB
REM ========================================
REM Inicia el dashboard web (desarrollo o producci\u00f3n)
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ========================================
echo   INICIAR APLICATIVO WEB
echo ========================================
echo.
echo Selecciona el modo:
echo.
echo   1 - Desarrollo (npm run dev - puerto 5173)
echo   2 - Compilar para Producci\u00f3n (npm run build)
echo   3 - Servir Compilado (puerto 8888)
echo.
choice /C 123 /N /M "Opci\u00f3n: "

if errorlevel 3 goto :produccion_servir
if errorlevel 2 goto :produccion_build
if errorlevel 1 goto :desarrollo
goto :fin

:desarrollo
echo.
echo [1/3] Verificando Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo \u274c ERROR: Node.js no instalado
    echo Descarga desde: https://nodejs.org/
    pause
    exit /b 1
)

echo [2/3] Instalando dependencias...
cd web-admin
call npm install >nul 2>&1

echo [3/3] Iniciando servidor...
echo.
echo \ud83d\ude80 Web Admin: http://localhost:5173
echo \ud83d\udcc2 Presiona Ctrl+C para detener
echo.
call npm run dev
goto :fin

:produccion_build
echo.
echo [1/2] Verificando Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo \u274c ERROR: Node.js no instalado
    pause
    exit /b 1
)

echo [2/2] Compilando...
cd web-admin
call npm install >nul 2>&1
call npm run build

if errorlevel 1 (
    echo \u274c ERROR: Fall\u00f3 compilaci\u00f3n
    pause
    exit /b 1
)

echo.
echo \u2705 Compilaci\u00f3n completada
echo \ud83d\udcc2 Archivos en: web-admin\dist\
pause
goto :fin

:produccion_servir
echo.
if not exist "web-admin\dist" (
    echo \u274c ERROR: No existe web-admin\dist
    echo Primero compila: selecciona opci\u00f3n 2
    pause
    exit /b 1
)

echo Instalando http-server...
call npm install -g http-server >nul 2>&1

echo.
echo \ud83d\ude80 Web Admin: http://localhost:8888
echo \ud83d\udcc2 Presiona Ctrl+C para detener
echo.
cd web-admin\dist
http-server -p 8888

:fin
exit /b 0
