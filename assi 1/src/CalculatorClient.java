import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * CalculatorClient connects to the Calculator RMI service and tests its operations.
 */
public class CalculatorClient {
    public static void main(String[] args) throws RemoteException, NotBoundException, InterruptedException {

            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Calculator calc = (Calculator) registry.lookup("CalculatorService");

            // Test push and pop
            calc.pushValue(42);
            calc.pushValue(38);
            calc.pushOperation("max");
            System.out.println("Pop after max: " + calc.pop());

            // Test lcm
            calc.pushValue(44);
            calc.pushValue(66);
            calc.pushValue(89);
            calc.pushOperation("lcm");
            System.out.println("LCM result: " + calc.pop());

            // Test delayPop
            calc.pushValue(99);
            System.out.println("Delayed pop (2s): " + calc.delayPop(2000));


    }
}
