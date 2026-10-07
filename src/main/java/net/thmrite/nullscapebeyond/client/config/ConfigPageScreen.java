package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.thmrite.nullscapebeyond.client.ClientConfig;

public abstract class ConfigPageScreen extends Screen {
    protected final Screen parent;

    protected ConfigPageScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    protected int rowY(int index) {
        return this.height / 6 + index * 24;
    }

    protected abstract void buildContent();

    @Override
    protected void init() {
        buildContent();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        ClientConfig.SPEC.save(); // write changes to config/nscapebeyond-client.toml
        this.minecraft.setScreen(parent);
    }
}