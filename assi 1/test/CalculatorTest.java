import org.junit.jupiter.api.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalculatorTest {

    private static Calculator calc;
    private static String sessionA;
    private static String sessionB;

    @BeforeAll
    public static void setup() throws Exception {
        // Connect to RMI registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        calc = (Calculator) registry.lookup("CalculatorService");

        // Register two separate clients
        sessionA = calc.registerClient();
        sessionB = calc.registerClient();
        System.out.println("Session A: " + sessionA);
        System.out.println("Session B: " + sessionB);
    }

    @Test
    @Order(1)
    // testing pushValue and pop
    public void testPushAndPopPerClient() throws Exception {
        calc.pushValue(sessionA, 42);
        assertFalse(calc.isEmpty(sessionA));
        assertTrue(calc.isEmpty(sessionB)); // B should still be empty

        int val = calc.pop(sessionA);
        assertEquals(42, val);
        assertTrue(calc.isEmpty(sessionA));
        assertTrue(calc.isEmpty(sessionB));
    }

    @Test
    @Order(2)
    public void testMinOperationIsolation() throws Exception {
        calc.pushValue(sessionA, 5);
        calc.pushValue(sessionA, 2);
        calc.pushValue(sessionA, 9);
        calc.pushOperation(sessionA, "min");
        assertEquals(2, calc.pop(sessionA));

        assertTrue(calc.isEmpty(sessionB)); // Still untouched
    }

    @Test
    @Order(3)
    public void testMaxOperationIsolation() throws Exception {
        calc.pushValue(sessionB, 1);
        calc.pushValue(sessionB, 50);
        calc.pushValue(sessionB, 10);
        calc.pushOperation(sessionB, "max");
        assertEquals(50, calc.pop(sessionB));

        assertTrue(calc.isEmpty(sessionA)); // A is unaffected
    }

    @Test
    @Order(4)
    public void testLcmAndGcdPerClient() throws Exception {
        // Client A tests LCM
        calc.pushValue(sessionA, 4);
        calc.pushValue(sessionA, 6);
        calc.pushValue(sessionA, 8);
        calc.pushOperation(sessionA, "lcm");
        assertEquals(24, calc.pop(sessionA));

        // Client B tests GCD
        calc.pushValue(sessionB, 54);
        calc.pushValue(sessionB, 24);
        calc.pushOperation(sessionB, "gcd");
        assertEquals(6, calc.pop(sessionB));
    }

    @Test
    @Order(5)
    public void testDelayPopPerClient() throws Exception {
        calc.pushValue(sessionA, 99);
        long start = System.currentTimeMillis();
        int result = calc.delayPop(sessionA, 1000);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(99, result);
        assertTrue(elapsed >= 1000);
        assertTrue(calc.isEmpty(sessionA));
    }

    @Test
    @Order(6)
    public void testMultipleClientsConcurrently() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Runnable clientATask = () -> {
            try {
                for (int i = 0; i < 3; i++) {
                    calc.pushValue(sessionA, i + 1);
                }
            } catch (Exception e) {
                fail("Client A thread failed: " + e.getMessage());
            }
        };

        Runnable clientBTask = () -> {
            try {
                for (int i = 10; i < 13; i++) {
                    calc.pushValue(sessionB, i);
                }
            } catch (Exception e) {
                fail("Client B thread failed: " + e.getMessage());
            }
        };

        executor.submit(clientATask);
        executor.submit(clientBTask);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Validate Client A’s stack
        int countA = 0;
        while (!calc.isEmpty(sessionA)) {
            calc.pop(sessionA);
            countA++;
        }
        assertEquals(3, countA);

        // Validate Client B’s stack
        int countB = 0;
        while (!calc.isEmpty(sessionB)) {
            calc.pop(sessionB);
            countB++;
        }
        assertEquals(3, countB);
    }
}
