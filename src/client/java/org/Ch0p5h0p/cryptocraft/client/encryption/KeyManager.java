package org.Ch0p5h0p.cryptocraft.client.encryption;

import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KeyManager {
    private static final Map<String, PGPPublicKey> keys = new HashMap<>();

    private static String passphrase;

    public static void init() {
        File[] files = getKeys();
        if (files == null) return;
        keys.clear();

        for (File file : files) {
            try {
                loadKeyFromFile(file);
            } catch (Exception e){
                System.err.println("Failed to load key: "+file.getName());
                e.printStackTrace();
            }
        }
    }

    private static File[] getKeys() {
        File keyDir= new File("CryptoCraft/keys");
        if (!keyDir.exists()) {
            keyDir.mkdirs();
        }
        return keyDir.listFiles((dir, name) ->
            name.endsWith(".asc") || name.endsWith(".pgp")
        );
    }

    private static void loadKeyFromFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             InputStream decoder = PGPUtil.getDecoderStream(fis)) {
            PGPPublicKeyRingCollection rings = new PGPPublicKeyRingCollection(
                    decoder,
                    new JcaKeyFingerprintCalculator()
            );
            Iterator<PGPPublicKeyRing> ringIter = rings.getKeyRings();

            while (ringIter.hasNext()) {
                PGPPublicKeyRing ring = ringIter.next();

                PGPPublicKey key = extractSigningKey(ring);

                if (key != null) {
                    String fingerprint = Hex.toHexString(key.getFingerprint());
                    keys.put(fingerprint, key);
                    KeyRegistry.addFingerprint(FilenameUtils.removeExtension(file.getName()), fingerprint);

                    System.out.println("Loaded key: "+fingerprint);
                }
            }
        }
    }

    private static PGPPublicKey extractSigningKey(PGPPublicKeyRing ring) {
        Iterator<PGPPublicKey> keys = ring.getPublicKeys();

        while (keys.hasNext()) {
            PGPPublicKey key = keys.next();
            if (!key.isEncryptionKey() & !key.isMasterKey()) {
                return key;
            }
        }
        return null;
    }

    public static PGPPublicKey getKey(String fingerprint) {
        return keys.get(fingerprint);
    }
}
