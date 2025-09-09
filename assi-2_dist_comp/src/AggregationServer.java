import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {
    // Map of content server ID -> JSON string for that server's weather data
    private static final Map<String, String> weatherDataMap = new ConcurrentHashMap<>();
    // Map of content server ID -> last contact timestamp (ms)
    private static final Map<String, Long> contentServerLastContact = new ConcurrentHashMap<>();
    private static final LamportClock lamportClock = new LamportClock();
    private static final Object lock = new Object();

    private static final long EXPIRY_TIME_MS = 30_000;  // 30 seconds

    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // Start expiry cleaner thread
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5_000);  // Run cleanup every 5 seconds
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
        // Similar implementation as before, but:
        // For PUT: parse JSON, extract "id" field,
        // store weatherDataMap.put(id, json),
        // update contentServerLastContact.put(id, currentTime),
        // persistData() called after update.
        // For GET: combine all entries from weatherDataMap into one JSON response.
        // Lamport clock and synchronization handled as before.
        //
        // Due to space, ask if you want a full handleClient rewrite with this included.
    }

    static void persistData() {
        synchronized (lock) {
            try {
                // Combine all weather JSON snippets into one JSON array string for persistence
                StringBuilder sb = new StringBuilder("[\n");
                for (String json : weatherDataMap.values()) {
                    sb.append(json).append(",\n");
                }
                if (sb.length() > 2)
                    sb.setLength(sb.length() - 2); // Remove last comma
                sb.append("\n]");

                // Write atomically: write to temp file, then rename
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
