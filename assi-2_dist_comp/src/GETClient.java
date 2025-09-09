import java.io.*;
import java.net.*;
import java.util.Arrays;

public class GETClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: GETClient <server:port> [stationID]");
            return;
        }

        String serverInfo = args[0];  // Just the server:port string
        String stationID = (args.length > 1) ? args[1] : null;

        try {
            String[] parts = serverInfo.split(":");
            String host = parts[0];                  // Hostname string, e.g. "localhost"
            int port = Integer.parseInt(parts[1]);  // Port number, e.g. 4567
            Socket socket = new Socket(host, port);

            // TODO: Send GET request, receive and print response

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
