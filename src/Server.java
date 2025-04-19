import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;




public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/greet", new GreetHandler());
        server.createContext("/api/echo", new EchoHandler());
        server.createContext("/dyn", new DynamicHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/item/add", new addItem());
        //server.createContext("/json", new JSONHandler());
	server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8080...");
    }
}