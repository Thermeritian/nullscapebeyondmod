package net.thmrite.nullscapebeyond.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.classcontrol.ChargerController;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class StaminaBarOverlay {
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation FG = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    @SubscribeEvent
    public static void onLayer(RenderGuiLayerEvent.Pre event) {
        if (!ChargerController.isActive()) return;

        ResourceLocation name = event.getName();
        if (name.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
            event.setCanceled(true); // hide the green level number too
            return;
        }
        if (!name.equals(VanillaGuiLayers.EXPERIENCE_BAR)) return;
        event.setCanceled(true);

        GuiGraphics g = event.getGuiGraphics();
        int x = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - 32 + 3;
        float frac = Mth.clamp(ChargerController.stamina / ChargerController.maxStamina, 0f, 1f);

        g.blitSprite(BG, 182, 5, 0, 0, x, y, 182, 5);
        int w = (int) (frac * 182);
        if (w > 0) {
            if (ChargerController.charging) {
                g.fill(x, y, x + w, y + 5, 0xFFFFFFFF); // white while charging
            } else {
                g.setColor(1.0f, 0.85f, 0.1f, 1.0f); // yellow while recovering
                g.blitSprite(FG, 182, 5, 0, 0, x, y, w, 5);
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    private StaminaBarOverlay() {}
}
