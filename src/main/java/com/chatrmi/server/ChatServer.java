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
    
    private static ChatService exportObject(ChatServiceImpl chatService, int preferredPort) throws Exception {
        try {
            return (ChatService) UnicastRemoteObject.exportObject(chatService, preferredPort);
        } catch (ExportException e) {
            if (e.getCause() instanceof BindException) {
                System.out.println("Porta " + preferredPort + " está em uso. Tentando portas alternativas...");
                
                for (int port = preferredPort + 1; port <= preferredPort + 7; port++) {
                    try {
                        ChatService stub = (ChatService) UnicastRemoteObject.exportObject(chatService, port);
                        System.out.println("[OK] Objeto RMI exportado na porta alternativa: " + port);
                        return stub;
                    } catch (ExportException ex) {
                        if (!(ex.getCause() instanceof BindException)) {
                            throw ex;
                        }
                    }
                }
                
                throw new Exception("Não foi possível encontrar uma porta livre. " +
                    "Tente fechar processos usando as portas 1098-1105 ou reinicie o computador.", e);
            }
            throw e;
        }
    }
    
    private static String getLocalIP() {
        try {
            // Tentar obter IP da interface de rede principal (não loopback)
            java.net.NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(iface -> {
                try {
                    if (!iface.isLoopback() && iface.isUp()) {
                        iface.getInetAddresses().asIterator().forEachRemaining(addr -> {
                            if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                                // Encontrou um IP IPv4 válido
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignorar erros de interface
                }
            });
            
            // Fallback: usar getLocalHost()
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível detectar o IP automaticamente. Usando 'localhost'.");
            return "localhost";
        }
    }
    
    private static void printNetworkInfo(String serverHost) {
        System.out.println("\n=== INFORMAÇÕES DE REDE ===");
        System.out.println("IP do servidor configurado: " + serverHost);
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(serverHost);
            System.out.println("Endereço resolvido: " + addr.getHostAddress());
            
            System.out.println("\nInterfaces de rede disponíveis:");
            java.net.NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(iface -> {
                try {
                    if (!iface.isLoopback() && iface.isUp()) {
                        System.out.println("  - " + iface.getName() + ":");
                        iface.getInetAddresses().asIterator().forEachRemaining(inetAddr -> {
                            if (inetAddr instanceof java.net.Inet4Address) {
                                System.out.println("    -> " + inetAddr.getHostAddress());
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignorar
                }
            });
        } catch (Exception e) {
            System.err.println("Erro ao obter informações de rede: " + e.getMessage());
        }
        System.out.println("============================\n");
    }
    
    public static void main(String[] args) {
        try {
            // Obter IP do servidor (padrão: IP local)
            String serverHost = args.length > 0 ? args[0] : getLocalIP();
            System.setProperty("java.rmi.server.hostname", serverHost);
            
            System.out.println("========================================");
            System.out.println("  SERVIDOR CHAT RMI - MODO REDE");
            System.out.println("========================================");
            System.out.println("IP do servidor: " + serverHost);
            System.out.println("========================================\n");
            
            // Mostrar informações de rede
            printNetworkInfo(serverHost);
            
            System.out.println("IMPORTANTE: Para conectar de outro PC, use este IP: " + serverHost);
            System.out.println("Certifique-se de que o firewall permite conexões nas portas:");
            System.out.println("  - 1099 (RMI Registry)");
            System.out.println("  - 1098 (RMI Server)");
            System.out.println("  - 9876 (UDP File Server)");
            System.out.println("  - 9877 (UDP Download Server)");
            System.out.println();
            
            ChatServiceImpl chatService = new ChatServiceImpl();
            ChatService stub = exportObject(chatService, RMI_SERVER_PORT);
            
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
                System.out.println("RMI Registry criado na porta " + RMI_REGISTRY_PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(RMI_REGISTRY_PORT);
                System.out.println("RMI Registry encontrado na porta " + RMI_REGISTRY_PORT);
            }
            
            registry.rebind("ChatService", stub);
            System.out.println("Servidor RMI iniciado e registrado como 'ChatService'");
            
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

