@echo off
echo ========================================
echo   INICIADOR COMPLETO - RED DE CHAT
echo ========================================
echo.
echo Este script inicia SERVIDORES y CLIENTES juntos.
echo.
echo Si prefieres iniciarlos por separado:
echo   1. IniciarServidores.bat  - Solo servidores
echo   2. IniciarClientes.bat    - Solo clientes
echo.
echo Presiona una tecla para continuar con inicio completo...
pause >nul

REM Matar procesos Java previos
echo.
echo [1/2] Cerrando procesos Java anteriores...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 >nul

REM Iniciar servidores
echo [2/2] Iniciando servidores...
call IniciarServidores.bat

REM Preguntar si desea iniciar clientes
echo.
echo ========================================
echo Deseas iniciar clientes ahora? (S/N)
choice /C SN /N /M "Presiona S para Si o N para No: "
if errorlevel 2 goto :fin
if errorlevel 1 call IniciarClientes.bat

:fin
echo.
echo Script completado.
pause