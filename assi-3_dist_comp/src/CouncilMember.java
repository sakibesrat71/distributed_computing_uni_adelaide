import java.net.*;
import java.io.*;

public class CouncilMember {
    private String memberId;
    private String profile;
    private NetworkConfig config;
    private int myPort;

    public CouncilMember(String memberId, String profile, String configPath) throws Exception {
        this.memberId = memberId;
        this.profile = profile;
        this.config = new NetworkConfig(configPath);
        this.myPort = config.getAddress(memberId).getPort();
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
        if (args.length < 2) {
            System.err.println("Usage: java CouncilMember <MemberId> --profile <profile>");
            System.exit(1);
        }
        String memberId = args[0];
        String profile = args[2];
        CouncilMember member = new CouncilMember(memberId, profile, "network.config");
        member.startServer();
    }
}
