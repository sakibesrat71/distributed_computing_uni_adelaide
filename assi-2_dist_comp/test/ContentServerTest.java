import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

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
}
