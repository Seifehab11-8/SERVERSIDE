import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class MakeTransaction implements HttpHandler {
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
            Transaction t = new Gson().fromJson(requestBody, Transaction.class);
            //------------debug section-------------
            System.out.println("MakeTransaction : got \""+requestBody+"\"");
            System.out.print("json obj:");
            System.out.println(
                "buyer_id=" + t.buyer_id + ", " +
                "buyer_government=" + t.buyer_government + ", " +
                "seller_id=" + t.seller_id + ", " +
                "seller_government=" + t.seller_government + ", " +
                "item_id=" + t.item_id + ", " +
                "item_government=" + t.item_government + ", " +
                "item_name=" + t.item_name + ", " +
                "quantity=" + t.quantity + ", " +
                "amount=" + t.amount + ", " +
                "transaction_date=" + t.transaction_date
            );
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
