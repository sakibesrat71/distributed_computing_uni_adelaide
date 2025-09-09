public class LamportClock {
    private int clock = 0;
    public synchronized void tick() {
        clock++;
    }
    public synchronized void update(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }
    public synchronized int get() {
        return clock;
    }
}
