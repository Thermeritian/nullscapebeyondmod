package net.thmrite.nullscapebeyond.client.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class ModConfigScreen extends ConfigPageScreen {
    public ModConfigScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.title"));
    }

    @Override
    protected void buildContent() {
        page(0, "credits", CreditsScreen::new);
        page(1, "accessibility", AccessibilityScreen::new);
        page(2, "visual", VisualScreen::new);
        page(3, "sound", SoundScreen::new);
        page(4, "controls", ControlsScreen::new);
    }

    private void page(int row, String key, Function<Screen, Screen> factory) {
        this.addRenderableWidget(Button.builder(
                        Component.translatable("config.nscapebeyond.category." + key),
                        b -> this.minecraft.setScreen(factory.apply(this)))
                .bounds(this.width / 2 - 100, rowY(row), 200, 20).build());
    }
}