import org.junit.jupiter.api.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalculatorTest {

    private static Calculator calc;

    @BeforeAll
    public static void setup() throws Exception {
        // Connecting RMI and looking up service
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        calc = (Calculator) registry.lookup("CalculatorService");

        // Clear the stack to avoid weird behaviour
        while (!calc.isEmpty()) {
            calc.pop();
        }
    }

    @Test
    @Order(1)
    public void testPushAndPopSingleClient() throws Exception {
        calc.pushValue(42);
        assertFalse(calc.isEmpty(), "Stack should not be empty after push");
        int popped = calc.pop();
        assertEquals(42, popped, "Popped value should match pushed value");
        assertTrue(calc.isEmpty(), "Stack should be empty");
    }

    @Test
    @Order(2)
    public void testMinOperation() throws Exception {
        calc.pushValue(5);
        calc.pushValue(2);
        calc.pushValue(9);
        calc.pushOperation("min");
        assertEquals(2, calc.pop(), "Min of (5,2,9) should be 2");
    }

    @Test
    @Order(3)
    public void testMaxOperation() throws Exception {
        calc.pushValue(1);
        calc.pushValue(50);
        calc.pushValue(10);
        calc.pushOperation("max");
        assertEquals(50, calc.pop(), "Max of (1,50,10) should be 50");
    }

    @Test
    @Order(4)
    public void testLcmOperation() throws Exception {
        calc.pushValue(4);
        calc.pushValue(6);
        calc.pushValue(8);
        calc.pushOperation("lcm");
        assertEquals(24, calc.pop(), "LCM of (4,6,8) should be 24");
    }

    @Test
    @Order(5)
    public void testGcdOperation() throws Exception {
        calc.pushValue(54);
        calc.pushValue(24);
        calc.pushOperation("gcd");
        assertEquals(6, calc.pop(), "GCD of (54,24) should be 6");
    }

    @Test
    @Order(6)
    public void testDelayPop() throws Exception {
        calc.pushValue(99);
        long start = System.currentTimeMillis();
        int value = calc.delayPop(1000);
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(99, value, "Value should match pushed value");
        assertTrue(elapsed >= 1000, "delayPop should wait at least 1 second");
    }

    @Test
    @Order(7)
    public void testMultipleClients() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Runnable clientTask = () -> {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                Calculator localCalc = (Calculator) registry.lookup("CalculatorService");
                for (int i = 0; i < 3; i++) {
                    localCalc.pushValue(i + 1);
                }
            } catch (Exception e) {
                fail("Client thread failed: " + e.getMessage());
            }
        };

        // Spawn 3 clients
        for (int i = 0; i < 3; i++) {
            executor.submit(clientTask);
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // There should be exactly 9 values in total
        int count = 0;
        while (!calc.isEmpty()) {
            calc.pop();
            count++;
        }

        assertEquals(9, count, "Expected stack to have 9 items pushed by 3 clients");
    }
}
