package net.thmrite.nullscapebeyond.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.thmrite.nullscapebeyond.registry.ModBoats;

/** Boat of a mod wood type. Rendering is done by ModBoatRenderer; the drop item comes from ModBoats. */
public class ModBoat extends Boat {
    public ModBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDropItem() {
        return ModBoats.dropItemFor(getType());
    }
}
