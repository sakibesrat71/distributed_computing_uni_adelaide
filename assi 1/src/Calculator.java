import java.rmi.Remote;
import java.rmi.RemoteException;

// Remote interface for a Calculator that gives each client its own stack.

public interface Calculator extends Remote {

    // Registers a new client session and gives an unique session ID.

    String registerClient() throws RemoteException;

    void pushValue(String sessionId, int val) throws RemoteException;

    void pushOperation(String sessionId, String operator) throws RemoteException;

    int pop(String sessionId) throws RemoteException;

    boolean isEmpty(String sessionId) throws RemoteException;

    int delayPop(String sessionId, int millis) throws RemoteException, InterruptedException;
}
