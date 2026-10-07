package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.thmrite.nullscapebeyond.client.ClientConfig;

public class AccessibilityScreen extends ConfigPageScreen {
    public AccessibilityScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.category.accessibility"));
    }

    @Override
    protected void buildContent() {
        this.addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.PHOTOSENSITIVE_MODE.get())
                .withTooltip(v -> Tooltip.create(
                        Component.translatable("config.nscapebeyond.accessibility.photosensitive.tooltip")))
                .create(this.width / 2 - 100, rowY(0), 200, 20,
                        Component.translatable("config.nscapebeyond.accessibility.photosensitive"),
                        (button, value) -> ClientConfig.PHOTOSENSITIVE_MODE.set(value)));

        this.addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.SHOW_FLASH_WARNING.get())
                .create(this.width / 2 - 100, rowY(1), 200, 20,
                        Component.translatable("config.nscapebeyond.accessibility.flash_warning"),
                        (button, value) -> ClientConfig.SHOW_FLASH_WARNING.set(value)));
    }
}