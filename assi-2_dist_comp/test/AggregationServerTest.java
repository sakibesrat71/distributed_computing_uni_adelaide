import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AggregationServerTest {

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
}
