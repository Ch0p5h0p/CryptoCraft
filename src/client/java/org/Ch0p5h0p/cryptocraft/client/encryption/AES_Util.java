package org.Ch0p5h0p.cryptocraft.client.encryption;

import net.minecraft.structure.OceanMonumentGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AES_Util {
    public static byte[] generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey key = kg.generateKey();
        return key.getEncoded();
    }

    public static String padKey(String key) {
        StringBuilder paddedKey = new StringBuilder(key);
        while (paddedKey.length()%16 > 0) {
            paddedKey.append("0");
        }
        return paddedKey.toString();
    }

    public static String encrypt(String plaintext, byte[] keyBytes) throws Exception {
        byte[] iv = new byte[16]; // 128 bit IV for AES
        new SecureRandom().nextBytes(iv);

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // IV + ciphertext and base64 it
        ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
        buf.put(iv);
        buf.put(ciphertext);
        return Base64.getEncoder().encodeToString(buf.array());
    }

    public static String decrypt(String base64Ciphertext, byte[] keyBytes) throws Exception {
        byte[] all = Base64.getDecoder().decode(base64Ciphertext);

        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[all.length - 16];
        System.arraycopy(all, 0, iv, 0, 16);
        System.arraycopy(all, 16, ciphertext, 0, ciphertext.length);

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] plain = cipher.doFinal(ciphertext);
        return new String(plain, StandardCharsets.UTF_8);
    }
}
