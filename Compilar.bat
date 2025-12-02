@echo off
chcp 65001 >nul
echo ========================================
echo   COMPILAR PROYECTO
echo ========================================
echo.

echo [1/2] Compilando con Maven...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo ❌ Error en la compilación
    pause
    exit /b 1
)

echo.
echo ========================================
echo   COMPILACIÓN EXITOSA
echo ========================================
echo.
echo Archivos generados:
echo   - servidor\target\servidor-0.0.1-SNAPSHOT.jar
echo   - cliente\target\cliente-0.0.1-SNAPSHOT.jar
echo   - comun\target\comun-0.0.1-SNAPSHOT.jar
echo.
pause
