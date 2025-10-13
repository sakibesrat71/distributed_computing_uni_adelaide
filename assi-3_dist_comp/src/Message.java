// Supporting classes:

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Message {
    public String type;
    public String senderId;
    public String proposalNum;
    public String proposalVal;

    public Message(String type, String senderId, String proposalNum, String proposalVal) {
        this.type = type;
        this.senderId = senderId;
        this.proposalNum = proposalNum;
        this.proposalVal = proposalVal;
    }

    public String serialize() {
        return String.join(":", type, senderId, proposalNum, proposalVal);
    }

    public static Message parse(String raw) {
        String[] parts = raw.trim().split(":");
        if (parts.length != 4) throw new IllegalArgumentException("Malformed message: " + raw);
        return new Message(parts[0], parts[1], parts[2], parts[3]);
    }
}


