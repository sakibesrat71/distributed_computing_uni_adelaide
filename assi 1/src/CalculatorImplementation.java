import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

// Implementation of the interface Calculator.

public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {

    private final Map<String, Stack<Integer>> stacks;

    protected CalculatorImplementation() throws RemoteException {
        stacks = new HashMap<>();
    }

    @Override
    public synchronized String registerClient() throws RemoteException {
        Random random = new Random();
        String sessionId;
        // Keep generating until we get a unique number
        do {
            int randomNum = 10000 + random.nextInt(90000); // generates 5-digit number
            sessionId = String.valueOf(randomNum);
        } while (stacks.containsKey(sessionId));

        stacks.put(sessionId, new Stack<>());
        System.out.println("Registered client: " + sessionId);

        return sessionId;
    }


    @Override
    public synchronized void pushValue(String sessionId, int val) throws RemoteException {
        getStack(sessionId).push(val);
        System.out.printf("[%s] Pushed value: %d%n", sessionId, val);
    }

    @Override
    public synchronized void pushOperation(String sessionId, String operator) throws RemoteException {
        Stack<Integer> stack = getStack(sessionId);
        if (stack.isEmpty()) return;

        int result;
        switch (operator.toLowerCase()) {
            case "min": result = stack.stream().min(Integer::compare).orElse(0); break;
            case "max": result = stack.stream().max(Integer::compare).orElse(0); break;
            case "lcm": result = stack.stream().reduce(1, this::lcm); break;
            case "gcd": result = stack.stream().reduce(0, this::gcd); break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        stack.clear();
        stack.push(result);
        System.out.printf("[%s] Performed %s -> %d%n", sessionId, operator, result);
    }

    @Override
    public synchronized int pop(String sessionId) throws RemoteException {
        Stack<Integer> stack = getStack(sessionId);
        if (stack.isEmpty()) throw new RemoteException("Stack is empty!");
        int val = stack.pop();
        System.out.printf("[%s] Popped: %d%n", sessionId, val);
        return val;
    }

    @Override
    public synchronized boolean isEmpty(String sessionId) throws RemoteException {
        return getStack(sessionId).isEmpty();
    }

    @Override
    public synchronized int delayPop(String sessionId, int millis) throws RemoteException, InterruptedException {
        Thread.sleep(millis);
        return pop(sessionId);
    }

    // Helper to retrieve the correct stack
    private Stack<Integer> getStack(String sessionId) throws RemoteException {
        Stack<Integer> stack = stacks.get(sessionId);
        if (stack == null) throw new RemoteException("Invalid session ID: " + sessionId);
        return stack;
    }

    // GCD
    private int gcd(int a, int b) {
        return (b == 0) ? a : gcd(b, a % b);
    }

    // LCM
    private int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }
}
