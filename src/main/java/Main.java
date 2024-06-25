import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
  public static void main(String[] args) {
      //initializers
      ServerSocket serverSocket = null;
      Socket clientSocket = null;
      String directory="";
      String Echo= "HTTP/1.1 200 OK\r\n\r\n";
      String NotFound = "HTTP/1.1 404 Not Found\r\n\r\n";
      String Respond200 = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:";
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
              OutputStream writer = clientSocket.getOutputStream();
              //Read the request
              String req;
              ArrayList<String> HttpReq = new ArrayList<String>();

              //read request completely HTTP requests don't end with EOF but with blank line.
              while (!(req = clientIn.readLine()).equals(""))
                  HttpReq.add(req);

              System.out.println(HttpReq);
              //Striping URL from the HTTP req
              String URL[] = HttpReq.get(0).split(" ", 0);
              System.out.println(URL[1]);
              if (URL[0].equals("POST")) {
                  StringBuffer data=new StringBuffer();
                  while(clientIn.ready()){
                     data.append((char)clientIn.read());
                 }
                  String body = data.toString();
                  Path path = Paths.get(directory,URL[1].split("/")[2]);
                  Files.write(path,body.getBytes());
                  writer.write("HTTP/1.1 201 Created\r\n\r\n".getBytes(StandardCharsets.UTF_8));
              }
              else
              {
                  String URL_FULL[] = URL[1].split("/");
                  String  url_prfix;
                  if(URL_FULL.length==0)
                      url_prfix= "";
                  else
                      url_prfix=URL_FULL[1];
                  switch (url_prfix){
                      case "":
                          writer.write(Echo.getBytes(StandardCharsets.UTF_8));
                          break;
                      case "echo":
                          String path[] = URL[1].split("/", 0);
                          Respond200=Respond200 + path[2].length() + "\r\n\r\n" + path[2];
                          writer.write(Respond200.getBytes());
                          break;
                      case "files":
                          String filename = URL[1].split("/", 0)[2];
                          File file = new File(directory, filename);
                          if (file.exists()) {
                              //reading byte content
                              byte[] fileContent = Files.readAllBytes(file.toPath());
                              String httpResponse =
                                      "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " +
                                              fileContent.length + "\r\n\r\n" + new String(fileContent);
                              writer.write(httpResponse.getBytes(StandardCharsets.UTF_8));
                          } else {

                              writer.write(NotFound.getBytes());
                          }
                          break;
                      case "user-agent":
                          String user_agent[] = new String[2];
                          for (String s : HttpReq) {
                              if (s.startsWith("User-Agent"))
                                  user_agent = s.split(": ");
                          }
                          Respond200+= user_agent[1].length() + "\r\n\r\n" + user_agent[1];
                          writer.write(Respond200.getBytes());
                          break;
                      default:
                          writer.write(NotFound.getBytes());
                  }
                  clientSocket.close();
              }
          }
      } catch(IOException e){
              System.out.println("IOException: " + e.getMessage());
      }

  }
}
