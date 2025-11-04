package com.chatrmi.server;

import com.chatrmi.interfaces.ChatService;
import com.chatrmi.udp.UDPFileServer;
import com.chatrmi.udp.UDPFileDownloadServer;

import java.net.BindException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Servidor principal do chat RMI
 */
public class ChatServer {
    
    private static final int RMI_REGISTRY_PORT = 1099;
    private static final int RMI_SERVER_PORT = 1098;
    private static final int UDP_FILE_PORT = 9876;
    private static final int UDP_DOWNLOAD_PORT = 9877;
    
    /**
     * Tenta exportar o objeto remoto, tentando portas alternativas se necessário
     */
    private static ChatService exportObject(ChatServiceImpl chatService, int preferredPort) throws Exception {
        // Tenta a porta preferida primeiro
        try {
            return (ChatService) UnicastRemoteObject.exportObject(chatService, preferredPort);
        } catch (ExportException e) {
            if (e.getCause() instanceof BindException) {
                System.out.println("Porta " + preferredPort + " está em uso. Tentando portas alternativas...");
                
                // Tenta portas de 1098 a 1105
                for (int port = preferredPort + 1; port <= preferredPort + 7; port++) {
                    try {
                        ChatService stub = (ChatService) UnicastRemoteObject.exportObject(chatService, port);
                        System.out.println("[OK] Objeto RMI exportado na porta alternativa: " + port);
                        return stub;
                    } catch (ExportException ex) {
                        if (!(ex.getCause() instanceof BindException)) {
                            throw ex;
                        }
                        // Continua tentando próxima porta
                    }
                }
                
                // Se nenhuma porta funcionou, lança exceção com mensagem útil
                throw new Exception("Não foi possível encontrar uma porta livre. " +
                    "Tente fechar processos usando as portas 1098-1105 ou reinicie o computador.", e);
            }
            throw e;
        }
    }
    
    public static void main(String[] args) {
        try {
            // Cria a implementação do serviço
            ChatServiceImpl chatService = new ChatServiceImpl();
            
            // Exporta o objeto remoto na porta especificada (ou alternativa)
            ChatService stub = exportObject(chatService, RMI_SERVER_PORT);
            
            // Cria ou obtém o registry
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
                System.out.println("RMI Registry criado na porta " + RMI_REGISTRY_PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(RMI_REGISTRY_PORT);
                System.out.println("RMI Registry encontrado na porta " + RMI_REGISTRY_PORT);
            }
            
            // Registra o serviço
            registry.rebind("ChatService", stub);
            System.out.println("Servidor RMI iniciado e registrado como 'ChatService'");
            
            // Inicia servidor UDP para arquivos
            UDPFileServer udpFileServer = new UDPFileServer(UDP_FILE_PORT, chatService);
            Thread udpThread = new Thread(() -> {
                try {
                    udpFileServer.start();
                } catch (Exception e) {
                    System.err.println("Erro no servidor UDP: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            udpThread.setDaemon(true);
            udpThread.start();
            System.out.println("Servidor UDP iniciado na porta " + UDP_FILE_PORT);
            
            // Inicia servidor UDP para download de arquivos
            UDPFileDownloadServer downloadServer = new UDPFileDownloadServer(UDP_DOWNLOAD_PORT);
            Thread downloadThread = new Thread(() -> {
                try {
                    downloadServer.start();
                } catch (Exception e) {
                    System.err.println("Erro no servidor UDP de download: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            downloadThread.setDaemon(true);
            downloadThread.start();
            System.out.println("Servidor UDP de download iniciado na porta " + UDP_DOWNLOAD_PORT);
            
            System.out.println("\n=== SERVIDOR PRONTO ===");
            System.out.println("Pressione Ctrl+C para encerrar\n");
            
        } catch (Exception e) {
            System.err.println("\n[ERRO] Erro ao iniciar servidor: " + e.getMessage());
            
            if (e.getMessage() != null && e.getMessage().contains("Port already in use")) {
                System.err.println("\n[SOLUCAO]:");
                System.err.println("1. Verifique se ja ha uma instancia do servidor rodando");
                System.err.println("2. Feche processos usando a porta 1098:");
                System.err.println("   - Windows: netstat -ano | findstr :1098");
                System.err.println("   - Depois: taskkill /PID <numero_do_pid> /F");
                System.err.println("   - Ou use o script: kill-port-1098.bat");
                System.err.println("3. Ou reinicie o servidor (pode ter ficado em execucao)");
            }
            
            e.printStackTrace();
            System.err.println("\nPressione qualquer tecla para continuar...");
        }
    }
}

