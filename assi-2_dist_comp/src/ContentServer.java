import java.io.*;
import java.net.*;

public class ContentServer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ContentServer <server:port> <weatherfile>");
            return;
        }

        String serverInfo = args[0];   // server address and port, e.g. "localhost:4567"
        String dataFile = args[1];     // weather data filename

        try {
            // Read local weather file (basic)
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            // Connect to server (simple socket)
            String[] parts = serverInfo.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            Socket socket = new Socket(host, port);

            // TODO: Prepare and send PUT request to server

            // Close socket after operations
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
