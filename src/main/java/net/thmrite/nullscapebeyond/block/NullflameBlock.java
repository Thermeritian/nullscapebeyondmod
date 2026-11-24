package net.thmrite.nullscapebeyond.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Nullflame: a fire variant in the spirit of soul fire. It never spreads, never burns blocks and never burns out
 * by itself. It sits on any block with a solid top face. Unique features can be added here later.
 */
public class NullflameBlock extends BaseFireBlock {
    public static final MapCodec<NullflameBlock> CODEC = simpleCodec(NullflameBlock::new);

    public NullflameBlock(Properties properties) {
        super(properties, 2.0F);   // damage to entities standing in it (soul fire: 2)
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return this.canSurvive(state, level, pos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return false;
    }
}
