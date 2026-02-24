package org.Ch0p5h0p.cryptocraft.client.hashing;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.DefaultedList;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class Hasher {
    public static String hashToBase64(String text) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String hashItem(ItemStack stack) throws Exception {
        //ItemStack stack = p.getMainHandStack();

        // I would name this something else, but I don't know the name of text before you hash it :P
        StringBuilder raw = new StringBuilder();

        // Add item translation key
        raw.append("TRANSLATION_KEY["+stack.getItem().getTranslationKey()+"]");

        // Add name data
        if (stack.get(DataComponentTypes.CUSTOM_NAME) != null) {
            raw.append("CUSTOM_NAME["+stack.get(DataComponentTypes.CUSTOM_NAME).getString()+"]");
        }

        // Add enchantment data (sorted ofc)
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments!=null) {
            Set<RegistryEntry<Enchantment>> enchantmentData = enchantments.getEnchantments();
            ArrayList<String> enchantmentSet = new ArrayList<>();

            for (RegistryEntry<Enchantment> ench : enchantmentData) {
                enchantmentSet.add(ench.value().getClass().getCanonicalName() + ":"+String.valueOf(EnchantmentHelper.getLevel(ench, stack)));
            }

            Collections.sort(enchantmentSet);
            raw.append("ENCHANTS["+String.join("|", enchantmentSet)+"]");
        }


        // Add potion effects
        PotionContentsComponent potionEffects = stack.get(DataComponentTypes.POTION_CONTENTS);
        ArrayList<String> effectSet = new ArrayList<>();
        if (potionEffects != null) {
            for (StatusEffectInstance effect : potionEffects.getEffects()) {
                String effectString = effect.getEffectType().getIdAsString() + "|" +
                        effect.getAmplifier() + "|" +
                        effect.getDuration();
                effectSet.add(effectString);
            }

            Collections.sort(effectSet);
            raw.append("EFFECTS["+String.join("|", effectSet)+"]");
        }

        return hashToBase64(raw.toString());
    }

    public static String hashShulker(ItemStack stack) throws Exception {
        //ItemStack stack = p.getMainHandStack();
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        StringBuilder raw = new StringBuilder();
        if (stack.get(DataComponentTypes.CUSTOM_NAME) != null) {
            raw.append(stack.get(DataComponentTypes.CUSTOM_NAME).getString());
        }
        if (container!=null) {
            //List<ItemStack> stacks = container.stream().toList();
            DefaultedList<ItemStack> stacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            container.copyTo(stacks);

            for (int i=0; i<27; i++) {
                raw.append("|"+hashItem(stacks.get(i)));
            }
        }
        return hashToBase64(raw.toString());

    }
}
