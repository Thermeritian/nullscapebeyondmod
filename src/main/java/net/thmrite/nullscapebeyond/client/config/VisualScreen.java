package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.thmrite.nullscapebeyond.client.ClientConfig;

public class VisualScreen extends ConfigPageScreen {
    public VisualScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.category.visual"));
    }

    @Override
    protected void buildContent() {
        this.addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.CUSTOM_ACCESSORY_TOOLTIP.get())
                .withTooltip(v -> Tooltip.create(
                        Component.translatable("config.nscapebeyond.visual.accessory_tooltip.tooltip")))
                .create(this.width / 2 - 100, rowY(0), 200, 20,
                        Component.translatable("config.nscapebeyond.visual.accessory_tooltip"),
                        (button, value) -> ClientConfig.CUSTOM_ACCESSORY_TOOLTIP.set(value)));
    }
}