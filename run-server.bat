@echo off
echo Compilando projeto...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilacao!
    pause
    exit /b 1
)

echo.
echo Iniciando servidor...
java -cp "target/classes" com.chatrmi.server.ChatServer

pause

