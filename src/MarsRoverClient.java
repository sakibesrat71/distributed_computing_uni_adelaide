
import java.rmi.Naming;
import java.util.List;

public class MarsRoverClient {
    public static void main(String[] args) {
        try {
            MissionControl missionControl = (MissionControl) Naming.lookup("rmi://localhost:5000/MissionControl");

            System.out.println("Mars Rover: Adding samples...");
            missionControl.addSample("MARS-ROCK-001");
            missionControl.addSample("MARS-SOIL-007");

            System.out.println("Mars Rover: Requesting sample list...");
            List<String> samples = missionControl.getSampleList();
            System.out.println("Current Samples on Houston: " + samples);

            System.out.println("Mars Rover: Performing onboard sample check...");
            String result = missionControl.onboardSampleCheck("MARS-ROCK-001");
            System.out.println("Houston replied: " + result);

            System.out.println("Mars Rover: Removing sample MARS-SOIL-007...");
            missionControl.removeSample("MARS-SOIL-007");

            System.out.println("Mars Rover: Requesting updated sample list...");
            samples = missionControl.getSampleList();
            System.out.println("Updated Samples on Houston: " + samples);

        } catch (Exception e) {
            System.err.println("MarsRoverClient exception: " + e);
        }
    }
}
