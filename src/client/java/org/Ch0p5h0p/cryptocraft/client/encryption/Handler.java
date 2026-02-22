package org.Ch0p5h0p.cryptocraft.client.encryption;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class Handler {
    public static PGPPublicKey parsePublicKey(String armoredKey) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(armoredKey.getBytes(StandardCharsets.UTF_8));
        PGPPublicKeyRingCollection keyRings = new PGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(in),
                new JcaKeyFingerprintCalculator()
        );

        for (PGPPublicKeyRing keyRing : keyRings) {
            for (PGPPublicKey key : keyRing) {
                if (key.isEncryptionKey()) return key;
            }
        }

        throw new IllegalArgumentException("No usable public key found.");
    }

    public static void saveKeyToFile(PGPPublicKey key, String playername) throws Exception {
        // Old path would replace playername with Long.toHexString(key.getKeyID())
        String filename = "CryptoCraft/keys/" + playername + ".asc";
        try (ArmoredOutputStream out = new ArmoredOutputStream(new FileOutputStream(filename))) {
            key.encode(out);
            KeyManager.init();
        }
    }
}
