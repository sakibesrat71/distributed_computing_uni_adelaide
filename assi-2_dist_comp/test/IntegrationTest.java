import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {

    private ExecutorService executor;
    private final int port = 4567;

    @BeforeAll
    public void startServer() throws Exception {
        executor = Executors.newCachedThreadPool();

        // Start AggregationServer in a thread
        executor.submit(() -> {
            AggregationServer.main(new String[]{String.valueOf(port)});
        });

        // Give server time to start
        Thread.sleep(1000);
    }

    @AfterAll
    public void stopServer() {
        executor.shutdownNow();
    }

    @Test
    public void testConcurrentPutAndGet() throws Exception {
        // Launch multiple ContentServer PUTs concurrently with different IDs
        Runnable client1 = () -> ContentServer.main(new String[]{"localhost:" + port, "weather_input.txt"});
        // Runnable client2 = () -> ContentServer.main(new String[]{"localhost:" + port, "weather_input2.txt"});

        executor.submit(client1);
        // executor.submit(client2);

        // Wait a moment for PUTs to be processed
        Thread.sleep(2000);

        // Run GETClient and capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        System.setOut(new PrintStream(outContent));
        GETClient.main(new String[]{"localhost:" + port});
        System.setOut(originalOut);

        String output = outContent.toString();
        System.out.println("GETClient output:\n" + output);
        assertTrue(output.contains("id:")); // basic validation for JSON keys printed
        assertTrue(output.contains("weather_entry") || output.toLowerCase().contains("temp")); // adjust check based on actual data
    }

    @Test
    public void testExpiry() throws Exception {
        // Submit one content server update
        ContentServer.main(new String[]{"localhost:" + port, "weather_input.txt"});

        // Wait longer than expiry time (e.g. 35 seconds)
        Thread.sleep(35000);

        // Run GETClient to verify expired data is gone
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        System.setOut(new PrintStream(outContent));
        GETClient.main(new String[]{"localhost:" + port});
        System.setOut(originalOut);

        String output = outContent.toString().trim();
        System.out.println("GETClient output after expiry wait:\n" + output);

        assertEquals(true, output.contains("Empty weather data"));
    }
}
