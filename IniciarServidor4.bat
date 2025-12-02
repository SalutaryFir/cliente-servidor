@echo off
chcp 65001 >nul
echo ========================================
echo   INICIAR SERVIDOR 4
echo ========================================
echo.

cd servidor

echo [1/2] Verificando archivo JAR...
if not exist "target\servidor-0.0.1-SNAPSHOT.jar" (
    echo ❌ Error: Archivo JAR no encontrado
    echo Ejecuta primero: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo [2/2] Iniciando Servidor 4...
echo.
echo Puerto Cliente:    5006
echo Puerto Federación: 5007
echo Puerto API REST:   8083
echo.

java -jar target\servidor-0.0.1-SNAPSHOT.jar ^
     --spring.profiles.active=server4 ^
     --chat.server.name=Servidor-4 ^
     --chat.server.client-port=5006 ^
     --chat.server.federation-port=5007 ^
     --server.port=8083

pause
