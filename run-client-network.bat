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
echo Auto-detectando IP deste computador (cliente)...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set CLIENT_IP=%%a
    goto :found_client_ip
)
:found_client_ip
set CLIENT_IP=%CLIENT_IP: =%
if "%CLIENT_IP%"=="" (
    echo.
    echo Nao foi possivel detectar o IP automaticamente.
    echo Por favor, digite o IP deste computador (cliente):
    set /p CLIENT_IP=
) else (
    echo IP do cliente detectado: %CLIENT_IP%
)

if "%CLIENT_IP%"=="" (
    echo IP do cliente nao informado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO CLIENTE
echo ========================================
echo IP do servidor: %SERVER_IP%
echo IP do cliente: %CLIENT_IP%
echo.
echo IMPORTANTE: Certifique-se de que o firewall permite
echo conexoes de entrada e saida nas portas:
echo   - 1099 (RMI Registry - saida)
echo   - 1098 (RMI Server - saida)
echo   - Porta dinamica RMI (entrada - para callbacks)
echo   - 9876 (UDP File - saida)
echo   - 9877 (UDP Download - saida)
echo.
echo Para permitir no firewall do Windows, execute como Administrador:
echo   netsh advfirewall firewall add rule name="Chat RMI Client" dir=in action=allow protocol=TCP
echo   netsh advfirewall firewall add rule name="Chat RMI Client" dir=out action=allow protocol=TCP localport=1099,1098
echo   netsh advfirewall firewall add rule name="Chat UDP Client" dir=out action=allow protocol=UDP localport=9876,9877
echo.

java -Djava.rmi.server.hostname=%CLIENT_IP% -cp "target/classes" com.chatrmi.client.ChatClientGUI %SERVER_IP%

pause
