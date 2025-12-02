@echo off
echo ========================================
echo   CONFIGURAR PARA RED DISTRIBUIDA
echo ========================================
echo.
echo Este script te ayudara a configurar la arquitectura en 4 PCs
echo.
echo Requisitos previos:
echo   - Ejecuta "ipconfig" en cada PC para obtener su IP
echo   - Anota las IPs en un archivo
echo.
setlocal enabledelayedexpansion

REM Solicitar IPs
echo.
echo ========================================
echo   PASO 1: INGRESAR IPs DE LOS PCs
echo ========================================
echo.
set /p IP_PC1="IP del PC 1 (Web Admin): "
set /p IP_PC2="IP del PC 2 (Servidor+Cliente): "
set /p IP_PC3="IP del PC 3 (Servidor+2Clientes): "
set /p IP_PC4="IP del PC 4 (Servidor+2Clientes): "

echo.
echo ========================================
echo   CONFIRMANDO IPs
echo ========================================
echo.
echo PC 1 (Web Admin):    !IP_PC1!
echo PC 2 (Servidor):    !IP_PC2!:8081
echo PC 3 (Servidor):    !IP_PC3!:8082
echo PC 4 (Servidor):    !IP_PC4!:8083
echo.
choice /C SN /N /M "Continuar? (S/N): "
if errorlevel 2 goto :fin

echo.
echo ========================================
echo   PASO 2: CREAR ARCHIVO .env
echo ========================================
echo.

REM Crear archivo .env
(
echo # Web Admin - Configuracion Distribuida
echo # Generado automaticamente
echo.
echo VITE_SERVER_1_URL=http://!IP_PC2!:8081/api/v1/server
echo VITE_SERVER_2_URL=http://!IP_PC3!:8082/api/v1/server
echo VITE_SERVER_3_URL=http://!IP_PC4!:8083/api/v1/server
echo.
echo VITE_API_GATEWAY_URL=http://!IP_PC1!:8000
) > web-admin\.env

echo Archivo .env creado en web-admin\.env

echo.
echo ========================================
echo   PASO 3: SIGUIENTE PASO
echo ========================================
echo.
echo Ahora debes:
echo.
echo 1. En cada PC (2, 3, 4):
echo    - Abrir el archivo: servidor\src\main\resources\application-serverX.properties
echo    - Verificar que contenga: server.address=0.0.0.0
echo    - Asegurarse MySQL este corriendo localmente
echo.
echo 2. Ejecutar en cada PC:
echo    mvn clean package -DskipTests -pl servidor,comun,cliente
echo.
echo 3. Iniciar servidores en cada PC:
echo    - PC 2: java -jar servidor\target\servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server2
echo    - PC 3: java -jar servidor\target\servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server3
echo    - PC 4: java -jar servidor\target\servidor-0.0.1-SNAPSHOT.jar --spring.profiles.active=server4
echo.
echo 4. En PC 1:
echo    npm run dev
echo    O para produccion: npm run build ^&^& npm run preview
echo.
echo Para mas informacion, ver: CONFIGURACION-DISTRIBUIDA.md
echo.

:fin
pause

