# Chat RMI - Sistema de Chat com Java RMI

Sistema de chat distribuído usando Java RMI com as seguintes funcionalidades:

## Funcionalidades

### Chat e Comunicação

- ✅ **Chat cliente-servidor usando RMI**: Comunicação distribuída usando Java RMI
- ✅ **Chat Global**: Chat público onde todos os usuários podem conversar
- ✅ **Chat em Grupos**: Criação e participação em grupos privados de chat
- ✅ **Histórico de Mensagens**: Persistência do histórico de conversas (global e por grupo)
- ✅ **Interface gráfica moderna (Swing)**: Interface amigável com componentes visuais personalizados
  - `MessageBubble`: Bolhas de mensagem estilizadas
  - `FileBubble`: Componentes visuais para arquivos compartilhados
  - Lista de usuários online
  - Lista de membros do grupo

### Gerenciamento de Grupos

- ✅ **Criação de Grupos**: Usuários podem criar grupos privados
- ✅ **Sistema de Convites**: Donos podem convidar usuários para seus grupos
- ✅ **Solicitações de Entrada**: Usuários podem solicitar entrada em grupos
- ✅ **Aprovação/Reprovação**: Donos gerenciam solicitações de entrada
- ✅ **Gerenciamento de Membros**: Visualização e remoção de membros (apenas dono)
- ✅ **Interface de Gerenciamento**: GUI dedicada para gerenciar grupos (`GroupManagementGUI`)

### Compartilhamento de Arquivos

- ✅ **Envio de arquivos via UDP**: Upload de arquivos usando sockets UDP
- ✅ **Download de arquivos**: Download de arquivos compartilhados via UDP
- ✅ **Lista de arquivos disponíveis**: Visualização de todos os arquivos compartilhados
- ✅ **Notificações de arquivos**: Clientes são notificados quando novos arquivos são compartilhados

### Arquitetura e Padrões

- ✅ **Callback RMI**: Servidor notifica clientes remotamente (servidor "vai" no cliente e executa métodos)
- ✅ **Padrão Observer**: Implementação completa do padrão Observer para gerenciamento de eventos
- ✅ **Notificações em tempo real**: Clientes recebem atualizações instantâneas sobre mensagens, arquivos, usuários e grupos

## Estrutura do Projeto

```
src/
├── main/
│   └── java/
│       └── com/
│           └── chatrmi/
│               ├── interfaces/
│               │   ├── ChatService.java          # Interface remota do serviço de chat
│               │   └── ChatClientCallback.java  # Interface remota para callbacks do cliente
│               ├── observer/
│               │   ├── Observer.java            # Interface do padrão Observer
│               │   ├── Subject.java             # Classe abstrata do padrão Observer
│               │   └── ChatObserver.java        # Observador específico para eventos de chat
│               ├── server/
│               │   ├── ChatServiceImpl.java     # Implementação do serviço RMI
│               │   └── ChatServer.java          # Classe principal do servidor
│               ├── client/
│               │   ├── ChatClient.java          # Cliente RMI com implementação de callback
│               │   ├── ChatClientGUI.java        # Interface gráfica principal
│               │   ├── GlobalChatGUI.java        # Interface para chat global
│               │   ├── GroupChatGUI.java         # Interface para chat de grupos
│               │   ├── GroupManagementGUI.java  # Interface para gerenciar grupos
│               │   ├── MessageBubble.java       # Componente visual para mensagens
│               │   ├── FileBubble.java           # Componente visual para arquivos
│               │   └── MessageHistory.java       # Classe para histórico de mensagens
│               ├── model/
│               │   ├── Group.java               # Modelo de grupo de chat
│               │   └── GroupRequest.java        # Modelo de solicitação de grupo
│               └── udp/
│                   ├── UDPFileServer.java        # Servidor UDP para recebimento de arquivos
│                   ├── UDPFileClient.java        # Cliente UDP para envio de arquivos
│                   ├── UDPFileDownloadServer.java # Servidor UDP para download de arquivos
│                   └── UDPFileDownloadClient.java # Cliente UDP para download de arquivos
```

