package org.Ch0p5h0p.cryptocraft.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.Ch0p5h0p.cryptocraft.client.GUI.CreatePassphraseScreen;
import org.Ch0p5h0p.cryptocraft.client.GUI.PassphraseLoginScreen;
import org.Ch0p5h0p.cryptocraft.client.compression.GZip;
import org.Ch0p5h0p.cryptocraft.client.encryption.*;
import org.Ch0p5h0p.cryptocraft.client.hashing.Hasher;
import org.Ch0p5h0p.cryptocraft.client.util.BookHandler;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.List;

public class CryptocraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ensurePaths();
        Security.addProvider(new BouncyCastleProvider());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            try {
                if (PrivateKeyManager.getPassphrase() == null) {
                    if (PrivateKeyManager.hasPrivateKey(client.player.getName().getString())) {
                        client.setScreen(new PassphraseLoginScreen(null));
                    } else {
                        client.setScreen(new CreatePassphraseScreen(null, client.player.getName().getString()));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        ClientCommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess) -> {
            commandDispatcher.register(
                    ClientCommandManager.literal("hash_item")
                            .executes(ctx -> {
                                MinecraftClient mc = MinecraftClient.getInstance();
                                if (mc.player == null) return 0;
                                try {
                                    mc.player.sendMessage(Text.of(Hasher.hashItem(mc.player.getMainHandStack())), false);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                return 0;
                            })
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("hash_shulker")
                            .executes(ctx -> {
                                MinecraftClient mc = MinecraftClient.getInstance();
                                if (mc.player == null) return 0;
                                try {
                                    if (!mc.player.getMainHandStack().getItem().getTranslationKey().contains("shulker_box")) {
                                        mc.player.sendMessage(Text.of("You're not holding a shulker!"), false);
                                        return 0;
                                    }
                                    mc.player.sendMessage(Text.of(Hasher.hashShulker(mc.player.getMainHandStack())), false);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                return 0;
                            })
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("sign")
                            .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc.player == null) return 0;
                                    try {
                                        String signature = PGP.sign(StringArgumentType.getString(ctx, "text"));
                                        mc.player.sendMessage(Text.of(signature), false);
                                        return 0;
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                            )
                    );
            commandDispatcher.register(
                    ClientCommandManager.literal("verify")
                            .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                .then(ClientCommandManager.argument("signature", StringArgumentType.string())
                                    .then(ClientCommandManager.argument("original message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            MinecraftClient mc = MinecraftClient.getInstance();
                                            if (mc.player == null) return 0;
                                            String fingerprint = KeyRegistry.getFingerprint(StringArgumentType.getString(ctx, "player"));
                                            PGPPublicKey key = KeyManager.getKey(fingerprint);
                                            try {
                                                boolean valid = PGP.verify(
                                                        StringArgumentType.getString(ctx, "original message"),
                                                        StringArgumentType.getString(ctx, "signature"),
                                                        key
                                                );
                                                mc.player.sendMessage(Text.of(valid ? "Valid signature" : "Invalid signature"), false);
                                                return 0;
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }

                                        })
                                    )
                                )
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("sym_encrypt")
                            .then(ClientCommandManager.argument("key", StringArgumentType.word())
                                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc.player == null) return 0;
                                        try {
                                            String encrypted = PGP.sym_encrypt(
                                                    StringArgumentType.getString(ctx, "message"),
                                                    AES_Util.padKey(StringArgumentType.getString(ctx, "key")).getBytes(StandardCharsets.UTF_8)
                                            );
                                            mc.player.sendMessage(Text.of(encrypted), false);
                                            return 0;
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                )
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("sym_decrypt")
                            .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                .then(ClientCommandManager.argument("encrypted", StringArgumentType.string())
                                    .executes(ctx -> {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc.player == null) return 0;
                                        try {
                                            String decrypted = PGP.sym_decrypt(
                                                    StringArgumentType.getString(ctx, "encrypted"),
                                                    AES_Util.padKey(StringArgumentType.getString(ctx, "key")).getBytes(StandardCharsets.UTF_8)
                                            );
                                            mc.player.sendMessage(Text.of(decrypted), false);
                                            return 0;
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                )
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("asym_encrypt")
                            .then(ClientCommandManager.argument("recipient", StringArgumentType.string())
                                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                MinecraftClient mc = MinecraftClient.getInstance();
                                                if (mc.player == null) return 0;
                                                try {
                                                    String encrypted = PGP.asym_encrypt(
                                                            StringArgumentType.getString(ctx, "message"),
                                                            KeyManager.getKey(KeyRegistry.getFingerprint(StringArgumentType.getString(ctx, "recipient")))
                                                    );
                                                    mc.player.sendMessage(Text.of(encrypted), false);
                                                    return 0;
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            })
                                    )
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("asym_decrypt")
                            .then(ClientCommandManager.argument("encrypted", StringArgumentType.string())
                                    .executes(ctx -> {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc.player == null) return 0;
                                        try{
                                            String decrypted = PGP.asym_decrypt(StringArgumentType.getString(ctx, "encrypted"));
                                            mc.player.sendMessage(Text.of(decrypted), false);
                                            return 0;
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("get_key_fingerprints")
                            .executes(ctx -> {
                                MinecraftClient mc = MinecraftClient.getInstance();
                                if (mc.player == null) return 0;
                                for (String player : KeyRegistry.fingerprints.keySet()) {
                                    mc.player.sendMessage(Text.of(player+": "+KeyRegistry.getFingerprint(player)), false);
                                }
                                return 0;
                            })
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("add_key")
                            .then(ClientCommandManager.argument("player name", StringArgumentType.string())
                                    .then(ClientCommandManager.argument("key", StringArgumentType.string())
                                            .executes(ctx -> {
                                                MinecraftClient mc = MinecraftClient.getInstance();
                                                if (mc.player == null) return 0;
                                                try {
                                                    PGPPublicKey parsed = Handler.parsePublicKey(StringArgumentType.getString(ctx,"key"));
                                                    Handler.saveKeyToFile(parsed, StringArgumentType.getString(ctx, "player name"));
                                                    return 0;
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            })
                                    )
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("get_public")
                            .executes(ctx -> {
                                MinecraftClient mc = MinecraftClient.getInstance();
                                if (mc.player == null) return 0;
                                try {
                                    byte[] encoded = PrivateKeyManager.getPublic().getEncoded();
                                    String publicKeyString = Base64.getEncoder().encodeToString(encoded);
                                    mc.player.sendMessage(Text.of(publicKeyString), false);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return 0;
                            })
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("compress")
                            .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc.player == null) return 0;
                                        try {
                                            mc.player.sendMessage(Text.of(GZip.compressString(StringArgumentType.getString(ctx, "text"))), false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        return 0;
                                    })
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("decompress")
                            .then(ClientCommandManager.argument("compressed text", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc.player == null) return 0;
                                        try {
                                            mc.player.sendMessage(Text.of(GZip.decompressString(StringArgumentType.getString(ctx, "compressed text"))), false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        return 0;
                                    })
                            )
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("read_book")
                            .executes(ctx -> {
                                MinecraftClient mc = MinecraftClient.getInstance();
                                if (mc.player == null) return 0;
                                mc.player.sendMessage(Text.of(BookHandler.readBook(mc.player)), false);
                                return 0;
                            })
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("hash_book")
                            .executes(ctx -> {
                                MinecraftClient mc = MinecraftClient.getInstance();
                                if (mc.player == null) return 0;
                                try {
                                    mc.player.sendMessage(Text.of(Hasher.hashToBase64(BookHandler.readBook(mc.player))), false);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                return 0;
                            })
            );
            commandDispatcher.register(
                    ClientCommandManager.literal("chunk_text")
                            .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc.player == null) return 0;
                                        List<String> data = BookHandler.chunkString(StringArgumentType.getString(ctx, "text"));
                                        for (String chunk : data) {
                                            mc.player.sendMessage(Text.of(chunk), false);
                                            mc.player.sendMessage(Text.of(""), false);
                                        }
                                        return 0;
                                    })
                            )
            );

        }));
    }

    public static void ensurePaths() {
        File keyDir = new File("CryptoCraft/keys");
        if (!keyDir.exists()) {
            keyDir.mkdirs(); // create CryptoCraft/keys folder
        }
    }
}
