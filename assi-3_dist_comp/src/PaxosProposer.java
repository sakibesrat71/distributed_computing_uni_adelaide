import java.util.*;
import java.util.concurrent.*;

public class PaxosProposer {
    private final String proposerId;
    private final Set<String> memberIds;
    private final int quorum;
    private final CouncilMember councilMember;

    // For current proposal
    private double currentProposalNum;
    private String currentProposalVal;
    private Set<String> promises = ConcurrentHashMap.newKeySet();
    private Map<String, String> priorAcceptedValues = new ConcurrentHashMap<>();
    private boolean consensusReached = false;

    public PaxosProposer(String proposerId, Set<String> memberIds, CouncilMember councilMember) {
        this.proposerId = proposerId;
        this.memberIds = memberIds;
        this.quorum = memberIds.size() / 2 + 1;
        this.councilMember = councilMember;
    }

    // Start the prepare phase for a candidate
    public void initiateProposal(String candidate, double proposalNum) {
        this.currentProposalNum = proposalNum;
        this.currentProposalVal = candidate;
        this.promises.clear();
        this.priorAcceptedValues.clear();
        this.consensusReached = false;

        for (String peer : memberIds) {
            Message prepare = new Message("PREPARE", proposerId, String.valueOf(proposalNum), candidate);
            councilMember.sendMessage(peer, prepare);
        }
        System.out.println(proposerId + " [Proposer] sent PREPARE to all for: " + candidate);
    }

    // Called by CouncilMember when a PROMISE is received
    public synchronized void handlePromise(Message msg) {
        if (consensusReached || Double.parseDouble(msg.proposalNum) != currentProposalNum)
            return; // Not for current proposal or already decided

        promises.add(msg.senderId);
        if (!"null".equals(msg.proposalVal))
            priorAcceptedValues.put(msg.senderId, msg.proposalVal);

        System.out.println(proposerId + " received PROMISE from " + msg.senderId +
                " for proposal " + msg.proposalNum + " val " + msg.proposalVal);

        // Check for quorum
        if (promises.size() >= quorum) {
            // Find highest-numbered accepted value (if any) from received PROMISEs
            String useVal = currentProposalVal;
            if (!priorAcceptedValues.isEmpty()) {
                useVal = Collections.max(priorAcceptedValues.values());
            }
            // Send ACCEPT_REQUEST to all
            for (String peer : memberIds) {
                Message acceptReq = new Message("ACCEPT_REQUEST", proposerId, String.valueOf(currentProposalNum), useVal);
                councilMember.sendMessage(peer, acceptReq);
            }
            System.out.println(proposerId + " [Proposer] sent ACCEPT_REQUEST to all for: " + useVal);
        }
    }

    // Called by CouncilMember when ACCEPTED is received
    private Set<String> acceptances = ConcurrentHashMap.newKeySet();

    public synchronized void handleAccepted(Message msg) {
        if (consensusReached || Double.parseDouble(msg.proposalNum) != currentProposalNum)
            return;
        acceptances.add(msg.senderId);
        System.out.println(proposerId + " received ACCEPTED from " + msg.senderId);

        // Check for majority
        if (acceptances.size() >= quorum) {
            System.out.println(proposerId + " [Proposer] consensus achieved for " + msg.proposalVal);
            consensusReached = true;
            // Broadcast LEARN to all: final decision
            for (String peer : memberIds) {
                Message learn = new Message("LEARN", proposerId, String.valueOf(currentProposalNum), msg.proposalVal);
                councilMember.sendMessage(peer, learn);
            }
        }
    }
}
