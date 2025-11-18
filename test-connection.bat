@echo off
echo ========================================
echo   TESTE DE CONECTIVIDADE
echo ========================================
echo.
echo Este script testa se o servidor esta acessivel
echo.
echo Digite o IP do servidor:
set /p SERVER_IP=

if "%SERVER_IP%"=="" (
    echo IP do servidor nao informado!
    pause
    exit /b 1
)

echo.
echo Testando conectividade com %SERVER_IP%...
echo.

echo Testando porta 1099 (RMI Registry)...
powershell -Command "Test-NetConnection -ComputerName %SERVER_IP% -Port 1099" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [FALHOU] Porta 1099 nao esta acessivel
) else (
    echo [OK] Porta 1099 esta acessivel
)

echo.
echo Testando porta 1098 (RMI Server)...
powershell -Command "Test-NetConnection -ComputerName %SERVER_IP% -Port 1098" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [FALHOU] Porta 1098 nao esta acessivel
) else (
    echo [OK] Porta 1098 esta acessivel
)

echo.
echo Testando porta 9876 (UDP File Server)...
powershell -Command "Test-NetConnection -ComputerName %SERVER_IP% -Port 9876 -Udp" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [AVISO] Teste UDP pode nao ser confiavel
) else (
    echo [OK] Porta 9876 (UDP) parece estar acessivel
)

echo.
echo ========================================
echo   RESULTADO
echo ========================================
echo.
echo Se as portas 1099 e 1098 estao acessiveis,
echo o problema pode estar na configuracao RMI.
echo.
echo Se as portas nao estao acessiveis:
echo 1. Verifique se o servidor esta rodando
echo 2. Verifique o firewall do servidor
echo 3. Verifique se os PCs estao na mesma rede
echo.
pause

