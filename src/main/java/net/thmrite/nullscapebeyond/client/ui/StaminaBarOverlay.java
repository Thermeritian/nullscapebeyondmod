package net.thmrite.nullscapebeyond.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.classcontrol.ChargerController;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class StaminaBarOverlay {
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation XP = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
    private static final ResourceLocation STAMINA = ResourceLocation.fromNamespaceAndPath(
            NullscapeBeyond.MODID, "textures/gui/hotbar/class_stamina_bar_overlay.png");

    private static final float FADE_IN = 0.18f;
    private static final float FADE_OUT = 0.10f;
    private static final float TINT_UP = 0.35f, TINT_DOWN = 0.15f;

    private static float blend, prevBlend;
    private static float fill, prevFill;
    private static float white, prevWhite;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        prevBlend = blend;
        prevFill = fill;
        prevWhite = white;
        blend = Mth.clamp(blend + (ChargerController.isActive() ? FADE_IN : -FADE_OUT), 0f, 1f);
        fill = Mth.clamp(ChargerController.stamina / Math.max(1f, ChargerController.maxStamina), 0f, 1f);
        white = Mth.clamp(white + (ChargerController.charging ? TINT_UP : -TINT_DOWN), 0f, 1f);
    }

    @SubscribeEvent
    public static void onLayer(RenderGuiLayerEvent.Pre event) {
        ResourceLocation name = event.getName();
        boolean bar = name.equals(VanillaGuiLayers.EXPERIENCE_BAR);
        boolean level = name.equals(VanillaGuiLayers.EXPERIENCE_LEVEL);
        if (!bar && !level) return;

        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float b = Mth.lerp(pt, prevBlend, blend);
        if (blend <= 0f && b <= 0.001f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        event.setCanceled(true);

        GuiGraphics g = event.getGuiGraphics();
        boolean xpShown = mc.gameMode.hasExperience();

        if (level) {
            int alpha = (int) ((1f - b) * 255f);
            if (xpShown && mc.player.experienceLevel > 0 && alpha > 3) {
                String s = String.valueOf(mc.player.experienceLevel);
                int x = (g.guiWidth() - mc.font.width(s)) / 2;
                int y = g.guiHeight() - 31 - 4;
                int shade = alpha << 24;
                g.drawString(mc.font, s, x + 1, y, shade, false);
                g.drawString(mc.font, s, x - 1, y, shade, false);
                g.drawString(mc.font, s, x, y + 1, shade, false);
                g.drawString(mc.font, s, x, y - 1, shade, false);
                g.drawString(mc.font, s, x, y, shade | 0x80FF20, false);
            }
            return;
        }

        int x = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - 32 + 3;
        g.blitSprite(BG, 182, 5, 0, 0, x, y, 182, 5);

        // XP fill fading out
        if (xpShown) {
            int w = (int) (mc.player.experienceProgress * 183.0f);
            float a = 1f - b;
            if (w > 0 && a > 0.01f) {
                g.setColor(1f, 1f, 1f, a);
                g.blitSprite(XP, 182, 5, 0, 0, x, y, w, 5);
                g.setColor(1f, 1f, 1f, 1f);
            }
        }

        // Stamina fills fading in; white while using class, easing to yellow while recoverings
        int sw = (int) (Mth.lerp(pt, prevFill, fill) * 182f);
        if (sw > 0 && b > 0.01f) {
            float t = Mth.lerp(pt, prevWhite, white);
            g.setColor(1f, Mth.lerp(t, 0.85f, 1f), Mth.lerp(t, 0.1f, 1f), b);
            g.blit(STAMINA, x, y, 0, 0, sw, 5, 182, 5);
            g.setColor(1f, 1f, 1f, 1f);
        }
    }

    private StaminaBarOverlay() {}
}
