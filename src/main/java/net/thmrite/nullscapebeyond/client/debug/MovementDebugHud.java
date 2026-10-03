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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.movement.AirControlController;
import net.thmrite.nullscapebeyond.client.movement.GroundControlController;
import net.thmrite.nullscapebeyond.client.movement.MovementHandler;
import net.thmrite.nullscapebeyond.client.movement.MovementState;
import net.thmrite.nullscapebeyond.movement.ModAttributes;

/**
 * Right-aligned panel with live movement values, grouped in sections.
 * Toggle with /nsb movement. To add a value, add a Row to the matching section in buildSections().
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class MovementDebugHud implements LayeredDraw.Layer {
    // ARGB colors. Text colors are opaque; the panel is a translucent dark box.
    private static final int PANEL_BG = 0xB0101018;
    private static final int PANEL_BORDER = 0xB05A5A72;
    private static final int TITLE = 0xFFD866;
    private static final int TITLE_LINE = 0x80FFD866;
    private static final int LABEL = 0xAAAAAA;
    private static final int VALUE = 0xFFFFFF;
    private static final int GOOD = 0x6FE28A;
    private static final int BAD = 0xFF6B6B;

    private static final int PADDING = 5;
    private static final int COLUMN_GAP = 14;
    private static final int SECTION_GAP = 5;
    private static final int MARGIN = 4;

    private record Row(String label, String value, int color) {
        Row(String label, String value) {
            this(label, value, VALUE);
        }
    }

    private record Section(String title, List<Row> rows) {}

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
        List<Section> sections = buildSections(mc.player);

        int rowHeight = font.lineHeight + 1;
        int titleHeight = font.lineHeight + 3; // text + divider line + a little air

        // Measure.
        int contentWidth = 0;
        int contentHeight = 0;
        for (Section section : sections) {
            contentWidth = Math.max(contentWidth, font.width(section.title()));
            for (Row row : section.rows()) {
                contentWidth = Math.max(contentWidth, font.width(row.label()) + COLUMN_GAP + font.width(row.value()));
            }
            contentHeight += titleHeight + section.rows().size() * rowHeight;
        }
        contentHeight += SECTION_GAP * (sections.size() - 1);

        int x2 = graphics.guiWidth() - MARGIN;
        int x1 = x2 - contentWidth - PADDING * 2;
        int y1 = MARGIN;
        int y2 = y1 + contentHeight + PADDING * 2;

        // Background + 1px border.
        graphics.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, PANEL_BORDER);
        graphics.fill(x1, y1, x2, y2, PANEL_BG);

        // Content.
        int left = x1 + PADDING;
        int right = x2 - PADDING;
        int y = y1 + PADDING;
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);

            graphics.drawString(font, section.title(), left, y, TITLE, true);
            graphics.fill(left, y + font.lineHeight, right, y + font.lineHeight + 1, TITLE_LINE);
            y += titleHeight;

            for (Row row : section.rows()) {
                graphics.drawString(font, row.label(), left, y, LABEL, true);
                graphics.drawString(font, row.value(), right - font.width(row.value()), y, row.color(), true);
                y += rowHeight;
            }
            if (i < sections.size() - 1) y += SECTION_GAP;
        }
    }

    private static List<Section> buildSections(LocalPlayer p) {
        MovementState s = MovementHandler.STATE;
        List<Section> sections = new ArrayList<>();

        Vec3 v = p.getDeltaMovement();
        double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);

        sections.add(new Section("VELOCITY", List.of(
                new Row("Velocity (b/t)", fmt("%.3f  %.3f  %.3f", v.x, v.y, v.z)),
                new Row("Horizontal", fmt("%.3f b/t  (%.2f b/s)", horizontal, horizontal * 20.0)),
                new Row("Vertical", fmt("%.2f b/s", v.y * 20.0))
        )));

        sections.add(new Section("STATE", List.of(
                new Row("Grounded", s.grounded ? "Yes" : "No", s.grounded ? GOOD : BAD),
                new Row("Air ticks", String.valueOf(s.ticksSinceGrounded)),
                new Row("Air jumps used", String.valueOf(s.airJumpsUsed))
                //new Row("Dash cooldown", s.dashCooldownTicks + " ticks")
        )));

        sections.add(new Section("GROUND", List.of(
                new Row("Max speed", fmt("%.2f b/s", p.getAttributeValue(ModAttributes.GROUND_MAX_SPEED))),
                new Row("Run cap now", fmt("%.2f b/s", GroundControlController.effectiveMaxSpeedPerTick(p) * 20.0)),
                new Row("Acceleration", fmt("x%.2f", p.getAttributeValue(ModAttributes.GROUND_ACCELERATION))),
                new Row("Turn speed", fmt("x%.2f", p.getAttributeValue(ModAttributes.GROUND_TURN_SPEED)))
        )));

        float baseAccel = AirControlController.baseAirAcceleration(p);
        double airControl = p.getAttributeValue(ModAttributes.AIR_CONTROL);
        sections.add(new Section("AIR", List.of(
                new Row("Air control", fmt("x%.2f", airControl)),
                new Row("Air accel", fmt("%.4f b/t2", baseAccel * airControl))
        )));

        sections.add(new Section("ABILITIES", List.of(
                new Row("Max air jumps", String.valueOf((int) p.getAttributeValue(ModAttributes.MAX_AIR_JUMPS))),
                new Row("Air jump strength", fmt("%.3f", p.getAttributeValue(ModAttributes.AIR_JUMP_STRENGTH)))
        )));

        sections.add(new Section("VANILLA", List.of(
                new Row("Gravity", fmt("%.4f", p.getAttributeValue(Attributes.GRAVITY))),
                new Row("Jump strength", fmt("%.4f", p.getAttributeValue(Attributes.JUMP_STRENGTH))),
                new Row("Movement speed", fmt("%.4f", p.getAttributeValue(Attributes.MOVEMENT_SPEED))),
                new Row("Step height", fmt("%.2f", p.getAttributeValue(Attributes.STEP_HEIGHT)))
        )));

        sections.add(new Section("CLASS", List.of(
                //empty atm
        )));
        return sections;
    }

    /** Plain-text version of the panel, used by /nsb movement dump. */
    public static List<String> buildLines(LocalPlayer player) {
        List<String> lines = new ArrayList<>();
        for (Section section : buildSections(player)) {
            lines.add("[" + section.title() + "]");
            for (Row row : section.rows()) {
                lines.add("  " + row.label() + ": " + row.value());
            }
        }
        return lines;
    }

    private static String fmt(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
}