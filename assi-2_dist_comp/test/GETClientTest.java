import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GETClientTest {

    @Test
    public void testParseAndPrintJson_ValidObject() {
        String json = "{\"id\":\"server1\",\"temp\":\"23\"}";


    }



    @Test
    public void testParseAndPrintJson_ValidJsonObject() {
        String json = "{\"id\":\"server1\",\"temp\":\"23\"}";

        try {
            GETClient.parseAndPrintJson(json);
        } catch (Exception e) {
            fail("parseAndPrintJson threw exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseAndPrintJson_InvalidJson() {
        String json = "invalid_json";
        try {
            GETClient.parseAndPrintJson(json);
        } catch (Exception e) {
            fail("parseAndPrintJson threw exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseAndPrintJsonArray_ValidArray() {
        String jsonArray = "[{\"id\":\"1\",\"temp\":\"20\"},{\"id\":\"2\",\"temp\":\"22\"}]";
        try {
            GETClient.parseAndPrintJsonArray(jsonArray);
        } catch (Exception e) {
            fail("parseAndPrintJsonArray threw exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseAndPrintJsonArray_EmptyArray() {
        String jsonArray = "[]";
        try {
            GETClient.parseAndPrintJsonArray(jsonArray);
        } catch (Exception e) {
            fail("parseAndPrintJsonArray threw exception: " + e.getMessage());
        }
    }
}
