import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

public class AggregationServerTest {

    @BeforeAll
    public static void startServer() {
        Thread serverThread = new Thread(() -> {
            try {
                AggregationServer.main(new String[]{"4567"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait for server to start accepting connections
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
    }

    @BeforeEach
    public void setUp() {
        AggregationServer.getWeatherDataMap().clear();
        AggregationServer.getContentServerLastContact().clear();

    }

    @BeforeEach
    public void cleanMaps() {
        AggregationServer.getWeatherDataMap().clear();
        AggregationServer.getContentServerLastContact().clear();
    }



    @Test
    public void testAggregationServerRejectsInvalidJson() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String badJson = "{\"id\":\"server1\", \"temp\": 25"; // missing closing brace

            String request = "PUT /weather.json HTTP/1.1\r\n" +
                    "Content-Length: " + badJson.length() + "\r\n" +
                    "\r\n" +
                    badJson;

            out.write(request);
            out.flush();

            String responseLine = in.readLine();
            assertNotNull(responseLine);
            assertTrue(responseLine.contains("500"), "Expected 500 response, got: " + responseLine);
        }
    }

    @Test
    public void testServerReturns204OnEmptyPut() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String request = "PUT /weather.json HTTP/1.1\r\n" +
                    "Content-Length: 0\r\n" +
                    "\r\n";

            out.write(request);
            out.flush();

            String responseLine = in.readLine();
            assertNotNull(responseLine);
            assertTrue(responseLine.contains("204"), "Expected 204 response, got: " + responseLine);
        }
    }

    @Test
    public void testExtractIdFromJson_Valid() {
        String json = "{\"id\":\"server1\", \"temp\": 23}";
        String id = AggregationServer.extractIdFromJson(json);
        assertEquals("server1", id);
    }

    @Test
    public void testExtractIdFromJson_MissingId() {
        String json = "{\"temp\": 23}";
        String id = AggregationServer.extractIdFromJson(json);
        assertNull(id);
    }

    @Test
    public void testCleanExpiredEntries_RemovesOld() {
        AggregationServer.getContentServerLastContact().put("server1", System.currentTimeMillis() - 35000);
        AggregationServer.getWeatherDataMap().put("server1", "{\"id\":\"server1\", \"temp\": 23}");

        AggregationServer.cleanExpiredEntries();

        assertFalse(AggregationServer.getWeatherDataMap().containsKey("server1"));
        assertFalse(AggregationServer.getContentServerLastContact().containsKey("server1"));
    }

    @Test
    public void testCleanExpiredEntries_KeepsRecent() {
        AggregationServer.getContentServerLastContact().put("server1", System.currentTimeMillis());
        AggregationServer.getWeatherDataMap().put("server1", "{\"id\":\"server1\", \"temp\": 23}");

        AggregationServer.cleanExpiredEntries();

        assertTrue(AggregationServer.getWeatherDataMap().containsKey("server1"));
        assertTrue(AggregationServer.getContentServerLastContact().containsKey("server1"));
    }



    @Test
    public void testExtractIdFromJson_Missing() {
        String json = "{\"temp\": 25}";
        assertNull(AggregationServer.extractIdFromJson(json));
    }

    @Test
    public void testCleanExpiredEntries_RemovesOldEntry() {
        String id = "server1";
        AggregationServer.getContentServerLastContact().put(id, System.currentTimeMillis() - 35000);
        AggregationServer.getWeatherDataMap().put(id, "{\"id\":\"server1\", \"temp\":25}");

        AggregationServer.cleanExpiredEntries();

        assertFalse(AggregationServer.getWeatherDataMap().containsKey(id));
        assertFalse(AggregationServer.getContentServerLastContact().containsKey(id));
    }

    @Test
    public void testCleanExpiredEntries_KeptRecentEntry() {
        String id = "server2";
        AggregationServer.getContentServerLastContact().put(id, System.currentTimeMillis());
        AggregationServer.getWeatherDataMap().put(id, "{\"id\":\"server2\", \"temp\": 22}");

        AggregationServer.cleanExpiredEntries();

        assertTrue(AggregationServer.getWeatherDataMap().containsKey(id));
        assertTrue(AggregationServer.getContentServerLastContact().containsKey(id));
    }

    @Test
    public void testPersistData_CreatesFile() throws Exception {
        String id = "server3";
        AggregationServer.getWeatherDataMap().put(id, "{\"id\":\"server3\", \"temp\": 20}");
        AggregationServer.getContentServerLastContact().put(id, System.currentTimeMillis());

        AggregationServer.persistData();

        java.io.File file = new java.io.File("weather.json");
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        // Cleanup
        file.delete();
    }

    @Test
    public void testAggregationServerRejectsMalformedJson() throws Exception {
        // connect directly using socket and send bad JSON in PUT
        Socket socket = new Socket("localhost", 4567);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String badJson = "{ \"id\": \"server1\", \"temp\": 25 ";  // missing closing brace

        String request = "PUT /weather.json HTTP/1.1\r\n" +
                "Content-Length: " + badJson.length() + "\r\n" +
                "\r\n" +
                badJson;
        out.write(request);
        out.flush();

        String response = in.readLine();
        socket.close();

        assertTrue(response.contains("500"));
    }

    @Test
    public void testAggregationServerReturns204OnEmptyPut() throws Exception {
        Socket socket = new Socket("localhost", 4567);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String request = "PUT /weather.json HTTP/1.1\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n";
        out.write(request);
        out.flush();

        String response = in.readLine();
        socket.close();

        assertTrue(response.contains("204"));
    }


}
