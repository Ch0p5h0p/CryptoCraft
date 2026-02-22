package org.Ch0p5h0p.cryptocraft.client.encryption;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGInputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.encoders.Base64;

import javax.swing.text.html.HTMLDocument;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Iterator;

public class PGP {
    public static void init(String playername) throws Exception {
        System.out.println("Loading");
        PrivateKeyManager.loadPrivateKey(playername);
        System.out.println("Loaded. Initializing");
        KeyManager.init();
        System.out.println("Initialized");

    }

    public static String sign(String message) throws Exception {
        PGPPrivateKey privKey = PrivateKeyManager.getPrivate();
        PGPPublicKey pubKey = PrivateKeyManager.getPublic();

        PGPSignatureGenerator sigGen = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(pubKey.getAlgorithm(), HashAlgorithmTags.SHA256)
                        .setProvider("BC")
        );
        sigGen.init(PGPSignature.BINARY_DOCUMENT, privKey);
        sigGen.update(message.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sigGen.generate().encode(out);

        return Base64.toBase64String(out.toByteArray());
    }

    public static boolean verify(String message, String base64Signature, PGPPublicKey publicKey) throws Exception {
        byte[] sigBytes = Base64.decode(base64Signature);
        ByteArrayInputStream in = new ByteArrayInputStream(sigBytes);
        PGPSignature sig = new PGPSignature(new BCPGInputStream(in));

        sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
        sig.update(message.getBytes(StandardCharsets.UTF_8));

        return sig.verify();
    }

    public static String asym_encrypt(String plaintext, PGPPublicKey recipient) throws Exception {
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();

        // ASCII Base64 armoring
        ArmoredOutputStream armoredOut = new ArmoredOutputStream(encOut);

        // Generate session key
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                        .setWithIntegrityPacket(true)
                        .setSecureRandom(new SecureRandom())
                        .setProvider("BC")
        );
        encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(recipient).setProvider("BC"));

        OutputStream cOut = encGen.open(armoredOut, new byte[4096]);
        cOut.write(plaintext.getBytes(StandardCharsets.UTF_8));
        cOut.close();
        armoredOut.close();

        return Base64.toBase64String(encOut.toByteArray());
    }

    public static String asym_decrypt(String base64CipherText) throws Exception {
            PGPPrivateKey privKey = PrivateKeyManager.getPrivate();

            byte[] data = Base64.decode(base64CipherText);
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            InputStream decoder = PGPUtil.getDecoderStream(in);

            PGPObjectFactory pgpFact = new PGPObjectFactory(decoder, new JcaKeyFingerprintCalculator());
            Object obj = pgpFact.nextObject();

            if (!(obj instanceof PGPEncryptedDataList)) {
                obj = pgpFact.nextObject();
            }

            PGPEncryptedDataList encList = (PGPEncryptedDataList) obj;
            Iterator<PGPEncryptedData> it = encList.getEncryptedDataObjects();
            PGPPublicKeyEncryptedData pbe = null;
            while (it.hasNext()) {
                PGPPublicKeyEncryptedData candidate = (PGPPublicKeyEncryptedData) it.next();
                if (candidate.getKeyIdentifier() == privKey.getKeyIdentifier(new JcaKeyFingerprintCalculator())) {
                    pbe = candidate;
                    break;
                }
            }

            if (pbe == null) throw new IllegalArgumentException("Encrypted data not for your key!");

            InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder()
                    .setProvider("BC").build(privKey));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int ch;
            while ((ch = clear.read()) >= 0) out.write(ch);
            clear.close();

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public static String sym_encrypt(String plaintext, byte[] key) throws Exception {
        return AES_Util.encrypt(plaintext, key);
    }

    public static String sym_decrypt(String ciphertext, byte[] key) throws Exception {
        return AES_Util.decrypt(ciphertext, key);
    }
}
