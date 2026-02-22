package org.Ch0p5h0p.cryptocraft.client.encryption;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;

public class PrivateKeyManager {
    private static PGPPublicKey myPublic;
    private static PGPPrivateKey myPrivate;
    private static char[] passphrase = null;
    private static boolean loggedIn = false;

    public static boolean hasPrivateKey(String playername) throws Exception {
        System.out.println("Checking");
        File keyfile = new File(MinecraftClient.getInstance().runDirectory, "CryptoCraft/keys/" +playername+".asc");
        System.out.println("Done");
        if (!keyfile.exists()) {
            return false;
        } else {
            return true;
        }
    }

    public static void loadPrivateKey(String playername) throws Exception{
        char[] passphrase = getPassphrase();
        try (InputStream in = PGPUtil.getDecoderStream(new FileInputStream(new File(MinecraftClient.getInstance().runDirectory, "CryptoCraft/keys/"  + playername+".asc")))) {
            PGPSecretKeyRingCollection keyRings = new PGPSecretKeyRingCollection(
                    in, new JcaKeyFingerprintCalculator()
            );

            // grab first signing key
            for (PGPSecretKeyRing ring : keyRings) {
                PGPSecretKey secret = ring.getSecretKey();
                if (secret != null && secret.isSigningKey()) {
                    myPrivate = secret.extractPrivateKey(
                            new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(passphrase)
                    );
                    myPublic = secret.getPublicKey();
                    break;
                }
            }
        }

        if (myPrivate == null) {
            generatePrivateKey(playername);
        }
    }

    public static void generatePrivateKey(String playername) throws Exception {
        File file = new File(MinecraftClient.getInstance().runDirectory, "CryptoCraft/keys/" +playername+".asc");
        char[] passphrase = getPassphrase();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024, new SecureRandom());
        java.security.KeyPair kp = kpg.generateKeyPair();

        PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, new Date());

        PGPSecretKey secretKey = new PGPSecretKey(
                PGPSignature.DEFAULT_CERTIFICATION,
                pgpKeyPair,
                playername,
                new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1),
                null,
                null,
                new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.CAST5, new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA256))
                        .setProvider("BC")
                        .build(passphrase)
        );

        // Save to file
        file.getParentFile().mkdirs(); // ensure the dir exists
        try (FileOutputStream out = new FileOutputStream(file)) {
            secretKey.encode(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        myPrivate = secretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder()
                        .setProvider("BC")
                        .build(passphrase)
        );
        myPublic = secretKey.getPublicKey();
    }

    public static PGPPrivateKey getPrivate() {
        return myPrivate;
    }

    public static PGPPublicKey getPublic() {
        return myPublic;
    }

    public static void setPassphrase(char[] pass) {
        passphrase=pass;
    }

    public static char[] getPassphrase() {
        return passphrase;
    }

    public static boolean validateKey(String playername, char[] passphrase) {
        try (InputStream in = PGPUtil.getDecoderStream(
                new FileInputStream(new File(MinecraftClient.getInstance().runDirectory, "CryptoCraft/keys/"  + playername+ ".asc")))) {

            PGPSecretKeyRingCollection keyRings =
                    new PGPSecretKeyRingCollection(in, new JcaKeyFingerprintCalculator());

            for (PGPSecretKeyRing ring : keyRings) {
                PGPSecretKey secret = ring.getSecretKey();
                if (secret != null && secret.isSigningKey()) {

                    secret.extractPrivateKey(
                            new JcePBESecretKeyDecryptorBuilder()
                                    .setProvider("BC")
                                    .build(passphrase)
                    );

                    loggedIn=true;
                    return true; // success = correct passphrase
                }
            }

        } catch (Exception ignored) {
            return false;
        }

        return false;
    }
}
