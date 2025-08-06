
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MissionControlImpl_RemoteFuncTest {

    private MissionControlImpl_RemoteFunc missionControl;

    @BeforeEach
    void setUp() throws RemoteException {
        missionControl = new MissionControlImpl_RemoteFunc();
    }

    @Test
    void testAddSample() throws RemoteException {
        missionControl.addSample("MARS-ROCK-001");
        List<String> samples = missionControl.getSampleList();
        assertTrue(samples.contains("MARS-ROCK-001"), "Sample should be added to the list.");
    }

    @Test
    void testRemoveSample() throws RemoteException {
        missionControl.addSample("MARS-SOIL-007");
        missionControl.removeSample("MARS-SOIL-007");
        List<String> samples = missionControl.getSampleList();
        assertFalse(samples.contains("MARS-SOIL-007"), "Sample should be removed from the list.");
    }

    @Test
    void testRemoveNonExistentSample() throws RemoteException {
        missionControl.addSample("MARS-ROCK-005");
        missionControl.removeSample("MARS-DUST-999");
        List<String> samples = missionControl.getSampleList();
        assertTrue(samples.contains("MARS-ROCK-005"), "Non-existent sample removal should not affect other samples.");
    }

    @Test
    void testOnboardSampleCheck() throws RemoteException {
        String result = missionControl.onboardSampleCheck("MARS-ICE-003");
        assertEquals("Sample MARS-ICE-003 analysis: Composition Stable, Organic traces found.", result, "Sample check response should match expected output.");
    }

    @Test
    void testSampleListPersistence() throws RemoteException {
        missionControl.addSample("MARS-ROCK-004");
        missionControl.addSample("MARS-SOIL-009");
        missionControl.removeSample("MARS-ROCK-004");
        List<String> samples = missionControl.getSampleList();
        assertEquals(1, samples.size(), "Only one sample should remain after removal.");
        assertTrue(samples.contains("MARS-SOIL-009"), "Remaining sample should be MARS-SOIL-009.");
    }
}
