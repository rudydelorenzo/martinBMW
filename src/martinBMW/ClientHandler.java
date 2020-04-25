package martinBMW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {
    final DataInputStream in; 
    final DataOutputStream out; 
    final Socket s;
    
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) { 
        this.s = s; 
        this.in = dis; 
        this.out = dos;
    } 
  
    @Override
    public void run() { 
        String received; 
        while (true) { 
            try {
                // receive the answer from client 
                received = in.readUTF(); 
                
                System.out.println(received);
                
                
                
                // write on output stream based on the input from the client
                switch (received) { 
                    default:
                        out.writeUTF("success:Success"); 
                        this.in.close();
                        this.out.close();
                        break;
                } 
            } catch (SocketException e) {
                System.out.println("Socket Closed");
                break;
            } catch (IOException e) { 
                e.printStackTrace(); 
            }
        } 
    }
}