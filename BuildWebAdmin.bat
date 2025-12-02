@echo off
echo ========================================
echo   WEB ADMIN - COMPILAR PARA PRODUCCION
echo ========================================
echo.

REM Cambiar al directorio raiz
cd /d "%~dp0"

REM Verificar si Node.js esta instalado
echo [1/3] Verificando Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js no esta instalado
    echo Por favor instala Node.js desde: https://nodejs.org/
    echo.
    pause
    exit /b 1
)
echo OK - Node.js detectado

REM Instalar dependencias
echo [2/3] Instalando dependencias (npm install)...
cd web-admin
call npm install
if errorlevel 1 (
    echo ERROR: Fallo al instalar dependencias
    pause
    exit /b 1
)

echo.
echo [3/3] Compilando para produccion...
call npm run build
if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo.
echo ========================================
echo   COMPILACION COMPLETADA
echo ========================================
echo.
echo Los archivos compilados estan en:
echo   web-admin\dist\
echo.
echo Puedes servir esta aplicacion con:
echo   1. Servidor HTTP local (python, node, etc)
echo   2. Docker (incluye nginx)
echo   3. Servidor web (Apache, Nginx, IIS)
echo.
echo Para usar Docker:
echo   docker build -t chat-admin .
echo   docker run -p 80:80 chat-admin
echo.
pause

