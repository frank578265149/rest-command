package com.everdata.server;

import java.io.IOException;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.core.UriBuilder;

public class PublicService {
   public static URI getBaseURI(){
	   return UriBuilder.fromUri("http://localhost/").port(9999).build();
	   
   }
   public static final URI BASE_URI = getBaseURI();
   protected static HttpServer startServer(){
	   System.out.println("Start server");
	   HttpServer httpServer=null;
	   ResourceConfig config=new PackagesResourceConfig("com.everdata.server.resource");
	   try{

		   httpServer= GrizzlyServerFactory.createHttpServer(BASE_URI, config);
	   }catch(IOException e){
		   System.out.print("ddddddddddddddddd");
	   }
	   return  httpServer;
   }
  public static void main(String[] args) {
	 try{
		 HttpServer httpServer = startServer();
		 System.out.println(String.format("Jersey app started with WADL available at" +
				 "%sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...", BASE_URI, BASE_URI));
		 System.in.read();
		 httpServer.stop();
		 
	 }catch(IllegalArgumentException | NullPointerException | IOException e){
		 e.printStackTrace();
	 }
}
}
