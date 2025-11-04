# Como Executar no Windows

## Método 1: Usando os Scripts .bat (Recomendado)

### Pré-requisito: Maven instalado

1. **Abra o PowerShell ou Prompt de Comando (CMD)**
   - Pressione `Win + R`, digite `cmd` ou `powershell` e pressione Enter
   - OU clique com botão direito na pasta do projeto e selecione "Abrir no Terminal"

2. **Navegue até a pasta do projeto** (se necessário):
   ```cmd
   cd C:\Users\Vitor\Documents\GitHub\chatrmi
   ```

3. **Para iniciar o SERVIDOR:**
   ```cmd
   run-server.bat
   ```
   Ou simplesmente **duplo clique** no arquivo `run-server.bat` no Explorer do Windows

4. **Para iniciar o CLIENTE:**
   - Abra uma **nova janela** do PowerShell/CMD
   - Navegue até a pasta do projeto
   - Execute:
   ```cmd
   run-client.bat
   ```
   Ou **duplo clique** no arquivo `run-client.bat`

### Método 2: Compilação Manual (Sem Maven)

Se você **não tem Maven instalado**, use os scripts manuais:

1. **Para o servidor:**
   ```cmd
   run-server-manual.bat
   ```
   Ou duplo clique em `run-server-manual.bat`

2. **Para o cliente:**
   ```cmd
   run-client-manual.bat
   ```
   Ou duplo clique em `run-client-manual.bat`

### Método 3: Execução Manual (Passo a passo)

1. **Compilar o projeto:**
   
   **Com Maven:**
   ```cmd
   mvn clean compile
   ```
   
   **Ou manualmente (sem Maven):**
   ```cmd
   mkdir target\classes
   javac -d target/classes -sourcepath src/main/java src/main/java/com/chatrmi/**/*.java
   ```

2. **Executar o servidor:**
   ```cmd
   java -cp "target/classes" com.chatrmi.server.ChatServer
   ```

3. **Executar o cliente (em outra janela):**
   ```cmd
   java -cp "target/classes" com.chatrmi.client.ChatClientGUI
   ```

## Instruções Passo a Passo

### Primeira Execução:

1. **Abra o primeiro terminal** e execute o servidor:
   ```
   run-server.bat
   ```
   
   Você verá mensagens como:
   ```
   RMI Registry criado na porta 1099
   Servidor RMI iniciado e registrado como 'ChatService'
   Servidor UDP iniciado na porta 9876
   === SERVIDOR PRONTO ===
   ```

2. **Deixe o servidor rodando** e abra um **segundo terminal**

3. **No segundo terminal**, execute o cliente:
   ```
   run-client.bat
   ```

4. **Uma janela gráfica aparecerá** pedindo seu nome de usuário

5. **Digite seu nome** e clique OK

6. **Repita os passos 2-5** para abrir mais clientes e testar o chat

## Solução de Problemas

### Erro: "mvn não é reconhecido"
- **Solução**: Use `run-server-manual.bat` e `run-client-manual.bat` ao invés dos scripts com Maven

### Erro: "java não é reconhecido"
- **Solução**: Instale o Java JDK 11 ou superior e configure a variável de ambiente PATH

### Erro: "Port already in use"
- **Solução**: Verifique se já há um servidor rodando ou feche processos usando a porta 1099

### Cliente não conecta
- **Solução**: Certifique-se de que o servidor está rodando antes de iniciar o cliente

## Atalhos Rápidos

- **Duplo clique** em `run-server.bat` → Inicia servidor
- **Duplo clique** em `run-client.bat` → Inicia cliente (abrir várias vezes para múltiplos clientes)

## Nota Importante

- O **servidor precisa estar rodando** antes de iniciar qualquer cliente
- Você pode ter **múltiplos clientes** rodando simultaneamente
- Cada cliente precisa ter um **nome de usuário único**

