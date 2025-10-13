import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class NetworkConfig {
    private final Map<String, InetSocketAddress> memberAddresses = new HashMap<>();

    public NetworkConfig(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 3) {
                    memberAddresses.put(parts[0], new InetSocketAddress(parts[1], Integer.parseInt(parts[2])));
                }
            }
        }
    }

    public InetSocketAddress getAddress(String memberId) {
        return memberAddresses.get(memberId);
    }

    public Set<String> getMemberIds() {
        return memberAddresses.keySet();
    }
}