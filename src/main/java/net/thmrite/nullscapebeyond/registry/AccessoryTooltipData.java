package net.thmrite.nullscapebeyond.registry;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record AccessoryTooltipData(ItemStack stack) implements TooltipComponent {}
