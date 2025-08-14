import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// CalculatorServer opens a RMI registry -> exports the calc,
// and binds it to the registry with the name "CalculatorService".

public class CalculatorServer {
    public static void main(String[] args) throws RemoteException {

            CalculatorImplementation calc = new CalculatorImplementation();
            Registry registry = LocateRegistry.createRegistry(1099); // Default RMI port
            registry.rebind("CalculatorService", calc);
            System.out.println("Calculator Server ready.");

    }
}
