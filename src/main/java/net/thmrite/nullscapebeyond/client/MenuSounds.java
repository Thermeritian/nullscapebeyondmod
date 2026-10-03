package net.thmrite.nullscapebeyond.client;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.thmrite.nullscapebeyond.ModSounds;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public class MenuSounds {
    private static final ResourceLocation VANILLA_CLICK =
            ResourceLocation.withDefaultNamespace("ui.button.click");

    private static final Deque<Screen> history = new ArrayDeque<>(); // screens we came from
    private static Screen hoverScreen;
    private static AbstractButton hoveredButton;
    private static SoundInstance lastClick;
    private static long lastClickTime;

    /** "Main menu" = no world loaded. In a world, everything stays vanilla. */
    private static boolean inMenu() {
        return Minecraft.getInstance().level == null;
    }

    private static SoundInstance ui(SoundEvent event) {
        return SimpleSoundInstance.forUI(event, 1.0F);
    }

    // ---- Click: swap the shared vanilla click sound, menu only ----
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound != null && inMenu() && sound.getLocation().equals(VANILLA_CLICK)) {
            lastClick = ui(ModSounds.MENU_CLICK.get());
            lastClickTime = Util.getMillis();
            event.setSound(lastClick);
        }
    }

    // ---- Hover: fires when the mouse moves onto a different button ----
    @SubscribeEvent
    public static void onRenderPost(ScreenEvent.Render.Post event) {
        if (!inMenu()) {
            hoverScreen = null;
            hoveredButton = null;
            return;
        }
        Screen screen = event.getScreen();

        AbstractButton hovered = null;
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractButton b && b.visible && b.active && b.isHovered()) {
                hovered = b;
                break;
            }
        }

        if (screen != hoverScreen) {
            hoverScreen = screen; // new screen: sync silently, no sound on the first frame
        } else if (hovered != null && hovered != hoveredButton) {
            Minecraft.getInstance().getSoundManager().play(ui(ModSounds.MENU_HOVER.get()));
        }
        hoveredButton = hovered;
    }

    // ---- Return: we opened a screen we were previously on ----
    // LOWEST priority so we see the final screen after other handlers (e.g. the warning screen)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen current = event.getCurrentScreen();
        Screen next = event.getNewScreen();

        if (next == null) {          // back to the game
            history.clear();
            return;
        }
        if (history.contains(next)) {
            while (!history.isEmpty() && history.peek() != next) {
                history.pop();
            }
            if (!history.isEmpty()) {
                history.pop();       // next becomes the current screen
            }
            playReturn();
        } else if (current != null && current != next) {
            history.push(current);
        }
    }

    private static void playReturn() {
        if (!inMenu()) {
            return;
        }
        var soundManager = Minecraft.getInstance().getSoundManager();
        // If a button click caused this, drop its click sound so only Return is heard
        if (lastClick != null && Util.getMillis() - lastClickTime < 100) {
            soundManager.stop(lastClick);
        }
        soundManager.play(ui(ModSounds.MENU_RETURN.get()));
    }
}