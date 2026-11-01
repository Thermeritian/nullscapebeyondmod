package net.thmrite.nullscapebeyond.accessory.classaccessory;

import net.thmrite.nullscapebeyond.accessory.ClassAccessory;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.entity.LivingEntity;
import net.thmrite.nullscapebeyond.registry.ModItems;

/** Class accessory: Diver. Mechanics live in client/DiverController. */
public class DiverHelmet extends ClassAccessory {
    public DiverHelmet(Properties properties) {
        super(properties, "head", "diver_helmet", "diver");
    }

    public static boolean isEquipped(LivingEntity entity) {
        AccessoriesCapability cap = AccessoriesCapability.get(entity);
        return cap != null && cap.isEquipped(ModItems.DIVER_HELMET.get());
    }
}
