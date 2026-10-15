package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
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

        this.addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.COLORED_LIGHTING.get())
                .withTooltip(v -> Tooltip.create(
                        Component.translatable("config.nscapebeyond.visual.colored_lighting.tooltip")))
                .create(this.width / 2 - 100, rowY(1), 200, 20,
                        Component.translatable("config.nscapebeyond.visual.colored_lighting"),
                        (button, value) -> ClientConfig.COLORED_LIGHTING.set(value)));

        this.addRenderableWidget(new LightStrengthSlider(this.width / 2 - 100, rowY(2), 200, 20));
    }

    /** 0%..200% slider bound to ClientConfig.LIGHT_STRENGTH (0.0..2.0). */
    private static class LightStrengthSlider extends AbstractSliderButton {
        LightStrengthSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), ClientConfig.LIGHT_STRENGTH.get() / 2.0);
            setTooltip(Tooltip.create(Component.translatable("config.nscapebeyond.visual.light_strength.tooltip")));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("config.nscapebeyond.visual.light_strength",
                    Math.round(this.value * 200.0)));
        }

        @Override
        protected void applyValue() {
            ClientConfig.LIGHT_STRENGTH.set(this.value * 2.0);
        }
    }
}