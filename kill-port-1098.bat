@echo off
echo ========================================
echo   Fechando processos na porta 1098
echo ========================================
echo.

echo Procurando processos usando a porta 1098...
netstat -ano | findstr :1098

if %ERRORLEVEL% NEQ 0 (
    echo Nenhum processo encontrado na porta 1098.
    pause
    exit /b 0
)

echo.
echo Por favor, identifique o PID (ultima coluna) do processo que deseja fechar.
echo Digite o PID ou pressione Enter para tentar fechar todos automaticamente:
set /p PID=

if "%PID%"=="" (
    echo Tentando fechar todos os processos na porta 1098...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :1098') do (
        echo Fechando processo PID: %%a
        taskkill /PID %%a /F 2>nul
    )
) else (
    echo Fechando processo PID: %PID%
    taskkill /PID %PID% /F
)

echo.
echo Processo(s) fechado(s). Pode tentar iniciar o servidor novamente.
pause

