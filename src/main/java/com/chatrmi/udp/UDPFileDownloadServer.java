package com.chatrmi.udp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Servidor UDP para envio de arquivos (download)
 */
public class UDPFileDownloadServer {
    
    private int port;
    private DatagramSocket socket;
    private boolean running;
    private String uploadDir;
    
    public UDPFileDownloadServer(int port) {
        this.port = port;
        this.running = false;
        this.uploadDir = "uploads";
    }
    
    public void start() throws SocketException {
        socket = new DatagramSocket(port);
        running = true;
        System.out.println("Servidor UDP de download aguardando solicitações na porta " + port + "...");
        
        while (running) {
            try {
                handleDownloadRequest();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao processar solicitação de download: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleDownloadRequest() throws IOException {
        byte[] requestBuffer = new byte[2048];
        DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);
        socket.receive(requestPacket);
        
        InetAddress clientAddress = requestPacket.getAddress();
        int clientPort = requestPacket.getPort();
        
        String filename = new String(requestBuffer, 0, requestPacket.getLength(), StandardCharsets.UTF_8);
        File file = new File(uploadDir, filename);
        
        if (!file.exists() || !file.isFile()) {
            byte[] errorMsg = "FILE_NOT_FOUND".getBytes(StandardCharsets.UTF_8);
            DatagramPacket errorPacket = new DatagramPacket(
                errorMsg, errorMsg.length, clientAddress, clientPort
            );
            socket.send(errorPacket);
            return;
        }
        
        long fileSize = file.length();
        System.out.println("Enviando arquivo: " + filename + " (" + fileSize + " bytes) para " + clientAddress);
        
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuffer headerBuffer = ByteBuffer.allocate(2048);
        headerBuffer.putInt(filenameBytes.length);
        headerBuffer.put(filenameBytes);
        headerBuffer.putLong(fileSize);
        
        byte[] header = new byte[headerBuffer.position()];
        headerBuffer.rewind();
        headerBuffer.get(header);
        
        DatagramPacket headerPacket = new DatagramPacket(
            header, header.length, clientAddress, clientPort
        );
        socket.send(headerPacket);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            long sent = 0;
            
            while (sent < fileSize) {
                int bytesRead = fis.read(buffer);
                if (bytesRead == -1) break;
                
                DatagramPacket packet = new DatagramPacket(
                    buffer, bytesRead, clientAddress, clientPort
                );
                socket.send(packet);
                
                sent += bytesRead;
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        
        System.out.println("Arquivo enviado com sucesso: " + filename);
    }
    
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}




