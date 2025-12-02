@echo off
chcp 65001 >nul
echo ========================================
echo   CONFIGURAR RUTAS DE KONG
echo ========================================
echo.

echo REQUISITO: Kong debe estar ejecutándose
echo Ejecuta primero: IniciarKong.bat
echo.
pause

echo [1/4] Verificando Kong...
curl -s http://localhost:8001/ >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Kong no está ejecutándose
    echo Ejecuta primero: IniciarKong.bat
    pause
    exit /b 1
)

echo ✅ Kong está ejecutándose
echo.

echo [2/4] Creando servicio server1...
curl -i -X POST http://localhost:8001/services/ ^
  --data "name=server1" ^
  --data "url=http://host.docker.internal:8080"

echo.
echo [3/4] Creando ruta /server1...
curl -i -X POST http://localhost:8001/services/server1/routes ^
  --data "paths[]=/server1" ^
  --data "strip_path=true"

echo.
echo.
echo [4/4] Creando rutas para server2, server3, server4...

REM Server 2
curl -s -X POST http://localhost:8001/services/ ^
  --data "name=server2" ^
  --data "url=http://host.docker.internal:8081"
curl -s -X POST http://localhost:8001/services/server2/routes ^
  --data "paths[]=/server2" ^
  --data "strip_path=true"

REM Server 3
curl -s -X POST http://localhost:8001/services/ ^
  --data "name=server3" ^
  --data "url=http://host.docker.internal:8082"
curl -s -X POST http://localhost:8001/services/server3/routes ^
  --data "paths[]=/server3" ^
  --data "strip_path=true"

REM Server 4
curl -s -X POST http://localhost:8001/services/ ^
  --data "name=server4" ^
  --data "url=http://host.docker.internal:8083"
curl -s -X POST http://localhost:8001/services/server4/routes ^
  --data "paths[]=/server4" ^
  --data "strip_path=true"

echo.
echo ========================================
echo   RUTAS CONFIGURADAS
echo ========================================
echo.
echo Rutas creadas:
echo   - http://localhost:8000/server1 → localhost:8080
echo   - http://localhost:8000/server2 → localhost:8081
echo   - http://localhost:8000/server3 → localhost:8082
echo   - http://localhost:8000/server4 → localhost:8083
echo.
pause
