import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
public class EditItem implements HttpHandler{

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if("PUT".equals(method)) {
            System.out.println("requested editing item");
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody()));
            StringBuilder buildRequestBody = new StringBuilder();
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                buildRequestBody.append(line);
            }
            String requestBody = buildRequestBody.toString();
            Item item = new Gson().fromJson(requestBody, Item.class);
            //------------debug section-------------
            System.out.println("editItem : got \""+requestBody+"\"");
            System.out.println(item.name+","+item.user_id+","+item.price+","+item.quantity);
            //------------end of debug--------------
            int errorCode = 0;
            String response = null;
            /*
             * TODO: give the code to a function created by mohamed
             */
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            /*
             * TODO: give the code to a function created by ahmed essam
             */
            exchange.sendResponseHeaders(errorCode, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        else{
            throw new UnsupportedOperationException("Unimplemented method 'handle'");
        }
    }
    
}
