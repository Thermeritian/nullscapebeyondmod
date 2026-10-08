package net.thmrite.nullscapebeyond.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.thmrite.nullscapebeyond.client.ModKeybinds;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ControlsScreen extends ConfigPageScreen {
    private final List<Button> keyButtons = new ArrayList<>();
    private KeyMapping listening;

    public ControlsScreen(Screen parent) {
        super(parent, Component.translatable("config.nscapebeyond.category.controls"));
    }

    // Adjust these two lines if your fields are named differently or are Lazy<KeyMapping> (then add .get())
    private static List<KeyMapping> mappings() {
        return List.of(ModKeybinds.ABILITY, ModKeybinds.ALT_ABILITY);
    }

    @Override
    protected void buildContent() {
        keyButtons.clear();
        int i = 0;
        for (KeyMapping mapping : mappings()) {
            int y = rowY(i++);
            Button keyButton = Button.builder(Component.empty(), b -> {
                        listening = mapping;
                        refresh();
                    })
                    .bounds(this.width / 2 - 10, y, 100, 20).build();
            keyButtons.add(keyButton);
            this.addRenderableWidget(keyButton);

            this.addRenderableWidget(Button.builder(Component.translatable("controls.reset"),
                            b -> setKey(mapping, mapping.getDefaultKey()))
                    .bounds(this.width / 2 + 95, y, 60, 20).build());
        }
        refresh();
    }

    private void setKey(KeyMapping mapping, InputConstants.Key key) {
        this.minecraft.options.setKey(mapping, key);
        KeyMapping.resetMapping();
        listening = null;
        refresh();
    }

    private void refresh() {
        List<KeyMapping> list = mappings();
        for (int i = 0; i < list.size(); i++) {
            KeyMapping mapping = list.get(i);
            MutableComponent text = mapping.getTranslatedKeyMessage().copy();
            if (mapping == listening) {
                text = Component.literal("> ").append(text.withStyle(ChatFormatting.YELLOW)).append(" <")
                        .withStyle(ChatFormatting.YELLOW);
            } else if (conflicts(mapping)) {
                text.withStyle(ChatFormatting.RED);
            }
            keyButtons.get(i).setMessage(text);
        }
    }

    private boolean conflicts(KeyMapping mapping) {
        if (mapping.isUnbound()) {
            return false;
        }
        for (KeyMapping other : this.minecraft.options.keyMappings) {
            if (other != mapping && mapping.same(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            setKey(listening, keyCode == GLFW.GLFW_KEY_ESCAPE
                    ? InputConstants.UNKNOWN
                    : InputConstants.getKey(keyCode, scanCode));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listening != null) {
            setKey(listening, InputConstants.Type.MOUSE.getOrCreate(button));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int i = 0;
        for (KeyMapping mapping : mappings()) {
            g.drawString(this.font, Component.translatable(mapping.getName()),
                    this.width / 2 - 155, rowY(i++) + 6, 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.options.save(); // keybinds live in options.txt, not in your config file
        super.onClose();
    }
}