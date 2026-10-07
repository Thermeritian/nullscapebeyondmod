package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.thmrite.nullscapebeyond.client.ClientConfig;

public class SoundScreen extends ConfigPageScreen {
    public SoundScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.category.sound"));
    }

    @Override
    protected void buildContent() {
        this.addRenderableWidget(new VolumeSlider(this.width / 2 - 100, rowY(0), 200, 20,
                ClientConfig.SOUNDTRACK_VOLUME.get()));

        this.addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.MENU_SOUNDS.get())
                .create(this.width / 2 - 100, rowY(1), 200, 20,
                        Component.translatable("config.nscapebeyond.sound.menu_sounds"),
                        (button, value) -> ClientConfig.MENU_SOUNDS.set(value)));
    }

    private static class VolumeSlider extends AbstractSliderButton {
        VolumeSlider(int x, int y, int w, int h, double initial) {
            super(x, y, w, h, Component.empty(), initial);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("config.nscapebeyond.sound.soundtrack_volume",
                    (int) Math.round(this.value * 100)));
        }

        @Override
        protected void applyValue() {
            ClientConfig.SOUNDTRACK_VOLUME.set(this.value);
        }
    }
}