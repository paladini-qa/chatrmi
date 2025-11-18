@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   CLIENTE CHAT RMI - MODO REDE
echo ========================================
echo.

REM Verificar se Java está instalado
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Java nao encontrado no PATH!
    echo Por favor, instale o Java ou adicione-o ao PATH.
    pause
    exit /b 1
)
echo [OK] Java encontrado.

REM Verificar se Maven está instalado
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Maven nao encontrado no PATH!
    echo Por favor, instale o Maven ou adicione-o ao PATH.
    pause
    exit /b 1
)
echo [OK] Maven encontrado.

echo.
echo Compilando projeto...
call mvn clean compile >compile.log 2>&1
set COMPILE_ERROR=%ERRORLEVEL%

if %COMPILE_ERROR% NEQ 0 (
    echo [ERRO] Falha na compilacao!
    echo.
    echo Ultimas linhas do log de compilacao:
    echo ----------------------------------------
    powershell -Command "Get-Content compile.log -Tail 20"
    echo ----------------------------------------
    echo.
    echo Log completo salvo em: compile.log
    pause
    exit /b 1
)
echo [OK] Compilacao bem-sucedida.

echo.
echo ========================================
echo   CONFIGURACAO DO CLIENTE
echo ========================================
echo.
echo Digite o IP do servidor:
set /p SERVER_IP=

if "%SERVER_IP%"=="" (
    echo [ERRO] IP do servidor nao informado!
    pause
    exit /b 1
)

echo.
echo Digite o IP deste computador (cliente):
set /p CLIENT_IP=

if "!CLIENT_IP!"=="" (
    echo [ERRO] IP do cliente nao informado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   INICIANDO CLIENTE
echo ========================================
echo IP do servidor: %SERVER_IP%
echo IP do cliente: !CLIENT_IP!
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
echo Verificando se o diretorio de classes existe...
if not exist "target\classes" (
    echo [ERRO] Diretorio target\classes nao existe!
    pause
    exit /b 1
)
echo [OK] Diretorio de classes existe.

echo.
echo ========================================
echo   EXECUTANDO CLIENTE JAVA
echo ========================================
echo.
echo Comando que sera executado:
echo   java -Djava.rmi.server.hostname=!CLIENT_IP! -cp "target/classes" com.chatrmi.client.ChatClientGUI %SERVER_IP%
echo.
echo IMPORTANTE: Se o cliente fechar imediatamente, verifique:
echo   1. O arquivo client.log para erros
echo   2. Se o servidor esta rodando
echo   3. Se o firewall permite conexoes
echo.
echo Pressione qualquer tecla para iniciar o cliente...
pause >nul

REM Executar Java e redirecionar saída para arquivo de log
echo.
echo Iniciando cliente... (todos os logs serao salvos em client.log)
echo.

java -Djava.rmi.server.hostname=!CLIENT_IP! -cp "target/classes" com.chatrmi.client.ChatClientGUI %SERVER_IP% >client.log 2>&1

set JAVA_EXIT_CODE=%ERRORLEVEL%

echo.
echo ========================================
echo   CLIENTE ENCERRADO
echo ========================================
echo Codigo de saida: %JAVA_EXIT_CODE%
echo.

if %JAVA_EXIT_CODE% NEQ 0 (
    echo [ERRO] O cliente foi encerrado com codigo de erro: %JAVA_EXIT_CODE%
    echo.
    echo ========================================
    echo   ULTIMAS LINHAS DO LOG
    echo ========================================
    if exist client.log (
        powershell -Command "Get-Content client.log -Tail 30"
    ) else (
        echo Nenhum log encontrado. O cliente pode ter falhado antes de iniciar.
    )
    echo ========================================
    echo.
    echo Log completo salvo em: client.log
    echo.
    echo Possiveis causas:
    echo - Erro de conexao com o servidor
    echo - Classe nao encontrada (verifique a compilacao)
    echo - Erro de inicializacao do Java
    echo - Firewall bloqueando conexoes
    echo - IP do servidor incorreto
    echo.
) else (
    echo Cliente encerrado normalmente.
    if exist client.log (
        echo Log salvo em: client.log
    )
)

echo.
echo Pressione qualquer tecla para fechar...
pause >nul

endlocal
