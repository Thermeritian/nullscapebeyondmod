package net.thmrite.nullscapebeyond.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.thmrite.nullscapebeyond.fluid.SeaFloraFluid;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

public class TallNilreedBlock extends TallSeagrassBlock {
    public TallNilreedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.NILREED.get());
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return SeaFloraFluid.sourceState();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState result = super.updateShape(state, dir, neighbor, level, pos, neighborPos);
        if (!result.isAir()) {
            SeaFloraFluid.scheduleTick(level, pos);
        }
        return result;
    }
}
