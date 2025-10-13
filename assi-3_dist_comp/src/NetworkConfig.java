import java.io.*;
import java.net.*;
import java.util.*;

public class NetworkConfig {
    private Map<String, InetSocketAddress> memberMap;

    public NetworkConfig(String configPath) throws IOException {
        memberMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(configPath));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split(",");
            if (parts.length == 3) {
                String memberId = parts[0];
                String host = parts[1];
                int port = Integer.parseInt(parts[2]);
                memberMap.put(memberId, new InetSocketAddress(host, port));
            }
        }
        br.close();
    }

    public InetSocketAddress getAddress(String memberId) {
        return memberMap.get(memberId);
    }

    public Set<String> getAllMemberIds() {
        return memberMap.keySet();
    }
}
