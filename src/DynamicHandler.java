import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.regex.*;
import com.google.gson.Gson;

public class DynamicHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            // Extract dynamic part from the URL
            String path = exchange.getRequestURI().getPath();  // Get the entire URL path
                                        System.out.println(path);
            String dynamicPart = getDynamicPart(path);

            // Perform action based on the dynamic part (e.g., reverse it)
            String response = "{\"original\": \"" + dynamicPart + "\", \"reversed\": \"" + reverseString(dynamicPart) + "\"}";

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    // Method to extract the dynamic part from the URL path
    private String getDynamicPart(String path) {
        // Regex to match /api/{dynamicPart}
        Pattern pattern = Pattern.compile("/dyn/([^/]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);  // Extract the dynamic part
        }
        return "";
    }

    // Method to reverse the string
    private String reverseString(String input) {
        StringBuilder reversed = new StringBuilder(input);
        return reversed.reverse().toString();
    }
}