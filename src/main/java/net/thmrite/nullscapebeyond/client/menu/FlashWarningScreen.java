package net.thmrite.nullscapebeyond.client.menu;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class FlashWarningScreen extends Screen {
    private final Screen next;
    private List<FormattedCharSequence> lines = List.of();
    private int textTop;

    public FlashWarningScreen(Screen next) {
        super(Component.translatable("screen.nscapebeyond.flash_warning.title"));
        this.next = next;
    }

    @Override
    protected void init() {
        this.lines = this.font.split(
                Component.translatable("screen.nscapebeyond.flash_warning.body"),
                Math.min(this.width - 40, 320));

        int lineH = this.font.lineHeight + 2;
        int totalHeight = 20 + lines.size() * lineH + 30 + 20;
        this.textTop = (this.height - totalHeight) / 2;

        int buttonY = textTop + 20 + lines.size() * lineH + 30;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.nscapebeyond.flash_warning.continue"),
                        button -> this.onClose())
                .bounds(this.width / 2 - 100, buttonY, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); // background + button

        graphics.drawCenteredString(this.font, this.title, this.width / 2, textTop, 0xFFFF55);

        int y = textTop + 20;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFFFFF);
            y += this.font.lineHeight + 2;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // force the player to acknowledge it
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(next);
    }
}