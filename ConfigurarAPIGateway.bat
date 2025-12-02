@echo off
echo ========================================
echo   CONFIGURAR API GATEWAY (Kong)
echo   Para Arquitectura Distribuida
echo ========================================
echo.
echo Este script configura Kong para enrutar trafico
echo a los servidores distribuidos en diferentes PCs
echo.

setlocal enabledelayedexpansion

REM URL del Admin API de Kong
set KONG_ADMIN_URL=http://localhost:8001

REM ========================================
REM PASO 1: SOLICITAR IPs
REM ========================================
echo PASO 1: Ingresar IPs de los servidores
echo ========================================
echo.
set /p IP_SERVER2="IP del Servidor-2 (PC 2): "
set /p IP_SERVER3="IP del Servidor-3 (PC 3): "
set /p IP_SERVER4="IP del Servidor-4 (PC 4): "

echo.
echo IPs configuradas:
echo   Servidor-2: !IP_SERVER2!:8081
echo   Servidor-3: !IP_SERVER3!:8082
echo   Servidor-4: !IP_SERVER4!:8083
echo.
choice /C SN /N /M "Continuar? (S/N): "
if errorlevel 2 goto :fin

REM ========================================
REM PASO 2: VERIFICAR KONG
REM ========================================
echo.
echo PASO 2: Verificando Kong
echo ========================================
timeout /t 2 /nobreak >nul
curl -s %KONG_ADMIN_URL% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] No se pudo conectar a Kong en %KONG_ADMIN_URL%
    echo.
    echo Asegurate de:
    echo   1. Docker Desktop este corriendo
    echo   2. Kong este iniciado: docker-compose -f docker-compose-api-gateway.yml up -d kong
    echo.
    pause
    exit /b 1
)
echo [OK] Kong esta disponible
echo.

REM ========================================
REM PASO 3: LIMPIAR CONFIGURACION ANTERIOR
REM ========================================
echo PASO 3: Limpiando configuracion anterior
echo ========================================
curl -s -X DELETE %KONG_ADMIN_URL%/services/chat-server-2 >nul 2>&1
curl -s -X DELETE %KONG_ADMIN_URL%/services/chat-server-3 >nul 2>&1
curl -s -X DELETE %KONG_ADMIN_URL%/services/chat-server-4 >nul 2>&1
echo [OK] Configuracion anterior eliminada
echo.

REM ========================================
REM PASO 4: CONFIGURAR SERVIDOR 2
REM ========================================
echo PASO 4: Configurando Servidor-2
echo ========================================
echo Creando servicio para Servidor-2...
curl -s -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-2" ^
    --data "url=http://!IP_SERVER2!:8081" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000" >nul

echo Creando ruta...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-2/routes ^
    --data "name=route-server-2" ^
    --data "paths[]=/server2" ^
    --data "strip_path=true" >nul

echo Habilitando CORS...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-2/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true" >nul

echo Habilitando Rate Limiting...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-2/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000" >nul

echo [OK] Servidor-2 configurado
echo.

REM ========================================
REM PASO 5: CONFIGURAR SERVIDOR 3
REM ========================================
echo PASO 5: Configurando Servidor-3
echo ========================================
echo Creando servicio para Servidor-3...
curl -s -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-3" ^
    --data "url=http://!IP_SERVER3!:8082" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000" >nul

echo Creando ruta...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-3/routes ^
    --data "name=route-server-3" ^
    --data "paths[]=/server3" ^
    --data "strip_path=true" >nul

echo Habilitando CORS...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-3/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true" >nul

echo Habilitando Rate Limiting...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-3/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000" >nul

echo [OK] Servidor-3 configurado
echo.

REM ========================================
REM PASO 6: CONFIGURAR SERVIDOR 4
REM ========================================
echo PASO 6: Configurando Servidor-4
echo ========================================
echo Creando servicio para Servidor-4...
curl -s -X POST %KONG_ADMIN_URL%/services ^
    --data "name=chat-server-4" ^
    --data "url=http://!IP_SERVER4!:8083" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000" >nul

