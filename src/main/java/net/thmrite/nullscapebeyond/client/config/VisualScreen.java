package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VisualScreen extends ConfigPageScreen {
    public VisualScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.category.visual"));
    }

    @Override
    protected void buildContent() { }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, Component.translatable("config.nscapebeyond.visual.empty"),
                this.width / 2, rowY(0), 0xAAAAAA);
    }
}