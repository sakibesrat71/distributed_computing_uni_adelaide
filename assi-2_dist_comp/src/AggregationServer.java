import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class AggregationServer {
    // Maps to hold weather data and last contact timestamps
    private static final Map<String, String> weatherDataMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> contentServerLastContact = new ConcurrentHashMap<>();
    private static final LamportClock lamportClock = new LamportClock();
    private static final Object lock = new Object();

    private static final long EXPIRY_TIME_MS = 30_000;  // 30 seconds expiry

    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // Start cleaner thread to expire stale content servers
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5_000);
                    cleanExpiredEntries();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        boolean modified = false;
        for (Map.Entry<String, Long> entry : contentServerLastContact.entrySet()) {
            if (now - entry.getValue() > EXPIRY_TIME_MS) {
                String id = entry.getKey();
                System.out.println("Expiring data from content server: " + id);
                contentServerLastContact.remove(id);
                weatherDataMap.remove(id);
                modified = true;
            }
        }
        if (modified) {
            persistData();
        }
    }

    static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ) {
            // Read request line
            String requestLine = in.readLine();
            if (requestLine == null) {
                socket.close();
                return;
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                sendResponse(out, 400, "Bad Request", "Malformed request line");
                socket.close();
                return;
            }

            String method = requestParts[0];
            String resource = requestParts[1];

            // Read headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while (!(line = in.readLine()).equals("")) {
                int sep = line.indexOf(":");
                if (sep != -1) {
                    String key = line.substring(0, sep).trim();
                    String value = line.substring(sep + 1).trim();
                    headers.put(key, value);
                }
            }

            int receivedLamport = -1;
            if (headers.containsKey("Lamport-Clock")) {
                try {
                    receivedLamport = Integer.parseInt(headers.get("Lamport-Clock"));
                } catch (NumberFormatException e) {
                    // Ignore malformed Lamport clock header
                }
            }

            synchronized (lock) {
                if (receivedLamport >= 0) {
                    lamportClock.update(receivedLamport);
                }
                lamportClock.tick();
            }

            if (method.equals("GET")) {
                // Combine all weather JSON as a JSON array
                StringBuilder sb = new StringBuilder("[\n");
                for (String json : weatherDataMap.values()) {
                    sb.append(json).append(",\n");
                }
                if (sb.length() > 2)
                    sb.setLength(sb.length() - 2); // Remove trailing comma and newline
                sb.append("\n]");

                sendResponse(out, 200, "OK", sb.toString());

            } else if (method.equals("PUT")) {
                int contentLength = 0;
                if (headers.containsKey("Content-Length")) {
                    try {
                        contentLength = Integer.parseInt(headers.get("Content-Length"));
                    } catch (NumberFormatException e) {
                        sendResponse(out, 400, "Bad Request", "Invalid Content-Length");
                        socket.close();
                        return;
                    }
                }

                if (contentLength == 0) {
                    sendResponse(out, 204, "No Content", "");
                    socket.close();
                    return;
                }

                char[] body = new char[contentLength];
                int read = in.read(body, 0, contentLength);
                if (read != contentLength) {
                    sendResponse(out, 500, "Internal Server Error", "Incomplete body");
                    socket.close();
                    return;
                }
                String jsonBody = new String(body).trim();

                if (!jsonBody.startsWith("{") || !jsonBody.endsWith("}")) {
                    sendResponse(out, 500, "Internal Server Error", "Invalid JSON");
                    socket.close();
                    return;
                }

                String id = extractIdFromJson(jsonBody);
                if (id == null) {
                    sendResponse(out, 500, "Internal Server Error", "Missing id in JSON");
                    socket.close();
                    return;
                }

                synchronized (lock) {
                    boolean firstTime = !weatherDataMap.containsKey(id);
                    weatherDataMap.put(id, jsonBody);
                    contentServerLastContact.put(id, System.currentTimeMillis());
                    lamportClock.tick();
                    persistData();

                    if (firstTime) {
                        sendResponse(out, 201, "Created", "JSON stored");
                    } else {
                        sendResponse(out, 200, "OK", "JSON updated");
                    }
                }
            } else {
                sendResponse(out, 400, "Bad Request", "Method not supported");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractIdFromJson(String json) {
        Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static void sendResponse(BufferedWriter out, int statusCode, String statusMessage, String body)
            throws IOException {
        out.write("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        out.write("Content-Length: " + body.length() + "\r\n");
        out.write("Content-Type: application/json\r\n");
        out.write("Lamport-Clock: " + lamportClock.get() + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }

    private static void persistData() {
        synchronized (lock) {
            try {
                StringBuilder sb = new StringBuilder("[\n");
                for (String json : weatherDataMap.values()) {
                    sb.append(json).append(",\n");
                }
                if (sb.length() > 2)
                    sb.setLength(sb.length() - 2);
                sb.append("\n]");

                File tempFile = new File("weather_temp.json");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
                    bw.write(sb.toString());
                }

                File mainFile = new File("weather.json");
                if (!tempFile.renameTo(mainFile)) {
                    System.err.println("Failed to rename temp weather file");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
