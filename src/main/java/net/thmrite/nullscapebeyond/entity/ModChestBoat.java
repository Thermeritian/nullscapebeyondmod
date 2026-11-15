package net.thmrite.nullscapebeyond.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.thmrite.nullscapebeyond.registry.ModBoats;

public class ModChestBoat extends ChestBoat {
    public ModChestBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDropItem() {
        return ModBoats.dropItemFor(getType());
    }
}
