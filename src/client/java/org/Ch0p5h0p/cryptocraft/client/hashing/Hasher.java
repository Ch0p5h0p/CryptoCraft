package org.Ch0p5h0p.cryptocraft.client.hashing;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class Hasher {
    public static String hashToBase64(String text) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String hashItem(PlayerEntity p) throws Exception {
        ItemStack stack = p.getMainHandStack();
        return hashToBase64(stack.getComponents().toString());
    }
}
