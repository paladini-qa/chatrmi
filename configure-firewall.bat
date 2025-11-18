@echo off
echo ========================================
echo   CONFIGURACAO DO FIREWALL
echo ========================================
echo.
echo Este script configura o firewall do Windows
echo para permitir conexoes do Chat RMI
echo.
echo IMPORTANTE: Execute como Administrador!
echo.
pause

echo.
echo Adicionando regras de firewall...
echo.

netsh advfirewall firewall add rule name="Chat RMI Registry" dir=in action=allow protocol=TCP localport=1099 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Regra adicionada para porta 1099 (RMI Registry)
) else (
    echo [ERRO] Falha ao adicionar regra para porta 1099
    echo Execute como Administrador!
)

netsh advfirewall firewall add rule name="Chat RMI Server" dir=in action=allow protocol=TCP localport=1098 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Regra adicionada para porta 1098 (RMI Server)
) else (
    echo [ERRO] Falha ao adicionar regra para porta 1098
    echo Execute como Administrador!
)

netsh advfirewall firewall add rule name="Chat UDP File" dir=in action=allow protocol=UDP localport=9876 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Regra adicionada para porta 9876 (UDP File)
) else (
    echo [ERRO] Falha ao adicionar regra para porta 9876
    echo Execute como Administrador!
)

netsh advfirewall firewall add rule name="Chat UDP Download" dir=in action=allow protocol=UDP localport=9877 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Regra adicionada para porta 9877 (UDP Download)
) else (
    echo [ERRO] Falha ao adicionar regra para porta 9877
    echo Execute como Administrador!
)

echo.
echo ========================================
echo   CONFIGURACAO CONCLUIDA
echo ========================================
echo.
echo As regras de firewall foram adicionadas.
echo Agora voce pode executar o servidor e cliente.
echo.
pause

