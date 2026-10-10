package net.thmrite.nullscapebeyond.client.classcontrol;

import ca.weblite.objc.Client;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.ClientConfig;

public final class BashFlash {
    private static final int DURATION = 5;      // ticks
    private static final float MAX_ALPHA = 0.55f;
    private static int ticks;

    static boolean photosensitive() {
        return ClientConfig.PHOTOSENSITIVE_MODE.get();
    }

    public static void trigger() {
        if (!photosensitive()) ticks = DURATION;
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        if (ticks <= 0 || photosensitive()) return;
        float t = Mth.clamp((ticks - delta.getGameTimeDeltaPartialTick(false)) / DURATION, 0f, 1f);
        int alpha = (int) (t * MAX_ALPHA * 255);
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), (alpha << 24) | 0xFFFFFF);
    }

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent
        public static void register(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "bash_flash"), BashFlash::render);
        }
    }

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class Ticker {
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            if (ticks > 0) ticks--;
        }
    }

    private BashFlash() {}
}
