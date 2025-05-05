<<<<<<< HEAD
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.crypto.Data;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class MakeTransaction implements HttpHandler {
    private Database database;
    public MakeTransaction(Database database) {
        this.database = database;
        // Constructor can be used to initialize any required resources
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
            int statusNumber = 400;
            String response = null;
            // public Result buyItems(int buyerId, String buyerGovernment, int itemId, String sellerGovernment, int quantity) {
            Result result = database.buyItems(t.buyer_id, t.buyer_government, t.item_id, t.seller_government, t.quantity);
            switch(result.getMsgNum()) {
                case 16:
                    response = "{\"message\": \"TRANSACTION_SUCCESS\"}";
                    statusNumber = 200;
                    break;
                case 17:
                    response = "{\"message\": \"NOT_ENOUGH_QUANTITY\"}";
                    statusNumber = 400;
                    break;
                case 18:
                    response = "{\"message\": \"INSUFFICENT_BALANCE\"}";
                    statusNumber = 400;
                    break;
                case 19:
                    response = "{\"message\": \"ITEM_NOT_EXIST\"}";
                    statusNumber = 400;
                    break;
            }
            
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
=======
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.crypto.Data;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class MakeTransaction implements HttpHandler {
    private Database database;
    public MakeTransaction(Database database) {
        this.database = database;
        // Constructor can be used to initialize any required resources
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
            int statusNumber = 400;
            String response = null;
            // public Result buyItems(int buyerId, String buyerGovernment, int itemId, String sellerGovernment, int quantity) {
            Result result = database.buyItems(t.buyer_id, t.buyer_government, t.item_id, t.seller_government, t.quantity);
            switch(result.getMsgNum()) {
                case 16:
                response = "TRANSACTION_SUCCESS";
                statusNumber = 200;
                break;
                case 17:
                    response = "NOT_ENOUGH_QUANTITY";
                    statusNumber = 400;
                break;
                case 18:
                response = "INSUFFICENT_BALANCE";
                statusNumber = 400;
                break;
                case 19:
                response = "ITEM_NOT_EXIST";
                statusNumber = 400;
                break;
            }
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
>>>>>>> dc5d364d8f41c8be70e627537adacbfa511f0fd0
