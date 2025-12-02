@echo off
chcp 65001 >nul
echo ========================================
echo   INICIAR CLIENTE DE PRUEBA
echo ========================================
echo.

cd cliente

echo [1/2] Verificando archivo JAR...
if not exist "target\cliente-0.0.1-SNAPSHOT.jar" (
    echo ❌ Error: Archivo JAR no encontrado
    echo Ejecuta primero: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo [2/2] Iniciando Cliente...
echo.
echo Ingresa los datos de conexión cuando se soliciten:
echo - IP del servidor (ejemplo: localhost, 192.168.1.10)
echo - Puerto (5000, 5002, 5004, 5006)
echo.

java -jar target\cliente-0.0.1-SNAPSHOT.jar

pause
