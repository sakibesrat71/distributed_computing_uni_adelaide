import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorClient {
        public static void main(String[] args) {
                try {
                        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                        Calculator calc = (Calculator) registry.lookup("CalculatorService");

                        // Sakib registers
                        String sakibSession = calc.registerClient();
                        System.out.println("Sakib's ID: " + sakibSession);

                        // Work on my own stack
                        calc.pushValue(sakibSession, 10);
                        calc.pushValue(sakibSession, 20);
                        calc.pushOperation(sakibSession, "max");
                        System.out.println("Result from Sakib's: " + calc.pop(sakibSession));

                        // Esrat Registers
                        String esratSession = calc.registerClient();
                        System.out.println("Esrat's ID: " + esratSession);
                        
                        calc.pushValue(esratSession, 5);
                        calc.pushValue(esratSession, 15);
                        calc.pushOperation(esratSession, "min");
                        System.out.println("Result from another client's stack: " + calc.pop(esratSession));

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
