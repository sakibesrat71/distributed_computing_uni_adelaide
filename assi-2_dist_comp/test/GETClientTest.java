import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GETClientTest {

    @Test
    public void testParseAndPrintJson_ValidObject() {
        String json = "{\"id\":\"server1\",\"temp\":\"23\"}";
        // Redirect stdout or implement printable capture if needed
        // For now, test the parsing method returns expected key-values via helper

        // Adapt GETClient.parseAndPrintJson() to return a Map for test purposes (suggestion)
    }

    @Test
    public void testParseAndPrintJson_InvalidJson() {
        String json = "invalid json";
        // Should print "Invalid JSON format"
        // Can verify with stdout capture or method refactor to return status
    }
}
