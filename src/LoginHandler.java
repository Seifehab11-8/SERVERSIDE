import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class LoginHandler implements HttpHandler{
    private Database database;
    public LoginHandler(Database database){
        this.database = database;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if("POST".equals(method)) {
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody()));
            StringBuilder buildRequestBody = new StringBuilder();
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                buildRequestBody.append(line);
            }
            String requestBody = buildRequestBody.toString();
            User user = new Gson().fromJson(requestBody, User.class);
            //------------debug section-------------
            System.out.println("LoginHandler : got \""+requestBody+"\"");
            System.out.println("username:"+user.username+",Pass:"+user.password_hash);
            //------------end of debug--------------
            int errorCode = 0;
            int statusNumber = 400;
            String response = null;
            Result result = database.login(user.username, user.password_hash);
            errorCode = result.getMsgNum();
            System.out.println("error code: "+errorCode);
            if(errorCode == 3) {
                response = result.getSingleString().orElse("default"); // Admin login
                response = "{" + response + "}"; // Wrap the response in curly braces to make it a valid JSON object
                System.out.println("response: "+response);
                user = new Gson().fromJson(response, User.class);
                System.out.println("user id: "+user.id+" user government: "+user.government);
                statusNumber = 200;
            } else if (errorCode == 4) {
<<<<<<< HEAD
                response = "{\"message\": \"FAIL\"}"; // Login failed
            }
            
            
=======
                response = "FAIL"; // Login failed
            }
            
>>>>>>> dc5d364d8f41c8be70e627537adacbfa511f0fd0
            /*
             * TODO: give the code to a function created by mohamed
             */
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            /*
             * TODO: give the code to a function created by ahmed essam
             */
            exchange.sendResponseHeaders(statusNumber, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        else{
            throw new UnsupportedOperationException("Unimplemented method 'handle'");
        }
    }
	
}
