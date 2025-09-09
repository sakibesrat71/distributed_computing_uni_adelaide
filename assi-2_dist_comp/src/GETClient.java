import java.io.*;
import java.net.*;
import java.util.*;

public class GETClient {
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: GETClient <server:port> [stationID]");
            return;
        }

        String serverInfo = args[0];
        String stationID = (args.length > 1) ? args[1] : null;

        try {
            String[] parts = serverInfo.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            Socket socket = new Socket(host, port);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Update Lamport clock for send event
            lamportClock.tick();

            // Build GET request, optionally with stationID parameter (simple)
            StringBuilder request = new StringBuilder();
            request.append("GET /weather.json HTTP/1.1\r\n");
            request.append("User-Agent: GETClient/1.0\r\n");
            request.append("Lamport-Clock: ").append(lamportClock.get()).append("\r\n");
            request.append("\r\n");

            out.write(request.toString());
            out.flush();

            // Read status line
            String statusLine = in.readLine();
            System.out.println("Server response: " + statusLine);

            // Read headers until empty line
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).equals("")) {
                int sep = line.indexOf(":");
                if (sep != -1) {
                    String key = line.substring(0, sep).trim();
                    String value = line.substring(sep + 1).trim();
                    headers.put(key, value);
                }
            }
            if (headers.containsKey("Content-Length")) {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            }

            // Read JSON response body
            char[] bodyChars = new char[contentLength];
            int read = in.read(bodyChars, 0, contentLength);
            if (read < contentLength) {
                System.err.println("Incomplete response body");
                socket.close();
                return;
            }
            String jsonBody = new String(bodyChars);

            // Update Lamport clock based on response header if present
            if (headers.containsKey("Lamport-Clock")) {
                try {
                    int receivedLamport = Integer.parseInt(headers.get("Lamport-Clock"));
                    lamportClock.update(receivedLamport);
                } catch (NumberFormatException e) {
                    // ignore invalid Lamport clock header
                }
            }

            // Parse and print JSON attributes line-by-line
            parseAndPrintJson(jsonBody);

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseAndPrintJson(String json) {
        // Basic parsing assuming flat JSON object with string/number values
        // If JSON libraries are allowed, use them instead
        // A very naive parser:
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            System.out.println("Invalid JSON format");
            return;
        }
        json = json.substring(1, json.length() - 1).trim(); // remove braces
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", ""); // remove quotes
                String value = kv[1].trim().replaceAll("^\"|\"$", "");
                System.out.println(key + ": " + value);
            }
        }
    }
}
