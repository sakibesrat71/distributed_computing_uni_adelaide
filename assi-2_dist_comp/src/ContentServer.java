import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class ContentServer {
    private static final LamportClock lamportClock = new LamportClock();
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000; // 1 second
    private static final long RESEND_INTERVAL_MS = 15000; // 15 seconds, resend backups
    private static final String BACKUP_FILE = "weather_backup.txt";

    private static String serverInfo;
    private static String dataFile;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ContentServer <server:port> <weatherfile>");
            return;
        }

        serverInfo = args[0];
        dataFile = args[1];

        // Start background resend task to handle failed updates
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(ContentServer::resendBackup, RESEND_INTERVAL_MS, RESEND_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // Initial send from file
        sendWeatherDataFromFile(dataFile);

        // You can add watch or manual triggers to reread dataFile periodically or on update
    }

    private static void sendWeatherDataFromFile(String filePath) {
        try {
            String content = readFile(filePath);
            saveBackup(content);
            String jsonPayload = convertToJson(content);
            if (jsonPayload == null) {
                System.err.println("Failed to convert input to JSON.");
                return;
            }

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    sendPutRequest(serverInfo, jsonPayload);
                    deleteBackup();
                    break;
                } catch (IOException e) {
                    System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                    if (attempt == MAX_RETRIES) {
                        System.err.println("Max retries reached. Backup retained for retry.");
                    } else {
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error sending weather data: " + e.getMessage());
        }
    }

    private static void resendBackup() {
        try {
            if (!Files.exists(Path.of(BACKUP_FILE))) {
                return;
            }
            String content = readFile(BACKUP_FILE);
            System.out.println("Resending backup data...");
            sendWeatherDataFromFile(BACKUP_FILE);  // This tries to send and delete backup on success
        } catch (Exception e) {
            System.err.println("Error resending backup: " + e.getMessage());
        }
    }

    private static void sendPutRequest(String serverInfo, String jsonPayload) throws IOException {
        String[] parts = serverInfo.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            lamportClock.tick();

            StringBuilder request = new StringBuilder();
            request.append("PUT /weather.json HTTP/1.1\r\n");
            request.append("User-Agent: ContentServer/1.0\r\n");
            request.append("Content-Type: application/json\r\n");
            request.append("Content-Length: ").append(jsonPayload.length()).append("\r\n");
            request.append("Lamport-Clock: ").append(lamportClock.get()).append("\r\n");
            request.append("\r\n");
            request.append(jsonPayload);

            out.write(request.toString());
            out.flush();

            String statusLine = in.readLine();
            System.out.println("Server response: " + statusLine);
        }
    }

    private static String readFile(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }

    private static void saveBackup(String content) throws IOException {
        Files.writeString(Path.of(BACKUP_FILE), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void deleteBackup() throws IOException {
        Files.deleteIfExists(Path.of(BACKUP_FILE));
    }

    public static String convertToJson(String fileContent) {
        if (fileContent == null || fileContent.isEmpty()) return null;
        StringBuilder json = new StringBuilder("{\n");
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            String[] kv = line.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            String value = kv[1].trim();

            if (!value.matches("[-+]?[0-9]*\\.?[0-9]+") &&
                    !value.equalsIgnoreCase("true") &&
                    !value.equalsIgnoreCase("false")) {
                value = "\"" + value + "\"";
            }
            json.append("  \"").append(key).append("\": ").append(value).append(",\n");
        }
        if (json.length() > 2) json.setLength(json.length() - 2);
        json.append("\n}");
        return json.toString();
    }
}
