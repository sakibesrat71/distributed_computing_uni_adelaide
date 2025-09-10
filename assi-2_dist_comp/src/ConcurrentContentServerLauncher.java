public class ConcurrentContentServerLauncher {
    public static void main(String[] args) {
        String serverInfo = "localhost:4567"; // AggregationServer address
        // Hardcoded weather data file names; add or adjust as needed
        String[] weatherFiles = {
                "weather_input.txt"
        };

        for (String weatherFile : weatherFiles) {
            Thread t = new Thread(() -> {
                try {
                    ContentServer.main(new String[]{serverInfo, weatherFile});
                } catch (Exception e) {
                    System.out.println("Error in thread for " + weatherFile);
                    e.printStackTrace();
                }
            });
            t.start();


            try { Thread.sleep(100); } catch (InterruptedException e) { }
        }
    }
}
