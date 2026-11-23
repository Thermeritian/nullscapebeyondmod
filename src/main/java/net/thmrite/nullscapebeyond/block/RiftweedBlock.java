package net.thmrite.nullscapebeyond.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.thmrite.nullscapebeyond.fluid.SeaFloraFluid;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

public class RiftweedBlock extends KelpBlock {
    public RiftweedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return SeaFloraFluid.isSource(fluid) ? super.getStateForPlacement(context) : null;
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.is(SeaFloraFluid.liquidBlock());
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.RIFTWEED_PLANT.get();
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
