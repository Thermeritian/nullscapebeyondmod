package net.thmrite.nullscapebeyond.block;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class StrippableLogBlock extends RotatedPillarBlock {
    private final Supplier<? extends Block> stripped;

    public StrippableLogBlock(Properties properties, Supplier<? extends Block> stripped) {
        super(properties);
        this.stripped = stripped;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
        if (ability == ItemAbilities.AXE_STRIP) {
            return stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        }
        return super.getToolModifiedState(state, context, ability, simulate);
    }
}
