@echo off
echo ========================================
echo   SERVIDOR CHAT RMI - MODO REDE
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
echo   CONFIGURACAO DO SERVIDOR
echo ========================================
echo.
echo Digite o IP deste computador (ou pressione Enter para auto-detectar):
set /p SERVER_IP=

if "%SERVER_IP%"=="" (
    echo.
    echo Auto-detectando IP...
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
        set SERVER_IP=%%a
        goto :found
    )
    :found
    set SERVER_IP=%SERVER_IP: =%
    if "%SERVER_IP%"=="" (
        echo.
        echo Nao foi possivel detectar o IP automaticamente.
        echo Por favor, digite o IP manualmente:
        set /p SERVER_IP=
    ) else (
        echo IP detectado: %SERVER_IP%
    )
)

if "%SERVER_IP%"=="" (
    echo IP do servidor nao informado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO SERVIDOR
echo ========================================
echo IP do servidor: %SERVER_IP%
echo.
echo IMPORTANTE: Certifique-se de que o firewall permite
echo conexoes nas portas 1099, 1098, 9876 e 9877
echo.
echo Pressione qualquer tecla para iniciar o servidor...
pause >nul

java -Djava.rmi.server.hostname=%SERVER_IP% -cp "target/classes" com.chatrmi.server.ChatServer %SERVER_IP%

pause
