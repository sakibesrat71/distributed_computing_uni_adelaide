import java.io.*;
import java.net.*;

public class GETClient {
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: GETClient <server:port> [stationID]");
            return;
        }

        String serverInfo = args[0];
        // stationID currently not implemented, but can be used to filter

        try {
            String[] parts = serverInfo.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            Socket socket = new Socket(host, port);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            lamportClock.tick();

            // Build GET request string
            StringBuilder request = new StringBuilder();
            request.append("GET /weather.json HTTP/1.1\r\n");
            request.append("User-Agent: GETClient/1.0\r\n");
            request.append("Lamport-Clock: ").append(lamportClock.get()).append("\r\n");
            request.append("\r\n");

            out.write(request.toString());
            out.flush();

            String statusLine = in.readLine();
            System.out.println("Server response: " + statusLine);

            // Read headers
            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).equals("")) {
                int sep = line.indexOf(":");
                if (sep != -1) {
                    String key = line.substring(0, sep).trim();
                    String value = line.substring(sep + 1).trim();
                    if ("Content-Length".equalsIgnoreCase(key)) {
                        contentLength = Integer.parseInt(value);
                    } else if ("Lamport-Clock".equalsIgnoreCase(key)) {
                        try {
                            int receivedLamport = Integer.parseInt(value);
                            lamportClock.update(receivedLamport);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }

            // Read JSON body into char array
            char[] bodyChars = new char[contentLength];
            int read = in.read(bodyChars, 0, contentLength);
            if (read < contentLength) {
                System.err.println("Incomplete response body");
                socket.close();
                return;
            }
            String jsonBody = new String(bodyChars);

            parseAndPrintJsonArray(jsonBody);

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Naive parser: expects JSON array of objects, parses and prints each object's key-values line-by-line
     static void parseAndPrintJsonArray(String json) {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            System.out.println("Invalid JSON format");
            return;
        }
        // Remove [ ] and split objects by "}," (assuming no nested objects)
        String arrayContent = json.substring(1, json.length() - 1).trim();
        if (arrayContent.isEmpty()) {
            System.out.println("Empty weather data");
            return;
        }
        String[] objects = arrayContent.split("\\},\\s*\\{");

        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i].trim();
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            System.out.println("Weather Entry " + (i + 1) + ":");
            parseAndPrintJson(obj);
            System.out.println();
        }
    }

    // Similar naive parser for single JSON object
     static void parseAndPrintJson(String json) {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            System.out.println("Invalid JSON format");
            return;
        }
        json = json.substring(1, json.length() - 1).trim(); // remove braces
        // Split by commas not in quotes
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String value = kv[1].trim().replaceAll("^\"|\"$", "");
                System.out.println(key + ": " + value);
            }
        }
    }
}