## Como Executar

### Pré-requisitos

- Java 11 ou superior
- Maven (opcional, para compilação)

### 1. Compilar o projeto

**Usando Maven:**

```bash
mvn clean compile
```

**Ou compilar manualmente:**

```bash
javac -d target/classes -sourcepath src/main/java src/main/java/com/chatrmi/**/*.java
```

### 2. Executar o Servidor

**Windows (usando script):**

```bash
run-server.bat
```

**Ou manualmente:**

```bash
java -cp target/classes com.chatrmi.server.ChatServer
```

### 3. Executar o Cliente

Abra múltiplas instâncias do cliente para testar o chat:

**Windows (usando script):**

```bash
run-client.bat
```

**Ou manualmente:**

```bash
java -cp target/classes com.chatrmi.client.ChatClientGUI
```

Ao iniciar o cliente, você será solicitado a inserir seu nome de usuário.

### 4. Usando o Sistema

**Chat Global:**

- Após conectar, você verá a janela do chat global
- Digite mensagens no campo de texto e pressione Enter ou clique em "Enviar"
- Clique no botão de arquivo para compartilhar arquivos
- Todos os usuários online receberão suas mensagens e arquivos

**Grupos:**

- Clique em "Gerenciar Grupos" para abrir a interface de grupos
- **Criar Grupo**: Clique em "Criar Novo Grupo" e digite o nome
- **Convidar Usuários**: Como dono, selecione um grupo e clique em "Convidar" para adicionar usuários
- **Solicitar Entrada**: Selecione um grupo disponível e clique em "Solicitar Entrada"
- **Gerenciar Solicitações**: Como dono, veja e aprove/reprove solicitações pendentes
- **Abrir Chat do Grupo**: Clique duas vezes em um grupo para abrir a janela de chat do grupo
- **Sair do Grupo**: Clique em "Sair do Grupo" para deixar um grupo (dono não pode sair)

**Download de Arquivos:**

- Na lista de arquivos disponíveis, selecione um arquivo
- Clique em "Baixar" para iniciar o download
- Arquivos são salvos no diretório `downloads/` do cliente

## Conexão entre Dois PCs Diferentes

Para conectar dois PCs diferentes na mesma rede:

### 1. No PC Servidor

Execute o script de rede:

```bash
run-server-network.bat
```

Quando solicitado, digite o IP deste computador (ou pressione Enter para auto-detectar).

**Importante:** O servidor mostrará o IP que deve ser usado pelos clientes.

### 2. No PC Cliente

Execute o script de rede:

```bash
run-client-network.bat
```

Quando solicitado, digite o **IP do servidor** (o IP mostrado pelo servidor).

### 3. Configurar Firewall

**No PC Servidor**, configure o firewall manualmente como Administrador:

**Windows PowerShell (como Administrador):**

```powershell
# Permitir RMI Registry
netsh advfirewall firewall add rule name="Chat RMI Registry" dir=in action=allow protocol=TCP localport=1099

# Permitir RMI Server
netsh advfirewall firewall add rule name="Chat RMI Server" dir=in action=allow protocol=TCP localport=1098

# Permitir UDP File Upload
netsh advfirewall firewall add rule name="Chat UDP File" dir=in action=allow protocol=UDP localport=9876

# Permitir UDP File Download
netsh advfirewall firewall add rule name="Chat UDP Download" dir=in action=allow protocol=UDP localport=9877
```

**Ou via Interface Gráfica do Windows:**

1. Abra "Firewall do Windows Defender"
2. Clique em "Configurações avançadas"
3. Clique em "Regras de Entrada" → "Nova Regra"
4. Crie regras para as portas TCP 1099, 1098 e UDP 9876, 9877

### Troubleshooting

**Problema: "Erro ao conectar ao servidor"**

