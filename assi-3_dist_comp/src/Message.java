public class Message {
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

    // Example serialization: PREPARE:M1:3.1:M5
    public String serialize() {
        return String.join(":", type, senderId, proposalNum, proposalVal);
    }

    public static Message parse(String raw) {
        String[] parts = raw.trim().split(":");
        if (parts.length != 4) throw new IllegalArgumentException("Malformed message: " + raw);
        return new Message(parts[0], parts[1], parts[2], parts[3]);
    }
}
