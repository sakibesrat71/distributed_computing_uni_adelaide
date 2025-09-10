import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AggregationServerTest {

    @BeforeEach
    public void setUp() {
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
}
