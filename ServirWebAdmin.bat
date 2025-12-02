@echo off
echo ========================================
echo   SIRVIENDO WEB ADMIN (Produccion)
echo ========================================
echo.

REM Cambiar al directorio raiz
cd /d "%~dp0"

REM Verificar si dist existe
if not exist "web-admin\dist" (
    echo ERROR: No se encontro web-admin\dist
    echo Primero ejecuta: BuildWebAdmin.bat
    echo.
    pause
    exit /b 1
)

echo [1/2] Verificando Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js no esta instalado
    echo Por favor instala Node.js desde: https://nodejs.org/
    echo.
    pause
    exit /b 1
)
echo OK - Node.js detectado

echo.
echo [2/2] Instalando y usando servidor HTTP...
echo.
echo Instalando 'http-server' globalmente...
call npm install -g http-server >nul 2>&1

echo.
echo ========================================
echo   SERVIDOR WEB ADMIN EN EJECUCION
echo ========================================
echo.
echo El web admin estara disponible en:
echo   http://localhost:8888
echo   http://127.0.0.1:8888
echo.
echo Presiona Ctrl+C para detener el servidor
echo.

cd web-admin\dist
http-server -p 8888

:fin
echo.
echo Servidor detenido.
pause

