import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class ViewAccountInfo implements HttpHandler {
    private Database database;
    public ViewAccountInfo(Database database) {
        this.database = database;
    }
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            String query = exchange.getRequestURI().getQuery();
            System.out.println(query);
            String commaSeparated = parseQueryString(query);
            User quriedUser = new Gson().fromJson(commaSeparated, User.class);
            System.out.println("Queried ID : "+quriedUser.id+" Queried gov : "+quriedUser.government);
            Result result = database.getAccountInfo(quriedUser.id, quriedUser.government);
            int statusNumber = 400;
            String response = null;
            StringBuilder sb = new StringBuilder();
            switch(result.getMsgNum()) {
                case 20:
                // String[] accountInfo = accountInfoResult.getStringArray().orElse(new String[0]);
                // System.out.println("Account Info:");
                // System.out.println("Name: " + accountInfo[0]);
                // System.out.println("Password: " + accountInfo[1]);
                // System.out.println("Government: " + accountInfo[2]);
                // System.out.println("Balance: " + accountInfo[3]);
                String[] itemArr = result.getMultipleStrings().orElse(null);
                sb.append("{\n");
                sb.append("  \"user\": {\n");
                sb.append("    \"name\": \"").append(itemArr[0]).append("\",\n");
                sb.append("    \"password\": \"").append(itemArr[1]).append("\",\n");
                sb.append("    \"government\": \"").append(itemArr[2]).append("\",\n");
                sb.append("    \"balance\": \"").append(itemArr[3]).append("\"\n");
                sb.append("  }\n");
                System.out.println(sb.toString());
                statusNumber = 200;
                break;
                case 27:
                    response = "{\"message\": \"NO_ACCESS\"}";
                    statusNumber = 400;
                    break;
            }
            if(statusNumber == 200) {
                Result result2 = database.getSoldItems(quriedUser.id, quriedUser.government);
                String[] soldItems = result2.getMultipleStrings().orElse(null);
                if(soldItems != null){
                    sb.append("  \"sold_items\": [");
                    for (int i = 0; i < soldItems.length; i++) {
                        sb.append("{").append(soldItems[i]).append("}");
                        if (i < soldItems.length - 1) sb.append(", ");
                    }
                    sb.append("],\n");
                }
                else {
                    sb.append("  \"sold_items\": [");
                    sb.append("],\n");
                }
                Result result3 = database.getPurchasedItems(quriedUser.id, quriedUser.government);
                String[] buyItems = result3.getMultipleStrings().orElse(null);
                if(buyItems != null){
                    sb.append("  \"purchased_items\": [");
                    for (int i = 0; i < buyItems.length; i++) {
                        sb.append("{").append(buyItems[i]).append("}");
                        if (i < buyItems.length - 1) sb.append(", ");
                    }
                    sb.append("],\n");
                }
                else {
                    sb.append("  \"purchased_items\": [");
                    sb.append("],\n");
                }
                sb.append("}\n");
                System.out.println(sb.toString());
            }
            response = sb.toString();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusNumber, response.length());
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