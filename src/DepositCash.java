import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class DepositCash implements HttpHandler {
    private Database database;
    public DepositCash(Database database) {
        this.database = database;
    }
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("PUT".equals(method)) {
            String query = exchange.getRequestURI().getQuery();
            System.out.println(query);
            String commaSeparated = parseQueryString(query);
            commaSeparated = commaSeparated.replace("deposit", "balance");
            User quriedUser = new Gson().fromJson(commaSeparated, User.class);
            System.out.println("Queried ID : "+quriedUser.id+", Queried gov : "
                +quriedUser.government+", Queried Balance : "+quriedUser.balance);
            Result result = database.depositCash(quriedUser.id, quriedUser.government, quriedUser.balance);
            
            int statusNumber = 400;
            String response = null;
            /*
             * TODO: give the code to a function created by mohamed
             */
            switch(result.getMsgNum()) {
                case 12:
                response = "NEGATIVE_DEPOSIT_EXCEPTION";
                statusNumber = 400;
                break;
                case 13:
                    response = "DEPOSIT_SUCCESS";
                    statusNumber = 200;
                break;
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusNumber, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
    public static String parseQueryString(String query) {
            // if (query.startsWith("?")) {
            //     query = query.substring(1);
            // }
    
            String[] pairs = query.split("&");
            StringBuilder result = new StringBuilder();
            result.append("{");
    
            for (int i = 0; i < pairs.length; i++) {
                String[] keyValue = pairs[i].split("=", 2);
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";
    
                // Wrap value in quotes if it's not a number
                if (!value.matches("-?\\d+(\\.\\d+)?")) {
                    value = "\"" + value + "\"";
                }
    
                result.append("\""+key+"\"").append(":").append(value);
                if (i < pairs.length - 1) {
                    result.append(", ");
                }
            }
            result.append("}");
    
            return result.toString();
        }
        private String getDynamicPart(String path) {
        // Regex to match /api/{dynamicPart}
        // Pattern pattern = Pattern.compile("/item/add([?].+)");
        // //http://example.com/item/add?name=alice&age=30
        // Matcher matcher = pattern.matcher(path);
        // if (matcher.find()) {
        //     return matcher.group(1);  // Extract the dynamic part
        // }
        // return "";
        //String Path = "/item/add?name=alice&age=30";
        Pattern pattern = Pattern.compile("^/item/edit\\?(.*)$");
        Matcher matcher = pattern.matcher(path);

        if (matcher.find()) {
            String queryString = matcher.group(1);
            System.out.println("Query part: " + queryString);
            return queryString;
        } else {
            System.out.println("No match!");
            return "";
        }
    }
}