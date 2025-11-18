@echo off
echo ========================================
echo   CLIENTE CHAT RMI - MODO REDE
echo ========================================
echo.
echo Compilando projeto...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilacao!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   CONFIGURACAO DO CLIENTE
echo ========================================
echo.
echo Digite o IP do servidor:
set /p SERVER_IP=

if "%SERVER_IP%"=="" (
    echo IP do servidor nao informado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO CLIENTE
echo ========================================
echo Conectando ao servidor: %SERVER_IP%
echo.
echo IMPORTANTE: Certifique-se de que o firewall permite
echo conexoes de saida nas portas 1099, 1098, 9876 e 9877
echo.

java -Djava.rmi.server.hostname=%SERVER_IP% -cp "target/classes" com.chatrmi.client.ChatClientGUI %SERVER_IP%

pause
