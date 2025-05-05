import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class ViewItems implements HttpHandler {
    private Database database;
    public ViewItems(Database database) {
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
            Result result = database.viewItemsForSale(quriedUser.id, quriedUser.government);
            int statusNumber = 400;
            String response = null;
            switch(result.getMsgNum()) {
                case 14:
                    statusNumber = 200;
                    String[] itemArr = result.getMultipleStrings().orElse(null);
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\n");
                    sb.append("  \"items\": [\n");
                    for (int i = 0; i < itemArr.length; i++) {
                        sb.append("    {").append(itemArr[i]).append("}");
                        if (i < itemArr.length - 1) sb.append(",\n");
                    }
                    sb.append("\n  ]\n");
                    sb.append("}\n");
                    System.out.println(sb.toString());
                    response = sb.toString();
                    break;
                case 15:
                    response = "{\"message\": \"ERROR\"}";
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