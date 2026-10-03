package net.thmrite.nullscapebeyond.client.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.movement.MovementHandler;
import net.thmrite.nullscapebeyond.client.movement.MovementState;
import net.thmrite.nullscapebeyond.movement.ModAttributes;

/** Right-aligned text overlay with live movement values. Toggle with /nsb movement. */
@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class MovementDebugHud implements LayeredDraw.Layer {
    private static boolean enabled = false;

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "movement_debug"),
                new MovementDebugHud());
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!enabled || mc.player == null || mc.options.hideGui) return;

        Font font = mc.font;
        int right = graphics.guiWidth() - 4;
        int y = 4;
        for (String line : buildLines(mc.player)) {
            graphics.drawString(font, line, right - font.width(line), y, 0xFFFFFF, true);
            y += font.lineHeight + 1;
        }
    }

    /** Shared by the HUD and /nsb movement dump. */
    public static List<String> buildLines(LocalPlayer player) {
        List<String> lines = new ArrayList<>();
        MovementState s = MovementHandler.STATE;

        Vec3 v = player.getDeltaMovement();
        double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);

        lines.add(fmt("Vel (b/t): %.3f  %.3f  %.3f", v.x, v.y, v.z));
        lines.add(fmt("H-speed: %.3f b/t  (%.2f b/s)", horizontal, horizontal * 20.0));
        lines.add(fmt("V-speed: %.2f b/s", v.y * 20.0));
        lines.add("Grounded: " + s.grounded + "  (air ticks: " + s.ticksSinceGrounded + ")");
        lines.add("Air jumps: " + s.airJumpsUsed + " / " + (int) player.getAttributeValue(ModAttributes.MAX_AIR_JUMPS));
        lines.add("Dash cooldown: " + s.dashCooldownTicks + " / " + (int) player.getAttributeValue(ModAttributes.DASH_COOLDOWN));

        lines.add("-- attributes --");
        attr(lines, player, "gravity", Attributes.GRAVITY);
        attr(lines, player, "jump_strength", Attributes.JUMP_STRENGTH);
        attr(lines, player, "movement_speed", Attributes.MOVEMENT_SPEED);
        attr(lines, player, "step_height", Attributes.STEP_HEIGHT);
        for (var holder : ModAttributes.ATTRIBUTES.getEntries()) {
            attr(lines, player, holder.getId().getPath(), holder);
        }
        return lines;
    }

    private static void attr(List<String> lines, LocalPlayer player, String name, Holder<Attribute> attribute) {
        lines.add(fmt("%s: %.4f", name, player.getAttributeValue(attribute)));
    }

    private static String fmt(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
}