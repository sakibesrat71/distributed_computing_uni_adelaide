import java.io.*;
import java.net.*;

public class GETClient {
    private static LamportClock lamportClock = new LamportClock();
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000; // 1 second

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: GETClient <server:port>");
            return;
        }

        String serverInfo = args[0];

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                sendGetRequest(serverInfo);
                break; // success
            } catch (IOException e) {
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    System.err.println("Max retries reached. Giving up.");
                } else {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    private static void sendGetRequest(String serverInfo) throws IOException {
        String[] parts = serverInfo.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            lamportClock.tick();

            StringBuilder request = new StringBuilder();
            request.append("GET /weather.json HTTP/1.1\r\n");
            request.append("User-Agent: GETClient/1.0\r\n");
            request.append("Lamport-Clock: ").append(lamportClock.get()).append("\r\n");
            request.append("\r\n");

            out.write(request.toString());
            out.flush();

            String statusLine = in.readLine();
            System.out.println("Server response: " + statusLine);

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
                            // ignore bad lamport clock
                        }
                    }
                }
            }

            char[] bodyChars = new char[contentLength];
            int read = in.read(bodyChars, 0, contentLength);
            if (read < contentLength) {
                System.err.println("Incomplete response body");
            }
            String jsonBody = new String(bodyChars);
            parseAndPrintJsonArray(jsonBody);
        }
    }

    // Existing parseAndPrintJsonArray and parseAndPrintJson methods unchanged...
    public static void parseAndPrintJsonArray(String json) {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            System.out.println("Invalid JSON format");
            return;
        }
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

    public static void parseAndPrintJson(String json) {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            System.out.println("Invalid JSON format");
            return;
        }
        json = json.substring(1, json.length() - 1).trim();
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
