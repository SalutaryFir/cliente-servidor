@echo off
chcp 65001 >nul
echo ========================================
echo   INICIAR SERVIDOR 1 (Coordinador)
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

echo [2/2] Iniciando Servidor 1...
echo.
echo Puerto Cliente:    5000
echo Puerto Federación: 5001
echo Puerto API REST:   8080
echo.

java -jar target\servidor-0.0.1-SNAPSHOT.jar ^
     --spring.profiles.active=server1 ^
     --chat.server.name=Servidor-1 ^
     --chat.server.client-port=5000 ^
     --chat.server.federation-port=5001 ^
     --server.port=8080

pause
