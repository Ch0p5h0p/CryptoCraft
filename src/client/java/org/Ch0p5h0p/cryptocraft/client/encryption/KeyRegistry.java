package org.Ch0p5h0p.cryptocraft.client.encryption;

import java.util.HashMap;
import java.util.Map;

public class KeyRegistry {
    public static Map<String, String> fingerprints = new HashMap<>();

    public static String getFingerprint(String playerName) {
        return fingerprints.get(playerName);
    }

    public static void addFingerprint(String playerName, String fingerprint) {
        fingerprints.put(playerName, fingerprint);
    }
}
