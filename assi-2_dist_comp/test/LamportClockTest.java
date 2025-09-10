import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class LamportClockTest {

    @Test
    public void testInitialValue() {
        LamportClock clock = new LamportClock();
        assertEquals(0, clock.get());
    }

    @Test
    public void testTickIncrements() {
        LamportClock clock = new LamportClock();
        clock.tick();
        assertEquals(1, clock.get());
        clock.tick();
        assertEquals(2, clock.get());
    }

    @Test
    public void testUpdateAdvancesClock() {
        LamportClock clock = new LamportClock();
        clock.tick();  // clock = 1
        clock.update(5); // should set clock to 6
        assertEquals(6, clock.get());
    }

    @Test
    public void testUpdateDoesNotDecreaseClock() {
        LamportClock clock = new LamportClock();
        clock.update(10);
        int before = clock.get();
        clock.update(5); // ignored because 5 < 10
        assertEquals(before+1, clock.get());
    }
}
