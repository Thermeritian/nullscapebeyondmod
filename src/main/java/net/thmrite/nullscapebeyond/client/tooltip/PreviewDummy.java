package net.thmrite.nullscapebeyond.client.tooltip;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.ExtraSlotSync;

final class PreviewDummy {
    static final boolean USE_DUMMY = true;

    private static RemotePlayer dummy;
    private static ClientLevel dummyLevel;
    private static Item equipped;

    static LivingEntity get(ItemStack stack, String slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;
        if (!USE_DUMMY) return mc.player;

        if (dummy == null || dummyLevel != mc.level) {
            dummyLevel = mc.level;
            dummy = new RemotePlayer(mc.level, mc.player.getGameProfile());
            equipped = null;
        }
        if (equipped != stack.getItem()) {
            equip(dummy, stack, slot);
            equipped = stack.getItem();
        }
        return dummy;
    }

    static void tick() {
        if (dummy != null) dummy.tickCount++;
    }

    private static void equip(RemotePlayer d, ItemStack stack, String slot) {
        try {
            AccessoriesCapability cap = AccessoriesCapability.get(d);
            if (cap == null) return;
            if (slot.equals("extra")) cap.addTransientSlotModifiers(ExtraSlotSync.modifier(1));
            cap.getContainers().values().forEach(c -> c.getAccessories().clearContent());
            var container = cap.getContainers().get(slot);
            if (container != null && container.getSize() > 0) {
                container.getAccessories().setItem(0, stack.copy());
            }
        } catch (RuntimeException e) {
            NullscapeBeyond.LOGGER.warn("Could not equip tooltip preview dummy", e);
        }
    }

    private PreviewDummy() {}
}
