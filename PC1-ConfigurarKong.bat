@echo off
REM ========================================
REM   CONFIGURAR KONG PARA UN SERVIDOR
REM ========================================
echo.
echo ========================================
echo   CONFIGURAR API GATEWAY (Kong)
echo   Para Servidor Individual
echo ========================================
echo.

setlocal enabledelayedexpansion

REM ========================================
REM PASO 1: SOLICITAR DATOS
REM ========================================
echo Ingresa los datos del servidor:
echo.
set /p SERVER_NUM="Numero de servidor (2, 3, o 4): "
set /p SERVER_IP="IP del PC donde esta el servidor: "

REM Calcular puerto basado en numero de servidor
set /a HTTP_PORT=8080+!SERVER_NUM!-1
set /a FED_PORT=5000+!SERVER_NUM!

echo.
echo Configuracion:
echo   Servidor: !SERVER_NUM!
echo   IP: !SERVER_IP!
echo   Puerto HTTP: !HTTP_PORT!
echo   Puerto Federacion: !FED_PORT!
echo.
choice /C SN /N /M "Continuar? (S/N): "
if errorlevel 2 goto :fin

REM ========================================
REM PASO 2: VERIFICAR KONG
REM ========================================
echo.
echo [1/4] Verificando Kong...
timeout /t 2 /nobreak >nul
curl -s http://localhost:8001 >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Kong no esta disponible
    echo.
    echo Inicia Kong primero:
    echo   docker-compose -f docker-compose-api-gateway.yml up -d kong
    echo.
    pause
    exit /b 1
)
echo [OK] Kong disponible

REM ========================================
REM PASO 3: LIMPIAR CONFIGURACION ANTERIOR
REM ========================================
echo.
echo [2/4] Limpiando configuracion anterior...
curl -s -X DELETE http://localhost:8001/services/chat-server-!SERVER_NUM! >nul 2>&1
echo [OK] Limpiado

REM ========================================
REM PASO 4: CREAR SERVICIO
REM ========================================
echo.
echo [3/4] Creando servicio en Kong...
curl -s -X POST http://localhost:8001/services ^
    --data "name=chat-server-!SERVER_NUM!" ^
    --data "url=http://!SERVER_IP!:!HTTP_PORT!" ^
    --data "retries=3" ^
    --data "connect_timeout=60000" ^
    --data "write_timeout=60000" ^
    --data "read_timeout=60000" >nul

if errorlevel 1 (
    echo [ERROR] Fallo al crear servicio
    pause
    exit /b 1
)
echo [OK] Servicio creado

REM ========================================
REM PASO 5: CREAR RUTA
REM ========================================
echo.
echo [4/4] Creando ruta...
curl -s -X POST http://localhost:8001/services/chat-server-!SERVER_NUM!/routes ^
    --data "name=route-server-!SERVER_NUM!" ^
    --data "paths[]=/server!SERVER_NUM!" ^
    --data "strip_path=true" >nul

REM Habilitar CORS
curl -s -X POST http://localhost:8001/services/chat-server-!SERVER_NUM!/plugins ^
    --data "name=cors" ^
    --data "config.origins=*" ^
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" ^
    --data "config.headers=Accept,Content-Type,Authorization" ^
    --data "config.credentials=true" >nul

REM Rate Limiting
curl -s -X POST http://localhost:8001/services/chat-server-!SERVER_NUM!/plugins ^
    --data "name=rate-limiting" ^
    --data "config.minute=100" ^
    --data "config.hour=10000" >nul

echo [OK] Ruta configurada

REM ========================================
REM VERIFICACION
REM ========================================
echo.
echo ========================================
echo   CONFIGURACION COMPLETADA
echo ========================================
echo.
echo Ruta creada:
echo   http://localhost:8000/server!SERVER_NUM!/* --^> http://!SERVER_IP!:!HTTP_PORT!/*
echo.
echo Plugins habilitados:
echo   + CORS
echo   + Rate Limiting (100/min, 10000/hora)
echo.
echo ========================================
echo   PRUEBA
echo ========================================
echo.
echo Probando conexion...
curl -s http://localhost:8000/server!SERVER_NUM!/api/v1/server/health

if errorlevel 1 (
    echo.
    echo [ADVERTENCIA] No se pudo conectar
    echo.
    echo Verifica:
    echo   1. Servidor esta corriendo en !SERVER_IP!:!HTTP_PORT!
    echo   2. Firewall permite el puerto
    echo   3. Ambos PCs en misma red
) else (
    echo.
    echo [OK] Conexion exitosa!
)

echo.

:fin
pause
