import java.rmi.Remote;
import java.rmi.RemoteException;

// Calculator is the Remote interface that defines thee basic operations

public interface Calculator extends Remote {

    // Pushes an integer value onto the stack.

    void pushValue(int val) throws RemoteException;


     // Pushes an operation into the stack and calculates it.
     // Operations: min,max, lcm, gcd
    void pushOperation(String operator) throws RemoteException;


     // Pops and returns the top value from the stack.

    int pop() throws RemoteException;


     // Returns true if the stack is empty.

    boolean isEmpty() throws RemoteException;

    // Waits the given number of milliseconds before popping and returning the top of the stack.

    int delayPop(int millis) throws RemoteException, InterruptedException;
}
