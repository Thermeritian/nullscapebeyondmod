package net.thmrite.nullscapebeyond.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

public class NullGrassBlock extends GrassBlock {
    public NullGrassBlock(Properties properties) {
        super(properties);
    }

    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) return true;
        if (aboveState.getFluidState().getAmount() == 8) return false;
        int opacity = LightEngine.getLightBlockInto(level, state, pos, aboveState, above, Direction.UP, aboveState.getLightBlock(level, above));
        return opacity < level.getMaxLightLevel();
    }

    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        return canBeGrass(state, level, pos) && !level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            if (!level.isAreaLoaded(pos, 1)) return;
            level.setBlockAndUpdate(pos, ModBlocks.NULLSOIL.get().defaultBlockState());
        } else if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            BlockState me = this.defaultBlockState();
            for (int i = 0; i < 4; i++) {
                BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (level.getBlockState(target).is(ModBlocks.NULLSOIL.get()) && canPropagate(me, level, target)) {
                    level.setBlockAndUpdate(target, me.setValue(SNOWY, level.getBlockState(target.above()).is(Blocks.SNOW)));
                }
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }
}
