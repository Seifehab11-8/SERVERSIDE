import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

public class addItem implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if("POST".equals(method)) {
            System.out.println("requested adding item");
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
            User quriedUser = new Gson().fromJson(commaSeparated, User.class);
            System.out.println("Queried ID : "+quriedUser.id+" Queried gov : "+quriedUser.government);
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                buildRequestBody.append(line);
            }
            String requestBody = buildRequestBody.toString();
            Item item = new Gson().fromJson(requestBody, Item.class);
            //------------debug section-------------
            System.out.println("addItem : got \""+requestBody+"\"");
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
        Pattern pattern = Pattern.compile("^/item/add\\?(.*)$");
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
