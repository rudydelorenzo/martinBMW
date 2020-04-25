//Copyright © 2020 - Rudy de Lorenzo
package martinBMW;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class martinServer {
    
    public static ServerSocket serverSocket;
    public static Socket clientSocket;
    
    public static void startServer() {
        try {
            serverSocket = new ServerSocket(4444);

            System.out.println("Server running!");
            
            while (true) {
                clientSocket = null;
                clientSocket = serverSocket.accept();
                System.out.println("Connected to " + clientSocket.getInetAddress());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                
                Thread t = new ClientHandler(clientSocket, in, out);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
