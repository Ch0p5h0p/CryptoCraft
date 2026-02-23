package org.Ch0p5h0p.cryptocraft.client.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BookHandler {
    // worst-case chars per page: 266
    public static List<String> chunkString(String text) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int chunkSize = 266;

        for (int i = 0; i < length; i+=chunkSize) {
            int end = Math.min(length, i+chunkSize);
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }

    // Potentially without use.
    /*
    public static String concatChunks(List<String> chunks) {
        StringBuilder sb = new StringBuilder();
        for (String chunk : chunks) {
            sb.append(chunk);
        }
        return sb.toString();
    }
    */

    public static String readBook(PlayerEntity p) {
        ItemStack stack = p.getMainHandStack();
        String key = stack.getItem().getTranslationKey();
        StringBuilder concatenated = new StringBuilder();

        if (key.contains("writable_book")) {
            if (stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT) == null) return null;
            List<RawFilteredPair<String>> data = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT).pages();
            for (RawFilteredPair<String> pair : data) {
                concatenated.append(pair.get(false));
            }
        } else if (key.contains("written_book")) {
            if (stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) == null) return null;
            List<RawFilteredPair<Text>> data = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).pages();
            for (RawFilteredPair<Text> pair : data) {
                concatenated.append(pair.get(false).getString());
            }
        }


        return concatenated.toString();


    }
}
