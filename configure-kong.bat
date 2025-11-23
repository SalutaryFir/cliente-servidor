@echo off
REM Script para configurar rutas del API Gateway (Kong) en Windows
REM Este script crea servicios y rutas para los 4 servidores de chat

echo ========================================
echo   Configurando API Gateway Kong
echo ========================================
echo.

REM URL del Admin API de Kong
set KONG_ADMIN_URL=http://localhost:8001

REM Verificar que Kong este disponible
echo Verificando conexion con Kong Admin API...
timeout /t 3 /nobreak >nul
curl -s %KONG_ADMIN_URL% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] No se pudo conectar a Kong
    echo Asegurate de que Kong este corriendo: docker-compose up -d kong
    pause
    exit /b 1
)
echo [OK] Kong esta disponible
echo.

echo ========================================
echo   Configurando Servidor 1
echo ========================================
REM Crear servicio para Servidor 1
curl -i -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-1" ^
    --data "url=http://host.docker.internal:8080" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000"

REM Crear ruta para Servidor 1
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-1/routes ^
    --data "name=route-server-1" ^
    --data "paths[]=/server1" ^
    --data "strip_path=true"

REM CORS para Servidor 1
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-1/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true"

REM Rate Limiting para Servidor 1
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-1/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000"

echo.
echo [OK] Servidor 1 configurado
echo.

echo ========================================
echo   Configurando Servidor 2
echo ========================================
REM Crear servicio para Servidor 2
curl -i -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-2" ^
    --data "url=http://host.docker.internal:8081" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000"

REM Crear ruta para Servidor 2
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-2/routes ^
    --data "name=route-server-2" ^
    --data "paths[]=/server2" ^
    --data "strip_path=true"

REM CORS para Servidor 2
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-2/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true"

REM Rate Limiting para Servidor 2
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-2/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000"

echo.
echo [OK] Servidor 2 configurado
echo.

echo ========================================
echo   Configurando Servidor 3
echo ========================================
REM Crear servicio para Servidor 3
curl -i -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-3" ^
    --data "url=http://host.docker.internal:8082" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000"

REM Crear ruta para Servidor 3
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-3/routes ^
    --data "name=route-server-3" ^
    --data "paths[]=/server3" ^
    --data "strip_path=true"

REM CORS para Servidor 3
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-3/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true"

REM Rate Limiting para Servidor 3
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-3/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000"

echo.
echo [OK] Servidor 3 configurado
echo.

echo ========================================
echo   Configurando Servidor 4
echo ========================================
REM Crear servicio para Servidor 4
curl -i -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-4" ^
    --data "url=http://host.docker.internal:8083" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000"

REM Crear ruta para Servidor 4
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-4/routes ^
    --data "name=route-server-4" ^
    --data "paths[]=/server4" ^
    --data "strip_path=true"

REM CORS para Servidor 4
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-4/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true"

REM Rate Limiting para Servidor 4
curl -i -X POST %KONG_ADMIN_URL%/services/chat-server-4/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000"

echo.
echo [OK] Servidor 4 configurado
echo.

echo ========================================
echo   CONFIGURACION COMPLETA
echo ========================================
echo.
echo Servicios y rutas creados en Kong:
echo   * Servidor 1: http://localhost:8000/server1/* -^> http://localhost:8080/*
echo   * Servidor 2: http://localhost:8000/server2/* -^> http://localhost:8081/*
echo   * Servidor 3: http://localhost:8000/server3/* -^> http://localhost:8082/*
echo   * Servidor 4: http://localhost:8000/server4/* -^> http://localhost:8083/*
echo.
echo Plugins habilitados:
echo   + CORS (Cross-Origin Resource Sharing)
echo   + Rate Limiting (100 req/min, 10000 req/hour)
echo.
echo API Gateway listo para usar!
echo.
echo ========================================
echo   PRUEBAS
echo ========================================
echo.
echo Prueba los endpoints con:
echo   curl http://localhost:8000/server1/api/v1/server/health
echo   curl http://localhost:8000/server2/api/v1/server/health
echo   curl http://localhost:8000/server3/api/v1/server/health
echo   curl http://localhost:8000/server4/api/v1/server/health
echo.
echo O accede a la web admin en: http://localhost:3000
echo.
pause