1. **Verifique se o servidor está rodando**

   - O servidor deve mostrar "=== SERVIDOR PRONTO ==="
   - Verifique se o RMI Registry foi iniciado corretamente

2. **Verifique o IP**

   - No servidor, confirme o IP mostrado na tela
   - No cliente, use exatamente esse IP (sem espaços extras)
   - Certifique-se de que não está usando `localhost` ou `127.0.0.1` em conexões de rede

3. **Verifique o firewall**

   - Configure as regras de firewall no servidor como Administrador (veja seção acima)
   - Ou desative temporariamente o firewall para testar
   - Verifique se o antivírus não está bloqueando as conexões

4. **Verifique a rede**

   - Os PCs devem estar na mesma rede (mesmo Wi-Fi ou cabo)
   - Teste conectividade: `ping <IP_DO_SERVIDOR>` no cliente
   - Verifique se ambos os PCs podem se comunicar

5. **Verifique as portas**
   - Certifique-se de que as portas 1099, 1098 (TCP) e 9876, 9877 (UDP) não estão em uso
   - Use `netstat -an | findstr "1099 1098 9876 9877"` para verificar portas em uso

**Problema: "Connection refused" ou timeout**

- O firewall está bloqueando as conexões
- O IP do servidor está incorreto no cliente
- O servidor não está escutando na porta correta
- Os PCs não estão na mesma rede local
- O RMI Registry não foi iniciado corretamente

**Problema: "Arquivo não encontrado" ou erro ao baixar arquivo**

- Verifique se o diretório `uploads/` existe no servidor
- Verifique se o diretório `downloads/` existe no cliente
- Certifique-se de que as portas UDP 9876 e 9877 estão abertas no firewall
- Verifique se o arquivo foi completamente enviado antes de tentar baixar

**Problema: "Grupo não encontrado" ou erro ao acessar grupo**

- Certifique-se de que você é membro do grupo
- Verifique se o grupo ainda existe (pode ter sido removido)
- Tente atualizar a lista de grupos

## Tecnologias Utilizadas

- **Java 11+**: Linguagem de programação
- **Java RMI (Remote Method Invocation)**: Para comunicação distribuída cliente-servidor
- **Java Swing**: Para interface gráfica do usuário
- **UDP Sockets**: Para transferência de arquivos
- **Maven**: Para gerenciamento de dependências e build (opcional)

## Portas Utilizadas

- **RMI Registry**: 1099 (TCP) - Registro de objetos remotos
- **RMI Server**: 1098 (TCP) - Servidor RMI principal
- **UDP File Server**: 9876 (UDP) - Upload de arquivos
- **UDP Download Server**: 9877 (UDP) - Download de arquivos

## Arquitetura

### Padrão Observer

O sistema implementa o padrão Observer da seguinte forma:

1. **Subject**: `ChatSubject` estende a classe `Subject` e mantém lista de observadores
2. **Observer**: `ChatObserver` implementa a interface `Observer` e recebe eventos
3. **Eventos**: `MessageEvent`, `UserEvent`, `FileEvent`, `GroupEvent` representam diferentes tipos de eventos

Quando uma mensagem é enviada:

- O servidor notifica todos os observadores locais usando `notifyObservers()`
- O servidor também chama o método `onMessageReceived()` em cada cliente (callback RMI)

### Callback RMI

O sistema usa callbacks RMI onde:

- Cada cliente implementa `ChatClientCallback` que é uma interface remota
- O servidor mantém referências remotas para cada cliente
- Quando há uma atualização, o **servidor vai ao cliente** e executa os métodos:
  - `onMessageReceived()` - para novas mensagens no chat global
  - `onGroupMessageReceived()` - para novas mensagens em grupos
  - `onFileReceived()` - para novos arquivos compartilhados
  - `onUsersUpdated()` - para atualizações na lista de usuários online
  - `onGroupCreated()` - quando um novo grupo é criado
  - `onGroupUpdated()` - quando um grupo é atualizado (membros, convites, etc.)
  - `onInviteReceived()` - quando um usuário recebe um convite para grupo
  - `onJoinRequestReceived()` - quando um dono recebe uma solicitação de entrada

