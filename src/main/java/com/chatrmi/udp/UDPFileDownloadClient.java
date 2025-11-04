package com.chatrmi.udp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Cliente UDP para download de arquivos
 */
public class UDPFileDownloadClient {
    
    private String serverHost;
    private int serverPort;
    private String downloadDir;
    
    public UDPFileDownloadClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.downloadDir = "downloads";
        
        // Cria diretório de downloads se não existir
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Baixa um arquivo do servidor
     * @param filename Nome do arquivo a ser baixado
     * @return File objeto do arquivo baixado, ou null se houve erro
     */
    public File downloadFile(String filename) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            
            // Envia solicitação de download
            byte[] request = filename.getBytes(StandardCharsets.UTF_8);
            DatagramPacket requestPacket = new DatagramPacket(
                request, request.length, serverAddress, serverPort
            );
            socket.send(requestPacket);
            socket.setSoTimeout(5000); // Timeout de 5 segundos
            
            // Recebe resposta (cabeçalho ou erro)
            byte[] responseBuffer = new byte[2048];
            DatagramPacket responsePacket = new DatagramPacket(
                responseBuffer, responseBuffer.length
            );
            socket.receive(responsePacket);
            
            String response = new String(
                responsePacket.getData(), 0, responsePacket.getLength(), 
                StandardCharsets.UTF_8
            );
            
            if ("FILE_NOT_FOUND".equals(response)) {
                System.err.println("Arquivo não encontrado no servidor: " + filename);
                return null;
            }
            
            // Parse do cabeçalho
            ByteBuffer buffer = ByteBuffer.wrap(responseBuffer);
            int filenameLength = buffer.getInt();
            byte[] filenameBytes = new byte[filenameLength];
            buffer.get(filenameBytes);
            String receivedFilename = new String(filenameBytes, StandardCharsets.UTF_8);
            long fileSize = buffer.getLong();
            
            System.out.println("Recebendo arquivo: " + receivedFilename + " (" + fileSize + " bytes)");
            
            // Recebe o arquivo em chunks
            File file = new File(downloadDir, receivedFilename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                long received = 0;
                byte[] dataBuffer = new byte[8192];
                socket.setSoTimeout(10000); // Timeout maior para recepção
                
                while (received < fileSize) {
                    DatagramPacket packet = new DatagramPacket(
                        dataBuffer, dataBuffer.length
                    );
                    socket.receive(packet);
                    
                    int packetSize = packet.getLength();
                    if (received + packetSize > fileSize) {
                        packetSize = (int) (fileSize - received);
                    }
                    
                    fos.write(dataBuffer, 0, packetSize);
                    received += packetSize;
                }
            }
            
            System.out.println("Arquivo baixado com sucesso: " + receivedFilename);
            return file;
            
        } catch (IOException e) {
            System.err.println("Erro ao baixar arquivo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public String getDownloadDir() {
        return downloadDir;
    }
}


