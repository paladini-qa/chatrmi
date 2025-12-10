package com.chatrmi.udp;

import com.chatrmi.server.ChatServiceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Servidor UDP para recebimento de arquivos
 */
public class UDPFileServer {
    
    private int port;
    private ChatServiceImpl chatService;
    private DatagramSocket socket;
    private boolean running;
    private String uploadDir;
    
    public UDPFileServer(int port, ChatServiceImpl chatService) {
        this.port = port;
        this.chatService = chatService;
        this.running = false;
        this.uploadDir = "uploads";
        
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public void start() throws SocketException {
        socket = new DatagramSocket(port);
        running = true;
        System.out.println("Servidor UDP aguardando arquivos na porta " + port + "...");
        
        while (running) {
            try {
                receiveFile();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao receber arquivo: " + e.getMessage());
                }
            }
        }
    }
    
    private void receiveFile() throws IOException {
        byte[] headerBuffer = new byte[2048];
        DatagramPacket headerPacket = new DatagramPacket(headerBuffer, headerBuffer.length);
        socket.receive(headerPacket);
        
        int receivedLength = headerPacket.getLength();
        
        // Validar tamanho mínimo do pacote (pelo menos 4 bytes para o primeiro int)
        if (receivedLength < 4) {
            System.err.println("Pacote UDP inválido: muito pequeno (" + receivedLength + " bytes)");
            return;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(headerBuffer, 0, receivedLength);
        
        // Ler e validar username
        int usernameLength = buffer.getInt();
        if (usernameLength <= 0 || usernameLength > 256 || buffer.remaining() < usernameLength) {
            System.err.println("Pacote UDP inválido: usernameLength inválido (" + usernameLength + ")");
            return;
        }
        byte[] usernameBytes = new byte[usernameLength];
        buffer.get(usernameBytes);
        String username = new String(usernameBytes, StandardCharsets.UTF_8);
        
        // Validar se ainda há dados para ler filenameLength
        if (buffer.remaining() < 4) {
            System.err.println("Pacote UDP inválido: dados insuficientes para filenameLength");
            return;
        }
        
        int filenameLength = buffer.getInt();
        if (filenameLength <= 0 || filenameLength > 512 || buffer.remaining() < filenameLength) {
            System.err.println("Pacote UDP inválido: filenameLength inválido (" + filenameLength + ")");
            return;
        }
        byte[] filenameBytes = new byte[filenameLength];
        buffer.get(filenameBytes);
        String filename = new String(filenameBytes, StandardCharsets.UTF_8);
        
        // Validar se ainda há dados para ler fileSize
        if (buffer.remaining() < 8) {
            System.err.println("Pacote UDP inválido: dados insuficientes para fileSize");
            return;
        }
        
        long fileSize = buffer.getLong();
        if (fileSize < 0 || fileSize > 100_000_000) { // Limite de 100MB
            System.err.println("Pacote UDP inválido: fileSize inválido (" + fileSize + ")");
            return;
        }
        
        System.out.println("Recebendo arquivo: " + filename + " (" + fileSize + " bytes) de " + username);
        
        File file = new File(uploadDir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            long received = 0;
            byte[] dataBuffer = new byte[8192];
            
            while (received < fileSize) {
                DatagramPacket packet = new DatagramPacket(dataBuffer, dataBuffer.length);
                socket.receive(packet);
                
                int packetSize = packet.getLength();
                if (packetSize == 0) {
                    continue; // Ignorar pacotes vazios
                }
                
                if (received + packetSize > fileSize) {
                    packetSize = (int) (fileSize - received);
                }
                
                fos.write(dataBuffer, 0, packetSize);
                received += packetSize;
            }
        }
        
        System.out.println("Arquivo recebido com sucesso: " + filename);
        
        if (chatService != null) {
            chatService.notifyFileReceived(username, filename);
        }
    }
    
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}

