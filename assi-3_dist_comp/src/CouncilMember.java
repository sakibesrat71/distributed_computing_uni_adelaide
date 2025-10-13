import java.net.*;
import java.io.*;

public class CouncilMember {
    private String memberId;
    private String profile;
    private NetworkConfig config;
    private int myPort;

    public CouncilMember(String memberId, String profile, NetworkConfig config) {
        this.memberId = memberId;
        this.profile = profile;
        this.config = config;
    }

    public void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(myPort);
        System.out.println(memberId + " listening on port " + myPort);
        while (true) {
            Socket client = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line = in.readLine();
            if (line != null) {
                Message msg = Message.parse(line);
                System.out.println(memberId + " received: " + msg.serialize());
                // TODO: handle Paxos logic
            }
            client.close();
        }
    }



    public void sendMessage(String targetMemberId, Message msg) throws Exception {
        InetSocketAddress addr = config.getAddress(targetMemberId);
        Socket socket = new Socket(addr.getHostName(), addr.getPort());
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write(msg.serialize());
        out.newLine();
        out.flush();
        socket.close();
        System.out.println(memberId + " sent to " + targetMemberId + ": " + msg.serialize());
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java CouncilMember <MemberId> --profile <profile>");
            System.exit(1);
        }
        String memberId = args[0];
        String profile = args[2];
        NetworkConfig config = new NetworkConfig("network.config");
        CouncilMember cm = new CouncilMember(memberId, profile, config);

        // Start server in a new thread
        new Thread(() -> {
            try {
                cm.startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // If this is M2, send message to M1
        if (memberId.equals("M2")) {
            Thread.sleep(1000); // give server time to start
            Message testMsg = new Message("PREPARE", "M2", "1.1", "CandidateX");
            cm.sendMessage("M1", testMsg);
        }
    }

}
