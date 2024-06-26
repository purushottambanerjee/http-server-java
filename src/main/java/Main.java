import org.w3c.dom.DOMStringList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

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
              //Striping URL from the HTTP req
              String URL[] = HttpReq.get(0).split(" ", 0);

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
                  String  url_prfix,response;
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

                          response=Respond200+ path[2].length() + "\r\n\r\n" + path[2];

                          for (String s : HttpReq) {

                              if (s.startsWith("Accept-Encoding"))
                              {
                                  String  encoding[] =s.split(": ")[1].split(",");
                                  for(String encode:encoding) {
                                      if(encode.trim().startsWith("gzip"))
                                      {

                                          // Compress the response body using gzip
                                          ByteArrayOutputStream byteArrayOutputStream =
                                                  new ByteArrayOutputStream();
                                          try (GZIPOutputStream gzipOutputStream =
                                                       new GZIPOutputStream(byteArrayOutputStream)) {
                                              gzipOutputStream.write(path[2].getBytes("UTF-8"));
                                          }
                                          byte[] gzipData = byteArrayOutputStream.toByteArray();
                                          response = "HTTP/1.1 200 OK\r\nContent-Encoding:gzip" + "\r\nContent-Type: text/plain\r\n" +
                                                  "Content-Length:" + gzipData.length + "\r\n\r\n"+gzipData;
                                      }
                              }
                              }
                          }

                          writer.write(response.getBytes());
                          break;
                      case "files":
                          String filename = URL[1].split("/", 0)[2];
                          File file = new File(directory, filename);
                          if (file.exists()) {
                              //reading byte content
                              byte[] fileContent = Files.readAllBytes(file.toPath());
                              response =
                                      "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " +
                                              fileContent.length + "\r\n\r\n" + new String(fileContent);
                              writer.write(response.getBytes(StandardCharsets.UTF_8));
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
                          response=Respond200+ user_agent[1].length() + "\r\n\r\n" + user_agent[1];
                          writer.write(response.getBytes());
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
