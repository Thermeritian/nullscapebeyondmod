package net.thmrite.nullscapebeyond.accessory;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoryItem;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.item.ItemStack;

import javax.tools.Tool;

public abstract class ClassAccessory extends AccessoryItem implements TooltipAccessory {
    private final String slot;

    protected ClassAccessory(Properties properties, String slot) {
        super(properties);
        this.slot = slot;
    }

    @Override
    public String tooltipSlot() {
        return slot;
    }

    @Override
    public int tooltipColor() {
        return 0xFFFF55; // Class accessories: yellow
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference reference) {
        AccessoriesCapability cap = AccessoriesCapability.get(reference.entity());
        if (cap == null) return true;
        // Accessories re-validates canEquip on load/reload, so a second class gets bounced out.
        return !cap.isAnotherEquipped(stack, reference, other -> other.getItem() instanceof ClassAccessory);
    }
}
