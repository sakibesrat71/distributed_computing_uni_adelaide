import java.io.*;
import java.net.*;

public class ContentServer {
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ContentServer <server:port> <weatherfile>");
            return;
        }

        String serverInfo = args[0];
        String dataFile = args[1];

        try {
            // Read weather data file contents
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            String jsonPayload = convertToJson(content.toString().trim());
            if (jsonPayload == null) {
                System.out.println("Error converting file to JSON.");
                return;
            }

            String[] parts = serverInfo.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            Socket socket = new Socket(host, port);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Update Lamport clock for send event
            lamportClock.tick();

            // Build PUT request
            StringBuilder request = new StringBuilder();
            request.append("PUT /weather.json HTTP/1.1\r\n");
            request.append("User-Agent: ContentServer/1.0\r\n");
            request.append("Content-Type: application/json\r\n");
            request.append("Content-Length: ").append(jsonPayload.length()).append("\r\n");
            request.append("Lamport-Clock: ").append(lamportClock.get()).append("\r\n");
            request.append("\r\n");
            request.append(jsonPayload);

            // Send request
            out.write(request.toString());
            out.flush();

            // Read response status line
            String statusLine = in.readLine();
            System.out.println("Server response: " + statusLine);

            // Optionally, read and print headers or body here as needed

            // Close connections
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Simple method to convert key:value pairs to JSON string format
    // For the assignment, you could extend or replace this with a JSON library later
    private static String convertToJson(String fileContent) {
        StringBuilder json = new StringBuilder("{\n");
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            String[] kv = line.split(":", 2);
            if (kv.length != 2) {
                continue; // ignore bad lines
            }
            String key = kv[0].trim();
            String value = kv[1].trim();

            // Quote value if it is non-numeric or contains spaces
            if (!value.matches("[-+]?\\d*\\.?\\d+") && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                value = "\"" + value + "\"";
            }

            json.append("  \"").append(key).append("\": ").append(value).append(",\n");
        }
        if (json.length() > 2) {
            json.setLength(json.length() - 2); // remove last comma
        }
        json.append("\n}");
        return json.toString();
    }
}
