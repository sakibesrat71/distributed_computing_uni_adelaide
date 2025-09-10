import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class LamportClockTest {
    @Test
    public void testInitialClock() {
        LamportClock clock = new LamportClock();
        assertEquals(0, clock.get());
    }

    @Test
    public void testTickIncrement() {
        LamportClock clock = new LamportClock();
        clock.tick();
        assertEquals(1, clock.get());
        clock.tick();
        assertEquals(2, clock.get());
    }

    @Test
    public void testUpdateClock() {
        LamportClock clock = new LamportClock();
        clock.tick();
        clock.update(10);
        assertEquals(11, clock.get());

        clock.update(5);
        assertEquals(12, clock.get()); // Should not decrease
    }
}
