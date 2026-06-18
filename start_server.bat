@echo off
echo --- Iniciando Sistema TowerDefense ---

:: 1. Intentar arrancar MariaDB en segundo plano
echo [1/4] Verificando Base de Datos...
netstat -ano | findstr :3306 >nul
if %errorlevel% neq 0 (
    echo MariaDB no esta corriendo. Arrancando MariaDB...
    start /min "" "C:\Program Files\MariaDB 12.2\bin\mysqld.exe"
    echo Esperando 5 segundos a que MariaDB se inicie...
    timeout /t 5 >nul
) else (
    echo MariaDB ya esta corriendo en el puerto 3306.
)

:: 2. Liberar el puerto 8080 si está ocupado por una instancia previa del servidor
echo [2/4] Liberando el puerto 8080 si esta en uso...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    echo Deteniendo proceso antiguo en puerto 8080 con PID %%a...
    taskkill /f /pid %%a >nul 2>&1
)

:: 3. Ir a la carpeta del proyecto
echo [3/4] Entrando en la carpeta del proyecto...
cd /d "%~dp0"

:: 4. Lanzar el servidor de Java
echo [4/4] Arrancando servidor... (No cierres esta ventana)
mvn compile exec:java
