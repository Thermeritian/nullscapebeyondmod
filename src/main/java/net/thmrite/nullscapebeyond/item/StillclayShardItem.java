package net.thmrite.nullscapebeyond.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.thmrite.nullscapebeyond.event.blockentity.NullflameFuel;

public class StillclayShardItem extends Item implements NullflameFuel {
    public static final int BURN_TIME = 90 * 20;

    public StillclayShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }
}
