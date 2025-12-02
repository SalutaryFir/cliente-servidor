@echo off
chcp 65001 >nul
echo ========================================
echo   INICIAR SERVIDOR 2
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

echo [2/2] Iniciando Servidor 2...
echo.
echo Puerto Cliente:    5002
echo Puerto Federación: 5003
echo Puerto API REST:   8081
echo.

java -jar target\servidor-0.0.1-SNAPSHOT.jar ^
     --spring.profiles.active=server2 ^
     --chat.server.name=Servidor-2 ^
     --chat.server.client-port=5002 ^
     --chat.server.federation-port=5003 ^
     --server.port=8081

pause
