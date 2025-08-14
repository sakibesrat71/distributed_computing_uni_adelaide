import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Stack;


 // Implementation of the Calculator interface.
public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {

    private Stack<Integer> stack;

    protected CalculatorImplementation() throws RemoteException {
        stack = new Stack<>();
    }

    // Synchronized is used to handle multiple clients
    @Override
    public synchronized void pushValue(int val) throws RemoteException {
        stack.push(val);
        System.out.println("Pushed value: " + val);
    }

    @Override
    public synchronized void pushOperation(String operator) throws RemoteException {
        if (stack.isEmpty()) {
            System.out.println("Stack is empty. Operation aborted.");
            return;
        }
        int result;
        switch (operator.toLowerCase()) {
            case "min":
                result = stack.stream().min(Integer::compare).orElse(0);
                break;
            case "max":
                result = stack.stream().max(Integer::compare).orElse(0);
                break;
            case "lcm":
                result = stack.stream().reduce(1, this::lcm);
                break;
            case "gcd":
                result = stack.stream().reduce(0, this::gcd);
                break;

            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        stack.clear();

        stack.push(result);
        System.out.println("Performed operation: " + operator + ", Result: " + result);
    }

    @Override
    public synchronized int pop() throws RemoteException {
        if (stack.isEmpty()) {
            throw new RemoteException("Stack is empty!");}
        int val = stack.pop();
        System.out.println("Popped value: " + val);
        return val;

    }

    @Override
    public synchronized boolean isEmpty() throws RemoteException {

        return stack.isEmpty();

    }

    @Override
    public synchronized int delayPop(int millis) throws RemoteException, InterruptedException {
        Thread.sleep(millis);

        return pop();
    }
    // Utility methods
    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
    private int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }
}
