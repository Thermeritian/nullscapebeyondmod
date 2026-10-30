package net.thmrite.nullscapebeyond.accessory;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.world.entity.player.Player;

public interface TooltipAccessory {
    //Slot Name
    String tooltipSlot();

    //RGB Colour
    int tooltipColor();

    //Is Normal Accessory or CLass
    default boolean hasFlavorText() {
        return true;
    }

    //Get Description based on Class Accessory
    @Nullable
    default String descriptionVariant(@Nullable Player viewer) {
        return null;
    }
}
