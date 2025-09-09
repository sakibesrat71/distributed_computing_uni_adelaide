import java.io.*;
import java.net.*;
import java.util.Arrays;

public class AggregationServer {
    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Basic placeholder for handling the client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void handleClient(Socket socket) {
        // TODO: Read/write from socket streams, parse request (GET/PUT)
    }
}
