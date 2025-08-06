
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class MissionControlImpl_RemoteFunc extends UnicastRemoteObject implements MissionControl {

    private final List<String> sampleList;

    protected MissionControlImpl_RemoteFunc() throws RemoteException {
        super();
        sampleList = new ArrayList<>();
    }

    @Override
    public String onboardSampleCheck(String sampleId) throws RemoteException {
        System.out.println("MissionControlImpl_RemoteFunc: Performing onboard sample check for " + sampleId);
        return "Sample " + sampleId + " analysis: Composition Stable, Organic traces found.";
    }

    @Override
    public void addSample(String sampleId) throws RemoteException {
        sampleList.add(sampleId);
        System.out.println("MissionControlImpl_RemoteFunc: Sample " + sampleId + " added.");
    }

    @Override
    public void removeSample(String sampleId) throws RemoteException {
        if(sampleList.remove(sampleId)) {
            System.out.println("MissionControlImpl_RemoteFunc: Sample " + sampleId + " removed.");
        } else {
            System.out.println("MissionControlImpl_RemoteFunc: Sample " + sampleId + " not found.");
        }
    }

    @Override
    public List<String> getSampleList() throws RemoteException {
        return new ArrayList<>(sampleList); // Return a copy
    }
}
