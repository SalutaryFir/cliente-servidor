@echo off
echo ========================================
echo   INICIANDO WEB ADMIN
echo ========================================
echo.

REM Cambiar al directorio raiz
cd /d "%~dp0"

REM Verificar si Node.js esta instalado
echo [1/4] Verificando Node.js...
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
echo [2/4] Instalando dependencias (npm install)...
cd web-admin
call npm install
if errorlevel 1 (
    echo ERROR: Fallo al instalar dependencias
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO SERVIDOR WEB ADMIN
echo ========================================
echo.
echo [3/4] Compilando y sirviendo aplicacion...
echo.
echo El web admin estara disponible en: http://localhost:5173
echo.
echo [4/4] Iniciando servidor de desarrollo...
call npm run dev

:fin
echo.
echo Script completado.
pause

