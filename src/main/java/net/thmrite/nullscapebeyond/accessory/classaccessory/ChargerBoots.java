package net.thmrite.nullscapebeyond.accessory.classaccessory;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.entity.LivingEntity;
import net.thmrite.nullscapebeyond.accessory.ClassAccessory;
import net.thmrite.nullscapebeyond.registry.ModItems;

public class ChargerBoots extends ClassAccessory {
    public ChargerBoots(Properties properties) {
        super(properties, "feet", "charger_boots", "charger");
    }

    @Override
    protected String idleAnimation() {
        return null; // no animations yet
    }

    public static boolean isEquipped(LivingEntity entity) {
        AccessoriesCapability cap = AccessoriesCapability.get(entity);
        return cap != null && cap.isEquipped(ModItems.CHARGER_BOOTS.get());
    }
}
