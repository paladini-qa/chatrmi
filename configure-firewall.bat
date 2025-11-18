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
echo Adicionando regras para portas dinamicas RMI (callbacks)...
echo IMPORTANTE: Clientes precisam permitir conexoes de entrada para receber mensagens!
echo.

netsh advfirewall firewall add rule name="Chat RMI Dynamic Ports" dir=in action=allow protocol=TCP localport=1024-65535 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Regra adicionada para portas dinamicas RMI (1024-65535)
    echo       Esta regra permite callbacks do servidor para o cliente
) else (
    echo [AVISO] Falha ao adicionar regra para portas dinamicas
    echo         Voce pode precisar configurar manualmente ou desativar o firewall temporariamente
)

echo.
echo ========================================
echo   CONFIGURACAO CONCLUIDA
echo ========================================
echo.
echo As regras de firewall foram adicionadas.
echo.
echo IMPORTANTE PARA CLIENTES EM OUTROS PCs:
echo - Execute este script TAMBEM no PC cliente
echo - Ou configure manualmente o firewall para permitir:
echo   * Conexoes de ENTRADA TCP (para receber callbacks)
echo   * Conexoes de SAIDA TCP nas portas 1099, 1098
echo   * Conexoes de SAIDA UDP nas portas 9876, 9877
echo.
pause

