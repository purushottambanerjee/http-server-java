import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class Main {
  public static void main(String[] args) {
      //initializers
      ServerSocket serverSocket = null;
      Socket clientSocket = null;
      String directory="";
      if(args.length>1 && args[0].equals("--directory")){
          directory=args[1];
          System.out.println(args[1]);
      }
      try {
          //creating new server Socket
          serverSocket = new ServerSocket(4221);
          serverSocket.setReuseAddress(true);

          while (true) {

              clientSocket = serverSocket.accept(); // Wait for connection from client.
              //client side conversion of bytes into data.
              BufferedReader clientIn = new BufferedReader(
                      new InputStreamReader(clientSocket.getInputStream()));

              //Read the request
              String req;
              ArrayList<String> HttpReq = new ArrayList<String>();

              //read request completely HTTP requests don't end with EOF but with blank line.
              while (!(req = clientIn.readLine()).equals(""))
                  HttpReq.add(req);

              System.out.println(HttpReq);
              //Striping URL from the HTTP req
              String URL[] = HttpReq.get(0).split(" ", 0);
              if (URL[1].equals("/"))
              {
                  String resposne = "HTTP/1.1 200 OK\r\n\r\n";
                  clientSocket.getOutputStream().write(resposne.getBytes());

              }
              else if (URL[1].startsWith("/echo/"))
              {
                  String path[] = URL[1].split("/", 0);
                  String resposne = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + path[2].length() + "\r\n\r\n" + path[2];
                  clientSocket.getOutputStream().write(resposne.getBytes());
              }
              else if (URL[1].startsWith("/user-agent"))
              {
                  String user_agent[] = new String[2];
                  for (String s : HttpReq) {
                      if (s.startsWith("User-Agent"))
                          user_agent = s.split(": ");
                  }
                  String resposne = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + user_agent[1].length() + "\r\n\r\n" + user_agent[1];
                  clientSocket.getOutputStream().write(resposne.getBytes());
              }
              else if(URL[1].startsWith("/files")){
                  String filename = URL[1].split("/")[1];
                  File file = new File(directory,filename);
                  System.out.println(file.toPath());
                  if(file.exists()){
                      //reading byte content
                      byte[] fileContent = Files.readAllBytes(file.toPath());
                      String httpResponse =
                              "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " +
                                      fileContent.length + "\r\n\r\n" + new String(fileContent);
                      clientSocket.getOutputStream().write(httpResponse.getBytes(StandardCharsets.UTF_8));
                  }
                  else {
                      String resposne = "HTTP/1.1 404 Not Found\r\n\r\n";
                      clientSocket.getOutputStream().write(resposne.getBytes());
                  }
              }
              else {
                  String resposne = "HTTP/1.1 404 Not Found\r\n\r\n";
                  clientSocket.getOutputStream().write(resposne.getBytes());
              }
              clientSocket.close();
              System.out.println("accepted new connection");
          }
      } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
      }
  }
}
