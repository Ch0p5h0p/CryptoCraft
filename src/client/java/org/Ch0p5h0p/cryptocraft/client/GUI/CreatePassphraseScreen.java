package org.Ch0p5h0p.cryptocraft.client.GUI;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.Ch0p5h0p.cryptocraft.client.encryption.PGP;
import org.Ch0p5h0p.cryptocraft.client.encryption.PrivateKeyManager;


public class CreatePassphraseScreen extends Screen {
    private TextFieldWidget passField;
    private final Screen parent;
    private final String playername;
    private Text errorMessage = Text.empty();

    public CreatePassphraseScreen(Screen parent, String playername) {
        super(Text.literal("Create a passphrase"));
        this.parent = parent;
        this.playername = playername;
    }

    @Override
    protected void init() {
        int centerX = this.width/2;
        int centerY = this.height/2;

        passField = new TextFieldWidget(
                this.textRenderer,
                centerX-100,
                centerY-10,
                200,
                20,
                Text.literal("Passphrase")
        );


        passField.setMaxLength(256);
        passField.setFocusUnlocked(false);
        passField.setFocused(true);

        this.addDrawableChild(passField);

        // Confirm button
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Register"), button -> {
                    String pass = passField.getText();
                    PrivateKeyManager.setPassphrase(pass.toCharArray());
                    try {
                        PrivateKeyManager.generatePrivateKey(playername);
                        PGP.init(String.valueOf(MinecraftClient.getInstance().player.getName().getString()));
                        this.client.setScreen(parent);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).dimensions(centerX-50, centerY+20, 100, 20).build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //20this.renderBackground(context, mouseX, mouseY, delta);

        context.fillGradient(0, 0, this.width, this.height, 0xFF202020, 0xFF202020);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);


        super.render(context, mouseX, mouseY, delta);

        if (!errorMessage.getString().isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    errorMessage,
                    this.width/2,
                    this.height/2 - 35,
                    0xFF5555
            );
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
