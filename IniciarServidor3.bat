@echo off
chcp 65001 >nul
echo ========================================
echo   INICIAR SERVIDOR 3
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

echo [2/2] Iniciando Servidor 3...
echo.
echo Puerto Cliente:    5004
echo Puerto Federación: 5005
echo Puerto API REST:   8082
echo.

java -jar target\servidor-0.0.1-SNAPSHOT.jar ^
     --spring.profiles.active=server3 ^
     --chat.server.name=Servidor-3 ^
     --chat.server.client-port=5004 ^
     --chat.server.federation-port=5005 ^
     --server.port=8082

pause
