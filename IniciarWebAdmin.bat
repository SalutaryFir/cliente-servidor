@echo off
chcp 65001 >nul
echo ========================================
echo   INICIANDO WEB ADMIN
echo ========================================
echo.

cd web-admin

echo [1/2] Verificando dependencias...
if not exist "node_modules\" (
    echo Instalando dependencias de Node.js...
    call npm install
    if errorlevel 1 (
        echo Error al instalar dependencias
        pause
        exit /b 1
    )
)

echo [2/2] Iniciando servidor de desarrollo...
echo.
echo Web Admin estará disponible en: http://localhost:5173
echo.
call npm run dev

pause
