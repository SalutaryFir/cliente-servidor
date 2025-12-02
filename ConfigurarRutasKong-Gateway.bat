@echo off
chcp 65001 >nul
echo ========================================
echo   CONFIGURAR RUTAS DE KONG - API GATEWAY
echo   IP Gateway: 192.168.137.253
echo ========================================
echo.

REM =====================================================
REM CONFIGURACIÓN DE IPS DE SERVIDORES
REM Modifica estas variables según tu red
REM =====================================================
set SERVER1_IP=192.168.137.253
set SERVER2_IP=192.168.137.180
set SERVER3_IP=192.168.147.69
set SERVER4_IP=192.168.137.253

echo IPs configuradas:
echo   Server 1: %SERVER1_IP%:8080
echo   Server 2: %SERVER2_IP%:8081
echo   Server 3: %SERVER3_IP%:8082
echo   Server 4: %SERVER4_IP%:8083
echo.

echo REQUISITO: Kong debe estar ejecutándose
echo Ejecuta primero: IniciarKong.bat
echo.
pause

echo [1/9] Verificando Kong...
curl -s http://192.168.137.253:8001/ >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Kong no está ejecutándose
    echo Ejecuta primero: IniciarKong.bat
    pause
    exit /b 1
)

echo ✅ Kong está ejecutándose
echo.

echo ========================================
echo   ELIMINANDO RUTAS EXISTENTES
echo ========================================
echo.

echo Eliminando rutas anteriores (si existen)...
curl -s -X DELETE http://192.168.137.253:8001/services/server1 2>nul
curl -s -X DELETE http://192.168.137.253:8001/services/server2 2>nul
curl -s -X DELETE http://192.168.137.253:8001/services/server3 2>nul
curl -s -X DELETE http://192.168.137.253:8001/services/server4 2>nul

echo.
echo ========================================
echo   CREANDO SERVICIOS Y RUTAS
echo ========================================
echo.

echo [2/9] Creando servicio server1...
curl -i -X POST http://192.168.137.253:8001/services/ ^
  --data "name=server1" ^
  --data "url=http://%SERVER1_IP%:8080"

echo.
echo [3/9] Creando ruta /server1...
curl -i -X POST http://192.168.137.253:8001/services/server1/routes ^
  --data "paths[]=/server1" ^
  --data "strip_path=true"

echo.
echo ========================================
echo [4/9] Creando servicio server2...
curl -i -X POST http://192.168.137.253:8001/services/ ^
  --data "name=server2" ^
  --data "url=http://%SERVER2_IP%:8081"

echo.
echo [5/9] Creando ruta /server2...
curl -i -X POST http://192.168.137.253:8001/services/server2/routes ^
  --data "paths[]=/server2" ^
  --data "strip_path=true"

echo.
echo ========================================
echo [6/9] Creando servicio server3...
curl -i -X POST http://192.168.137.253:8001/services/ ^
  --data "name=server3" ^
  --data "url=http://%SERVER3_IP%:8082"

echo.
echo [7/9] Creando ruta /server3...
curl -i -X POST http://192.168.137.253:8001/services/server3/routes ^
  --data "paths[]=/server3" ^
  --data "strip_path=true"

echo.
echo ========================================
echo [8/9] Creando servicio server4...
curl -i -X POST http://192.168.137.253:8001/services/ ^
  --data "name=server4" ^
  --data "url=http://%SERVER4_IP%:8083"

echo.
echo [9/9] Creando ruta /server4...
curl -i -X POST http://192.168.137.253:8001/services/server4/routes ^
  --data "paths[]=/server4" ^
  --data "strip_path=true"

echo.
echo ========================================
echo   RUTAS CONFIGURADAS CORRECTAMENTE
echo ========================================
echo.
echo Gateway API: http://192.168.137.253:8000
echo.
echo Rutas creadas:
echo   - http://192.168.137.253:8000/server1 → %SERVER1_IP%:8080
echo   - http://192.168.137.253:8000/server2 → %SERVER2_IP%:8081
echo   - http://192.168.137.253:8000/server3 → %SERVER3_IP%:8082
echo   - http://192.168.137.253:8000/server4 → %SERVER4_IP%:8083
echo.
echo ========================================
echo   VERIFICACIÓN
echo ========================================
echo.
echo Puedes verificar las rutas con:
echo   curl http://192.168.137.253:8001/services
echo.
echo Puedes probar las rutas con:
echo   curl http://192.168.137.253:8000/server1/api/v1/server/health
echo.
pause
