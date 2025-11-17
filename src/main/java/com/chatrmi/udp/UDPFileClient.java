package com.chatrmi.udp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Cliente UDP para envio de arquivos
 */
public class UDPFileClient {
    
    private String serverHost;
    private int serverPort;
    
    public UDPFileClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }
    
    /**
     * Envia um arquivo para o servidor
     * @param file Arquivo a ser enviado
     * @param username Nome do usuário que está enviando
     * @return true se o envio foi bem-sucedido
     */
    public boolean sendFile(File file, String username) {
        if (!file.exists() || !file.isFile()) {
            System.err.println("Arquivo não existe: " + file.getAbsolutePath());
            return false;
        }
        
        try (DatagramSocket socket = new DatagramSocket();
             FileInputStream fis = new FileInputStream(file)) {
            
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            String filename = file.getName();
            long fileSize = file.length();
            
            byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
            byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
            
            ByteBuffer headerBuffer = ByteBuffer.allocate(2048);
            headerBuffer.putInt(usernameBytes.length);
            headerBuffer.put(usernameBytes);
            headerBuffer.putInt(filenameBytes.length);
            headerBuffer.put(filenameBytes);
            headerBuffer.putLong(fileSize);
            
            byte[] header = new byte[headerBuffer.position()];
            headerBuffer.rewind();
            headerBuffer.get(header);
            
            DatagramPacket headerPacket = new DatagramPacket(
                header, header.length, serverAddress, serverPort
            );
            socket.send(headerPacket);
            
            System.out.println("Enviando arquivo: " + filename + " (" + fileSize + " bytes)");
            
            byte[] buffer = new byte[8192];
            long sent = 0;
            
            while (sent < fileSize) {
                int bytesRead = fis.read(buffer);
                if (bytesRead == -1) break;
                
                DatagramPacket packet = new DatagramPacket(
                    buffer, bytesRead, serverAddress, serverPort
                );
                socket.send(packet);
                
                sent += bytesRead;
                Thread.sleep(1);
            }
            
            System.out.println("Arquivo enviado com sucesso!");
            return true;
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao enviar arquivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

