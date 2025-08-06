
import java.rmi.Naming;

public class HoustonServer {
    public static void main(String[] args) {
        try {
            MissionControlImpl_RemoteFunc missionControlImpl = new MissionControlImpl_RemoteFunc();
            Naming.rebind("rmi://localhost:5000/MissionControl", missionControlImpl);
            System.out.println("HoustonServer is online and delegated tasks to MissionControlImpl_RemoteFunc.");
        } catch (Exception e) {
            System.err.println("HoustonServer exception: " + e);
        }
    }
}
