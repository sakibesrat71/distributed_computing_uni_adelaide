import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ContentServerTest {
    @Test
    public void testConvertToJson_ValidInput() {
        String input = "id:abc\nkey1:123\nkey2:true\nkey3:some value\n";
        String expected = "{\n  \"id\": \"abc\",\n  \"key1\": 123,\n  \"key2\": true,\n  \"key3\": \"some value\"\n}";

        String json = ContentServer.convertToJson(input);
        assertEquals(expected, json);
    }

    @Test
    public void testConvertToJson_InvalidLines() {
        String input = "id:abc\ninvalidline\nkey:val\n";
        String expected = "{\n  \"id\": \"abc\",\n  \"key\": \"val\"\n}";

        String json = ContentServer.convertToJson(input);
        assertEquals(expected, json);
    }

    @Test
    public void testConvertToJson_ValidSimple() {
        String input = "id:abc\nkey1:123\nkey2:true\nkey3:value\n";
        String expected = "{\n  \"id\": \"abc\",\n  \"key1\": 123,\n  \"key2\": true,\n  \"key3\": \"value\"\n}";
        assertEquals(expected, ContentServer.convertToJson(input));
    }

    @Test
    public void testConvertToJson_EmptyLinesSkipped() {
        String input = "id:abc\n\nkey:val\n";
        String expected = "{\n  \"id\": \"abc\",\n  \"key\": \"val\"\n}";
        assertEquals(expected, ContentServer.convertToJson(input));
    }

    @Test
    public void testConvertToJson_NumericParsing() {
        String input = "id:abc\nnum1:10\nnum2:10.5\nflag:true\n";
        String expected = "{\n  \"id\": \"abc\",\n  \"num1\": 10,\n  \"num2\": 10.5,\n  \"flag\": true\n}";
        assertEquals(expected, ContentServer.convertToJson(input));
    }

    @Test
    public void testConvertToJson_InvalidLinesIgnored() {
        String input = "id:abc\ninvalidline\nkey:val\n";
        String expected = "{\n  \"id\": \"abc\",\n  \"key\": \"val\"\n}";
        assertEquals(expected, ContentServer.convertToJson(input));
    }

    @Test
    public void testContentServerRetryOnServerUnreachable() {
        String fakeServer = "localhost:9999"; // assume no server here
        String dummyInput = "id:test\nvalue:123";

        // Override convertToJson to return a known JSON for test stability
        String json = "{ \"id\": \"test\", \"value\": 123 }";

        for (int attempt = 1; attempt <= ContentServer.getMaxRetry(); attempt++) {
            try {
                ContentServer.sendPutRequest(fakeServer, json);
                fail("Expected IOException due to unreachable server");
            } catch (IOException e) {
                // expected exception
                assertTrue(e instanceof IOException);
            }
        }
    }
    @Test
    public void testConcurrentGetPutClients() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        String serverInfo = "localhost:4567";

        // Launch 5 ContentServers concurrently
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            executor.submit(() -> {
                String file = "weather_input" + (idx + 1) + ".txt";
                ContentServer.main(new String[]{serverInfo, file});
            });
        }

        // Launch 5 GETClients concurrently
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                GETClient.main(new String[]{serverInfo});
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
    }


}
