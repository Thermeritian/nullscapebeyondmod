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

/**
 * A throwaway copy of the local player (same skin, no armor) wearing ONLY the hovered accessory,
 * so the tooltip viewport always shows the accessory, whatever the real player has equipped.
 * Never added to the world.
 */
final class PreviewDummy {
    /** false = preview the real local player instead of a dummy (fallback if dummy equipping misbehaves). */
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

    /** The dummy is never ticked by the game, so advance its age ourselves; this drives the idle arm sway. */
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
