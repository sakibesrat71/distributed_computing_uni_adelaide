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

            // Lamport clock tick before sending request
            lamportClock.tick();

            // Build PUT request string
            StringBuilder request = new StringBuilder();
            request.append("PUT /weather.json HTTP/1.1\r\n");
            request.append("User-Agent: ContentServer/1.0\r\n");
            request.append("Content-Type: application/json\r\n");
            request.append("Content-Length: ").append(jsonPayload.length()).append("\r\n");
            request.append("Lamport-Clock: ").append(lamportClock.get()).append("\r\n");
            request.append("\r\n");
            request.append(jsonPayload);

            // Send PUT request
            out.write(request.toString());
            out.flush();

            // Read and print the first response line (status)
            String statusLine = in.readLine();
            System.out.println("Server response: " + statusLine);

            // Optionally read remaining headers or body here

            // Close connections
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Converts colon separated key:value pairs into JSON formatted string (naive)
    public  static String convertToJson(String fileContent) {
        StringBuilder json = new StringBuilder("{\n");
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            String[] kv = line.split(":", 2);
            if (kv.length != 2) {
                continue; // skip invalid lines
            }
            String key = kv[0].trim();
            String value = kv[1].trim();

            // Quote value if non-numeric and not boolean
            if (!value.matches("[-+]?[0-9]*\\.?[0-9]+") && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                value = "\"" + value + "\"";
            }

            json.append("  \"").append(key).append("\": ").append(value).append(",\n");
        }
        if (json.length() > 2)
            json.setLength(json.length() - 2); // Remove trailing comma and newline
        json.append("\n}");
        return json.toString();
    }
}
