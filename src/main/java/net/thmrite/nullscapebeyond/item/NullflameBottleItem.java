package net.thmrite.nullscapebeyond.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Bottle of Nullflame: a material, and the fuel that gives a furnace / blast furnace / smoker "nullflame combustion"
 * (see furnace/NullflameCombustion). Burning the last one of a stack leaves an empty glass bottle.
 */
public class NullflameBottleItem extends Item {
    /** Furnace burn time in ticks (coal is 1600, a blaze rod 2400). */
    public static final int BURN_TIME = 2400;

    public NullflameBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(Items.GLASS_BOTTLE);
    }
}
