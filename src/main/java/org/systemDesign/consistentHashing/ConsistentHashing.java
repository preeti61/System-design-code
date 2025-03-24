package org.systemDesign.consistentHashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

class ConsistentHashing {
    private final TreeMap<Integer, String> ring = new TreeMap<>();
    private final int virtualNodes;

    public ConsistentHashing(List<String> servers, int virtualNodes) {
        this.virtualNodes = virtualNodes;
        for (String server : servers) {
            addServer(server);
        }
    }

    // Add a server to the hash ring
    public void addServer(String server) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = hash(server + i);
            ring.put(hash, server);
        }
    }

    // Remove a server from the hash ring
    public void removeServer(String server) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = hash(server + i);
            ring.remove(hash);
        }
    }

    // Get server for a given key
    public String getServer(String key) {
        if(ring.isEmpty()) {
            return  null;
        }
        int keyHash = hash(key);
        Integer dest =  ring.ceilingKey(keyHash);
        if(dest == null) {
            dest = ring.firstKey();
        }
        return ring.get(dest);
    }

    // Hash function using MD5
    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        List<String> servers = Arrays.asList("A12-3", "B34-5", "C12-3", "D88-7");
        ConsistentHashing ch = new ConsistentHashing(servers, 3);

        String[] keys = {"User1", "User2", "User3", "User4"};
        for (String key : keys) {
            System.out.println(key + " is assigned to " + ch.getServer(key));
        }

        System.out.println("\nRemoving Server A...");
        ch.removeServer("A12-3");

        for (String key : keys) {
            System.out.println(key + " is now assigned to " + ch.getServer(key));
        }
    }
}
