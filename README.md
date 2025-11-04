# Chat RMI - Sistema de Chat com Java RMI

Sistema de chat distribuído usando Java RMI com as seguintes funcionalidades:

## Funcionalidades

- ✅ **Chat cliente-servidor usando RMI**: Comunicação distribuída usando Java RMI
- ✅ **Interface gráfica (Swing)**: Interface amigável com lista de usuários online
- ✅ **Envio de arquivos via UDP**: Botão para envio de arquivos usando sockets UDP
- ✅ **Callback para notificações**: Servidor notifica clientes remotamente (servidor "vai" no cliente e executa métodos)
- ✅ **Padrão Observer**: Implementação completa do padrão Observer para gerenciamento de eventos
- ✅ **Notificações em tempo real**: Clientes recebem atualizações instantâneas sobre mensagens, arquivos e usuários

## Estrutura do Projeto

```
src/
├── main/
│   └── java/
│       └── com/
│           └── chatrmi/
│               ├── interfaces/
│               │   ├── ChatService.java
│               │   └── ChatClientCallback.java
│               ├── observer/
│               │   ├── Observer.java
│               │   ├── Subject.java
│               │   └── ChatObserver.java
│               ├── server/
│               │   ├── ChatServiceImpl.java
│               │   └── ChatServer.java
│               ├── client/
│               │   ├── ChatClient.java
│               │   └── ChatClientGUI.java
│               └── udp/
│                   ├── UDPFileServer.java
│                   └── UDPFileClient.java
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

## Portas Utilizadas

- **RMI Registry**: 1099
- **RMI Server**: 1098
- **UDP File Server**: 9876

## Arquitetura

### Padrão Observer

O sistema implementa o padrão Observer da seguinte forma:

1. **Subject**: `ChatSubject` estende a classe `Subject` e mantém lista de observadores
2. **Observer**: `ChatObserver` implementa a interface `Observer` e recebe eventos
3. **Eventos**: `MessageEvent`, `UserEvent`, `FileEvent` representam diferentes tipos de eventos

Quando uma mensagem é enviada:
- O servidor notifica todos os observadores locais usando `notifyObservers()`
- O servidor também chama o método `onMessageReceived()` em cada cliente (callback RMI)

### Callback RMI

O sistema usa callbacks RMI onde:
- Cada cliente implementa `ChatClientCallback` que é uma interface remota
- O servidor mantém referências remotas para cada cliente
- Quando há uma atualização, o **servidor vai ao cliente** e executa os métodos:
  - `onMessageReceived()` - para novas mensagens
  - `onFileReceived()` - para novos arquivos
  - `onUsersUpdated()` - para atualizações na lista de usuários

### Upload de Arquivos UDP

- Cliente envia arquivo via socket UDP para o servidor
- Servidor recebe o arquivo e salva no diretório `uploads/`
- Após receber, o servidor notifica todos os clientes via callback RMI

## Estrutura de Classes

### Interfaces RMI
- `ChatService`: Interface remota do serviço de chat
- `ChatClientCallback`: Interface remota para callbacks do cliente

### Observer Pattern
- `Observer`: Interface do padrão Observer
- `Subject`: Classe abstrata do padrão Observer
- `ChatObserver`: Observador específico para eventos de chat

### Servidor
- `ChatServer`: Classe principal do servidor
- `ChatServiceImpl`: Implementação do serviço RMI

### Cliente
- `ChatClient`: Cliente RMI com implementação de callback
- `ChatClientGUI`: Interface gráfica Swing

### UDP
- `UDPFileServer`: Servidor UDP para recebimento de arquivos
- `UDPFileClient`: Cliente UDP para envio de arquivos

