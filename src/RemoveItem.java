import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class RemoveItem implements HttpHandler {
    private Database database;
    public RemoveItem(Database database) {
        this.database = database;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if("DELETE".equals(method)) {
            System.out.println("requested deleting item");
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody()));
            StringBuilder buildRequestBody = new StringBuilder();
            String path = exchange.getRequestURI().getPath();  // Get the entire URL path
            System.out.println(path);
            String query = exchange.getRequestURI().getQuery();
            System.out.println(query);
            //String dynamicPart = getDynamicPart(path);
            //System.out.println("dynamic part : "+dynamicPart); //flag
            String commaSeparated = parseQueryString(query);
            //System.out.println("comma separated part : "+commaSeparated);
            DeleteObject deleteObject = new Gson().fromJson(commaSeparated, DeleteObject.class);
            System.out.println("Queried ID : "+deleteObject.id+" Queried gov : "+deleteObject.government);
            User user = new User();
            user.id = deleteObject.id;
            user.government = deleteObject.government;
            Item item = new Item();
            item.name = deleteObject.itemName;
            System.out.println("UserName : "+user.id+", UserGovernment : "+user.government+
                ", ItemName : "+item.name);
                int statusNumber = 400;
                String response = null;
                /*
                 * TODO: give the code to a function created by mohamed
                 */
                Result result = database.deleteItem(user.id, user.government, item.name);
                switch(result.getMsgNum()) {
                    case 9:
                    response = "ITEM_NOT_FOUND";
                    statusNumber = 400;
                    break;
                    case 11:
                        response = "SUCCESS";
                        statusNumber = 200;
                    break;
                }
                
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
        Pattern pattern = Pattern.compile("^/item/remove\\?(.*)$");
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
