import java.io.*;
import java.net.*;
import java.util.*;

public class AggregationServer {
    private static String storedJson = "{}";            // Stored weather JSON (initially empty)
    private static LamportClock lamportClock = new LamportClock();
    private static final Object lock = new Object();    // Lock for thread safety

    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
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

            // Get Lamport clock from header if present
            int receivedLamport = -1;
            if (headers.containsKey("Lamport-Clock")) {
                try {
                    receivedLamport = Integer.parseInt(headers.get("Lamport-Clock"));
                } catch (NumberFormatException e) {
                    // ignore malformed Lamport clock header
                }
            }

            synchronized (lock) {
                // Update Lamport clock
                if (receivedLamport >= 0) {
                    lamportClock.update(receivedLamport);
                }
                lamportClock.tick();
            }

            if (method.equals("GET")) {
                // Return stored JSON
                sendResponse(out, 200, "OK", storedJson);
            } else if (method.equals("PUT")) {
                // Read Content-Length header
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
                    // No content case
                    sendResponse(out, 204, "No Content", "");
                    socket.close();
                    return;
                }

                // Read JSON content (exact contentLength bytes)
                char[] body = new char[contentLength];
                int read = in.read(body, 0, contentLength);
                if (read != contentLength) {
                    sendResponse(out, 500, "Internal Server Error", "Incomplete body");
                    socket.close();
                    return;
                }
                String jsonBody = new String(body);

                // Basic JSON validation (very basic, can be improved)
                if (!jsonBody.trim().startsWith("{") || !jsonBody.trim().endsWith("}")) {
                    sendResponse(out, 500, "Internal Server Error", "Invalid JSON");
                    socket.close();
                    return;
                }

                synchronized (lock) {
                    boolean firstTime = storedJson.equals("{}");
                    storedJson = jsonBody;
                    // Increment Lamport clock for update
                    lamportClock.tick();
                    // Send 201 if first time, else 200
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

    private static void sendResponse(BufferedWriter out, int statusCode, String statusMessage, String body) throws IOException {
        out.write("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        out.write("Content-Length: " + body.length() + "\r\n");
        out.write("Content-Type: application/json\r\n");
        out.write("Lamport-Clock: " + lamportClock.get() + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }
}
