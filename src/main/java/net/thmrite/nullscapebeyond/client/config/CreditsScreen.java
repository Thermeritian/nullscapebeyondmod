package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class CreditsScreen extends ConfigPageScreen {
    private List<FormattedCharSequence> lines = List.of();

    public CreditsScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.category.credits"));
    }

    @Override
    protected void buildContent() {
        lines = this.font.split(Component.translatable("config.nscapebeyond.credits.text"),
                Math.min(this.width - 40, 320));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int y = 40;
        for (FormattedCharSequence line : lines) {
            g.drawCenteredString(this.font, line, this.width / 2, y, 0xDDDDDD);
            y += this.font.lineHeight + 2;
        }
    }
}