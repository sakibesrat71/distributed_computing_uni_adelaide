import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class CouncilMember {
    private final String memberId;
    private final String profile;
    private final NetworkConfig networkConfig;
    private final int myPort;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private PaxosProposer proposer;

    // Paxos state variables
    private double promisedProposalNum = -1;
    private double acceptedProposalNum = -1;
    private String acceptedProposalVal = null;
    private String decidedValue = null;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Random rand = new Random();

    public CouncilMember(String memberId, String profile, NetworkConfig config) throws IOException {
        this.memberId = memberId;
        this.profile = profile;
        this.networkConfig = config;
        this.myPort = config.getAddress(memberId).getPort();
        this.serverSocket = new ServerSocket(myPort);
        proposer = new PaxosProposer(memberId, networkConfig.getMemberIds(), this);
    }

    // Profile simulation: delay based on profile
    private void simulateLatency() {
        try {
            switch (profile) {
                case "reliable":
                    // Near-zero delay
                    Thread.sleep(rand.nextInt(50));
                    break;
                case "latent":
                    Thread.sleep(1000 + rand.nextInt(4000)); // 1-5 seconds
                    break;
                case "failure":
                    // Sometimes no delay, sometimes long delay or no response
                    if (rand.nextDouble() < 0.3) {
                        Thread.sleep(10000); // hang/fail
                    } else {
                        Thread.sleep(rand.nextInt(500));
                    }
                    break;
                case "standard":
                default:
                    Thread.sleep(200 + rand.nextInt(800)); // 200-1000ms
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Send a message with profile simulation
    public void sendMessage(String targetId, Message msg) {
        executor.submit(() -> {
            simulateLatency();
            // For failure profile: randomly drop messages
            if ("failure".equals(profile) && rand.nextDouble() < 0.3) {
                System.out.println(memberId + " (failure) dropping message " + msg.serialize());
                return;
            }
            try {
                InetSocketAddress addr = networkConfig.getAddress(targetId);
                try (Socket socket = new Socket(addr.getHostName(), addr.getPort());
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.println(msg.serialize());
                    System.out.println(memberId + " sent to " + targetId + ": " + msg.serialize());
                }
            } catch (IOException e) {
                System.err.println(memberId + " failed to send to " + targetId + ": " + e.getMessage());
            }
        });
    }

    // Start the server to listen for incoming messages
    public void startServer() {
        System.out.println(memberId + " listening on port " + myPort);
        while (running) {
            try {
                Socket client = serverSocket.accept();
                executor.submit(() -> handleClient(client));
            } catch (IOException e) {
                if (running) {
                    System.err.println(memberId + " server accept error: " + e.getMessage());
                }
            }
        }
    }

    // Handle incoming connection
    private void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String line = in.readLine();
            if (line != null) {
                simulateLatency();
                // For failure profile, randomly ignore messages (simulate crash)
                if ("failure".equals(profile) && rand.nextDouble() < 0.3) {
                    System.out.println(memberId + " (failure) ignoring received message");
                    return;
                }
                Message msg = Message.parse(line);
                System.out.println(memberId + " received: " + msg.serialize());
                handleMessage(msg);
            }
        } catch (IOException e) {
            System.err.println(memberId + " error handling client: " + e.getMessage());
        }
    }

    // Handle messages based on type and role
    private synchronized void handleMessage(Message msg) {
        switch (msg.type) {
            case "PREPARE":
                handlePrepare(msg);
                break;
            case "PROMISE":
                proposer.handlePromise(msg);
                break;
            case "ACCEPT_REQUEST":
                handleAcceptRequest(msg);
                break;
            case "ACCEPTED":
                proposer.handleAccepted(msg);
                break;
            case "LEARN":
                handleLearn(msg);
                break;
            default:
                System.err.println(memberId + " unknown message type: " + msg.type);
        }
    }

    private void handlePrepare(Message msg) {
        double proposalNum = Double.parseDouble(msg.proposalNum);
        if (proposalNum > promisedProposalNum) {
            promisedProposalNum = proposalNum;
            // Send PROMISE with accepted proposals if any
            Message promise = new Message("PROMISE", memberId, String.valueOf(promisedProposalNum),
                    acceptedProposalVal == null ? "null" : acceptedProposalVal);
            sendMessage(msg.senderId, promise);
            System.out.println(memberId + " promised proposal " + proposalNum + " to " + msg.senderId);
        }
    }

    private void handlePromise(Message msg) {
        // Proposer role: collect promises - stub
        System.out.println(memberId + " received PROMISE from " + msg.senderId);
        // For now no state machine; to be implemented later
    }

    private void handleAcceptRequest(Message msg) {
        double proposalNum = Double.parseDouble(msg.proposalNum);
        if (proposalNum >= promisedProposalNum) {
            promisedProposalNum = proposalNum;
            acceptedProposalNum = proposalNum;
            acceptedProposalVal = msg.proposalVal;
            // Accept the proposal, send ACCEPTED
            Message accepted = new Message("ACCEPTED", memberId, String.valueOf(acceptedProposalNum), acceptedProposalVal);
            sendMessage(msg.senderId, accepted);
            System.out.println(memberId + " accepted proposal " + proposalNum + " value " + acceptedProposalVal);
        } else {
            System.out.println(memberId + " ignored ACCEPT_REQUEST with lower proposal number " + proposalNum);
        }
    }

    private void handleAccepted(Message msg) {
        // Learner role: collect accepted. Stub for now
        System.out.println(memberId + " received ACCEPTED from " + msg.senderId + ": " + msg.serialize());
    }

    private void handleLearn(Message msg) {
        // Final consensus learnt by learner
        decidedValue = msg.proposalVal;
        System.out.println("CONSENSUS: " + decidedValue + " has been elected Council President!");
        running = false; // Optionally stop server after consensus
    }

    // Simple proposal initiation for testing - sends PREPARE to all
    public void initiateProposal(String candidate, double proposalNum) {
        System.out.println(memberId + " initiating proposal for " + candidate + " with number " + proposalNum);
        for (String peer : networkConfig.getMemberIds()) {
            Message prepare = new Message("PREPARE", memberId, String.valueOf(proposalNum), candidate);
            sendMessage(peer, prepare);
        }
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

        // Start server in a separate thread
        new Thread(cm::startServer).start();

        // TEST: If member is M1, initiate proposal after delay
        if ("M1".equals(memberId)) {
            Thread.sleep(2000);
            double proposalNum = 1.1; // example unique number
            cm.initiateProposal("M5", proposalNum);
        }
    }
}

