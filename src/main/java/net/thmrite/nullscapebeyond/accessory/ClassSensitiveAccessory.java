package net.thmrite.nullscapebeyond.accessory;

import org.jetbrains.annotations.Nullable;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.entity.player.Player;
import net.thmrite.nullscapebeyond.registry.ModItems;

public class ClassSensitiveAccessory extends NormalAccessory {
    public ClassSensitiveAccessory(Properties properties, String slot, String model) {
        super(properties, slot, model);
    }

    @Nullable
    @Override
    public String descriptionVariant(@Nullable Player viewer) {
        if (viewer == null) return null;
        AccessoriesCapability cap = AccessoriesCapability.get(viewer);
        if (cap == null) return null;
        for (ClassAccessory classAccessory : ModItems.classAccessories()) {
            if (cap.isEquipped(classAccessory)) return classAccessory.classId();
        }
        return null;
    }
}
