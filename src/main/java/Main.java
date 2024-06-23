import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage


     ServerSocket serverSocket = null;
     Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.
            //client side conversion of bytes into data.
            BufferedReader  clientIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            //Read the request
            String req = clientIn.readLine();
            System.out.println(req);
            String HttpReq[] = req.split(" ",0);
            if(HttpReq[1].equals("/")) {
                String resposne = "HTTP/1.1 200 OK\r\n\r\n";
                clientSocket.getOutputStream().write(resposne.getBytes());
            }
            else{
                String resposne = "HTTP/1.1 400 Not Found\r\n\r\n";
                clientSocket.getOutputStream().write(resposne.getBytes());
            }
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
    }
  }
}
