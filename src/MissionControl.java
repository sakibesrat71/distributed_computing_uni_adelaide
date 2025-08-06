
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MissionControl extends Remote {
    String onboardSampleCheck(String sampleId) throws RemoteException;
    void addSample(String sampleId) throws RemoteException;
    void removeSample(String sampleId) throws RemoteException;
    List<String> getSampleList() throws RemoteException;
}
