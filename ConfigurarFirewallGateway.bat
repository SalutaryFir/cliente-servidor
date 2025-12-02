@echo off
chcp 65001 >nul
echo ========================================
echo   CONFIGURAR FIREWALL - API GATEWAY
echo   IP: 192.168.137.253
echo ========================================
echo.

echo Este script debe ejecutarse como ADMINISTRADOR
echo.
pause

echo Abriendo puertos para API Gateway Kong...
echo.

REM Kong API Gateway
echo [1/4] Kong Gateway Proxy (8000)...
powershell -Command "New-NetFirewallRule -DisplayName 'Kong Gateway Proxy' -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow" 2>nul
if errorlevel 1 (
    echo ⚠️  Regla ya existe o error al crear
) else (
    echo ✅ Puerto 8000 abierto
)

echo [2/4] Kong Admin API (8001)...
powershell -Command "New-NetFirewallRule -DisplayName 'Kong Admin API' -Direction Inbound -LocalPort 8001 -Protocol TCP -Action Allow" 2>nul
if errorlevel 1 (
    echo ⚠️  Regla ya existe o error al crear
) else (
    echo ✅ Puerto 8001 abierto
)

REM Web Admin
echo [3/4] Web Admin (5173)...
powershell -Command "New-NetFirewallRule -DisplayName 'Web Admin Frontend' -Direction Inbound -LocalPort 5173 -Protocol TCP -Action Allow" 2>nul
if errorlevel 1 (
    echo ⚠️  Regla ya existe o error al crear
) else (
    echo ✅ Puerto 5173 abierto
)

REM Permitir ping
echo [4/4] Permitir ICMP (ping)...
powershell -Command "New-NetFirewallRule -DisplayName 'Allow ICMPv4 Ping' -Direction Inbound -Protocol ICMPv4 -IcmpType 8 -Action Allow" 2>nul
if errorlevel 1 (
    echo ⚠️  Regla ya existe o error al crear
) else (
    echo ✅ Ping habilitado
)

echo.
echo ========================================
echo   CONFIGURACIÓN COMPLETADA
echo ========================================
echo.
echo Puertos abiertos:
echo   - 8000  (Kong Gateway Proxy)
echo   - 8001  (Kong Admin API)
echo   - 5173  (Web Admin)
echo   - ICMP  (Ping)
echo.
echo Para verificar las reglas:
echo   powershell -Command "Get-NetFirewallRule | Where-Object {$_.DisplayName -like '*Kong*' -or $_.DisplayName -like '*Web Admin*'}"
echo.
pause
