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
}