echo Creando ruta...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-4/routes ^
    --data "name=route-server-4" ^
    --data "paths[]=/server4" ^
    --data "strip_path=true" >nul

echo Habilitando CORS...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-4/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true" >nul

echo Habilitando Rate Limiting...
curl -s -X POST %KONG_ADMIN_URL%/services/chat-server-4/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000" >nul

echo [OK] Servidor-4 configurado
echo.

REM ========================================
REM PASO 7: GUARDAR CONFIGURACION
REM ========================================
echo PASO 7: Guardando configuracion
echo ========================================

REM Crear archivo de configuracion
(
echo {
echo   "servers": [
echo     {
echo       "id": "server2",
echo       "name": "Servidor-2",
echo       "ip": "!IP_SERVER2!",
echo       "apiUrl": "http://!IP_SERVER2!:8081/api/v1/server",
echo       "gatewayRoute": "http://localhost:8000/server2/api/v1/server",
echo       "httpPort": 8081,
echo       "federationPort": 5002
echo     },
echo     {
echo       "id": "server3",
echo       "name": "Servidor-3",
echo       "ip": "!IP_SERVER3!",
echo       "apiUrl": "http://!IP_SERVER3!:8082/api/v1/server",
echo       "gatewayRoute": "http://localhost:8000/server3/api/v1/server",
echo       "httpPort": 8082,
echo       "federationPort": 5003
echo     },
echo     {
echo       "id": "server4",
echo       "name": "Servidor-4",
echo       "ip": "!IP_SERVER4!",
echo       "apiUrl": "http://!IP_SERVER4!:8083/api/v1/server",
echo       "gatewayRoute": "http://localhost:8000/server4/api/v1/server",
echo       "httpPort": 8083,
echo       "federationPort": 5004
echo     }
echo   ],
echo   "apiGateway": {
echo     "enabled": true,
echo     "url": "http://localhost:8000"
echo   }
echo }
) > api-config-distributed.json

echo [OK] Configuracion guardada en: api-config-distributed.json
echo.

REM Actualizar .env del web-admin
(
echo # API Gateway - Configuracion Distribuida
echo # Generado automaticamente por ConfigurarAPIGateway.bat
echo.
echo # Usar API Gateway centralizado
echo VITE_API_GATEWAY_URL=http://localhost:8000
echo VITE_USE_GATEWAY=true
echo.
echo # IPs directas ^(fallback^)
echo VITE_SERVER_1_URL=http://!IP_SERVER2!:8081/api/v1/server
echo VITE_SERVER_2_URL=http://!IP_SERVER3!:8082/api/v1/server
echo VITE_SERVER_3_URL=http://!IP_SERVER4!:8083/api/v1/server
) > web-admin\.env

echo [OK] Archivo .env actualizado en web-admin
echo.

REM ========================================
REM RESUMEN FINAL
REM ========================================
echo.
echo ========================================
echo   CONFIGURACION COMPLETADA
echo ========================================
echo.
echo Rutas configuradas en API Gateway:
echo   * http://localhost:8000/server2/* --^> http://!IP_SERVER2!:8081/*
echo   * http://localhost:8000/server3/* --^> http://!IP_SERVER3!:8082/*
echo   * http://localhost:8000/server4/* --^> http://!IP_SERVER4!:8083/*
echo.
echo Plugins habilitados:
echo   + CORS ^(Cross-Origin Resource Sharing^)
echo   + Rate Limiting ^(100 req/min, 10000 req/hour^)
echo.
echo Archivos actualizados:
echo   + api-config-distributed.json
echo   + web-admin\.env
echo.
echo ========================================
echo   PRUEBAS
echo ========================================
echo.
echo Prueba los endpoints:
echo   curl http://localhost:8000/server2/api/v1/server/health
echo   curl http://localhost:8000/server3/api/v1/server/health
echo   curl http://localhost:8000/server4/api/v1/server/health
echo.
echo Web Admin:
echo   El web admin ya esta configurado para usar el API Gateway
echo   Solo inicia: IniciarWebAdmin.bat
echo.

:fin
pause