### Sistema de Grupos

O sistema de grupos funciona da seguinte forma:

1. **Criação de Grupo**:

   - Usuário cria grupo através da `GroupManagementGUI`
   - Servidor cria instância de `Group` e define o criador como dono
   - Todos os clientes são notificados via callback

2. **Convites**:

   - Dono pode convidar usuários online para o grupo
   - Usuário convidado recebe notificação via `onInviteReceived()`
   - Usuário pode aceitar ou rejeitar o convite

3. **Solicitações de Entrada**:

   - Usuário pode solicitar entrada em um grupo
   - Dono recebe notificação via `onJoinRequestReceived()`
   - Dono pode aprovar ou reprovar a solicitação

4. **Mensagens em Grupo**:
   - Apenas membros do grupo podem enviar mensagens
   - Mensagens são distribuídas apenas para membros do grupo
   - Histórico é mantido separadamente por grupo

### Compartilhamento de Arquivos UDP

**Upload (Envio)**:

- Cliente seleciona arquivo e envia via `UDPFileClient` para o servidor
- Servidor (`UDPFileServer`) recebe o arquivo e salva no diretório `uploads/`
- Após receber, o servidor notifica todos os clientes via callback RMI

**Download**:

- Cliente solicita download via RMI (`requestFileDownload()`)
- Servidor retorna informações do arquivo (nome e tamanho)
- Cliente (`UDPFileDownloadClient`) conecta ao servidor (`UDPFileDownloadServer`) via UDP
- Arquivo é transferido e salvo no diretório `downloads/` do cliente

### Histórico de Mensagens

- Cada cliente mantém histórico local de mensagens
- Histórico é separado por contexto: chat global e grupos
- Histórico é carregado quando o cliente se conecta ou abre um grupo
- Mensagens são persistidas em memória durante a sessão

## Estrutura de Classes

### Interfaces RMI

- `ChatService`: Interface remota do serviço de chat com métodos para mensagens, grupos e arquivos
- `ChatClientCallback`: Interface remota para callbacks do cliente (notificações do servidor)

### Observer Pattern

- `Observer`: Interface do padrão Observer
- `Subject`: Classe abstrata do padrão Observer
- `ChatObserver`: Observador específico para eventos de chat (mensagens, usuários, arquivos)

### Modelos de Dados

- `Group`: Modelo que representa um grupo de chat com membros e solicitações pendentes
- `GroupRequest`: Modelo que representa uma solicitação de entrada em grupo
- `MessageHistory`: Classe para armazenar e gerenciar histórico de mensagens

### Servidor

- `ChatServer`: Classe principal do servidor que inicializa o RMI Registry e serviços
- `ChatServiceImpl`: Implementação do serviço RMI com lógica de negócio para chat, grupos e arquivos

### Cliente

- `ChatClient`: Cliente RMI com implementação de callback e lógica de comunicação
- `ChatClientGUI`: Interface gráfica principal que gerencia as diferentes janelas
- `GlobalChatGUI`: Interface gráfica para o chat global (todos os usuários)
- `GroupChatGUI`: Interface gráfica para chat de grupos específicos
- `GroupManagementGUI`: Interface gráfica para criar e gerenciar grupos
- `MessageBubble`: Componente Swing personalizado para exibir mensagens
- `FileBubble`: Componente Swing personalizado para exibir arquivos compartilhados

### UDP (Compartilhamento de Arquivos)

- `UDPFileServer`: Servidor UDP para recebimento de arquivos (upload)
- `UDPFileClient`: Cliente UDP para envio de arquivos (upload)
- `UDPFileDownloadServer`: Servidor UDP para download de arquivos
- `UDPFileDownloadClient`: Cliente UDP para download de arquivos
