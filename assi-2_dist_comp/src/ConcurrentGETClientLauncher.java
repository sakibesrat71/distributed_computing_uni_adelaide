public class ConcurrentGETClientLauncher {
    public static void main(String[] args) {
        String serverInfo = "localhost:4567";  // AggregationServer address
        int numberOfClients = 5;                // Number of concurrent GET clients

        for (int i = 1; i <= numberOfClients; i++) {
            Thread t = new Thread(() -> {
                try {
                    GETClient.main(new String[] {serverInfo});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();


            try { Thread.sleep(50); } catch (InterruptedException e) { }
        }
    }
}
