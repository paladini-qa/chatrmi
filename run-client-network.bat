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

echo Verificando se as classes foram compiladas...
if not exist "target\classes\com\chatrmi\client\ChatClientGUI.class" (
    echo [ERRO] Classe ChatClientGUI nao encontrada!
    echo Verifique se a compilacao foi bem-sucedida.
    pause
    exit /b 1
)
echo [OK] Classes encontradas.

echo.
echo Iniciando cliente Java...
echo Se o cliente fechar imediatamente, verifique os erros abaixo.
echo.

java -Djava.rmi.server.hostname=%CLIENT_IP% -cp "target/classes" com.chatrmi.client.ChatClientGUI %SERVER_IP%

set JAVA_EXIT_CODE=%ERRORLEVEL%

echo.
echo ========================================
echo   CLIENTE ENCERRADO
echo ========================================
if %JAVA_EXIT_CODE% NEQ 0 (
    echo.
    echo [ERRO] O cliente foi encerrado com codigo de erro: %JAVA_EXIT_CODE%
    echo.
    echo Possiveis causas:
    echo - Erro de conexao com o servidor
    echo - Classe nao encontrada (verifique a compilacao)
    echo - Erro de inicializacao do Java
    echo - Firewall bloqueando conexoes
    echo.
    echo Verifique os erros acima para mais detalhes.
) else (
    echo Cliente encerrado normalmente.
)
echo.
pause
