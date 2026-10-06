package net.thmrite.nullscapebeyond.client.tooltip;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thmrite.nullscapebeyond.accessory.TooltipAccessory;
import net.thmrite.nullscapebeyond.client.ModKeybinds;
import net.thmrite.nullscapebeyond.registry.AccessoryTooltipData;

/**
 * Layout (top to bottom):
 *   NAME (accent colour)  flavour (small, gray, italic)
 *   Slot: X
 *   ------------------------------------------------
 *   [viewport]  description (wrapped)
 *   FOOTER (mod name, italic blue)
 */
public final class AccessoryTooltipComponent implements ClientTooltipComponent {
    private static final int PREVIEW_W = 44, PREVIEW_H = 64, GAP = 6, TEXT_W = 140;
    private static final float FLAVOR_SCALE = 0.75f;

    private final ItemStack stack;
    private final TooltipAccessory accessory;
    private final Component name, flavor, slotLine, footer;
    private final List<FormattedCharSequence> description;
    private final int lh;
    private final int width, height, bodyHeight;

    public AccessoryTooltipComponent(AccessoryTooltipData data) {
        this.stack = data.stack();
        this.accessory = (TooltipAccessory) stack.getItem();
        Font font = Minecraft.getInstance().font;
        this.lh = font.lineHeight;

        String id = stack.getItem().getDescriptionId(stack);
        this.name = stack.getHoverName().copy().withStyle(s -> s.withColor(accessory.tooltipColor()).withItalic(false));
        this.flavor = Component.translatable(id + ".flavor").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        this.slotLine = Component.translatable("tooltip.nscapebeyond.slot",
                Component.translatable("tooltip.nscapebeyond.slot." + accessory.tooltipSlot()));
        this.footer = Component.translatable("tooltip.nscapebeyond.footer")
                .withStyle(Style.EMPTY.withItalic(true).withColor(0x5B5BFF));

        Component ability = ModKeybinds.ABILITY.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.YELLOW);
        Component alt = ModKeybinds.ALT_ABILITY.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.YELLOW);
        this.description = font.split(Component.translatable(id + ".desc", ability, alt), TEXT_W);

        int descW = description.stream().mapToInt(font::width).max().orElse(0);
        int headerW = Math.max(font.width(name) + 6 + (int) (font.width(flavor) * FLAVOR_SCALE), font.width(slotLine));
        this.width = Math.max(Math.max(headerW, PREVIEW_W + GAP + descW), font.width(footer));
        this.bodyHeight = Math.max(PREVIEW_H, description.size() * lh);
        this.height = lh + 3 + lh + 4 + 1 + 5 + bodyHeight + 5 + lh;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth(Font font) {
        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics g) {
        // Header
        g.drawString(font, name, x, y, 0xFFFFFFFF, true);
        g.pose().pushPose();
        g.pose().translate(x + font.width(name) + 6, y + 2.5f, 0);
        g.pose().scale(FLAVOR_SCALE, FLAVOR_SCALE, 1f);
        g.drawString(font, flavor, 0, 0, 0xFFAAAAAA, true);
        g.pose().popPose();

        int slotY = y + lh + 3;
        g.drawString(font, slotLine, x, slotY, 0xFFFFFFFF, true);

        // Separator
        int sepY = slotY + lh + 4;
        g.fill(x, sepY, x + width, sepY + 1, 0xFFFFFFFF);

        // Body: viewport + description
        int by = sepY + 1 + 5;
        LivingEntity preview = PreviewDummy.get(stack, accessory.tooltipSlot());
        if (preview != null) {
            // Same trick as the inventory screen: the dummy looks at the real cursor (GUI-scaled coordinates).
            Minecraft mc = Minecraft.getInstance();
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x, by, x + PREVIEW_W, by + PREVIEW_H,
                    28, 0.0625f, (float) mouseX, (float) mouseY, preview);
        }
        int tx = x + PREVIEW_W + GAP;
        for (int i = 0; i < description.size(); i++) {
            g.drawString(font, description.get(i), tx, by + i * lh, 0xFFFFFFFF, true);
        }

        // Footer
        g.drawString(font, footer, x, by + bodyHeight + 5, 0xFFFFFFFF, true);
    }
}
