package net.thmrite.nullscapebeyond.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.thmrite.nullscapebeyond.fluid.SeaFloraFluid;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

public class NilreedBlock extends SeagrassBlock {
    public NilreedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return SeaFloraFluid.isSource(fluid) ? this.defaultBlockState() : null;
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

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState lower = ModBlocks.TALL_NILREED.get().defaultBlockState();
        BlockState upper = lower.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
        BlockPos above = pos.above();
        if (SeaFloraFluid.isSourceBlock(level, above)) {
            level.setBlock(pos, lower, 2);
            level.setBlock(above, upper, 2);
        }
    }
}
