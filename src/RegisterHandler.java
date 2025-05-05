import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class RegisterHandler implements HttpHandler {
    private Database database;
    public RegisterHandler(Database database) {
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
            // error in type conversion
            User user = new Gson().fromJson(requestBody, User.class);

            //------------debug section-------------
            System.out.println("RegisterHandler : got \""+requestBody+"\"");
            System.out.println("id:"+user.id+"username:"+user.username+","+user.balance+","+user.government+","+user.password_hash);
            //------------end of debug--------------
            
            /*
             * TODO: give the code to a function created by ahmed essam
             */
            Result result = database.createAccount(user.username, user.password_hash, user.government, user.balance);
            int errorCode = result.getMsgNum();
            int statusNumber = 400;
            
            String response = null;
            if (errorCode == 1) {
                response = "{\"message\": \"SUCCESS\"}";
                statusNumber = 200;
            } else {
                response = "{\"message\": \"FAIL\"}";
            }
            
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            
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
