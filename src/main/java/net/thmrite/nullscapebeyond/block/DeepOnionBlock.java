package net.thmrite.nullscapebeyond.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thmrite.nullscapebeyond.fluid.SeaFloraFluid;

public class DeepOnionBlock extends BushBlock implements LiquidBlockContainer {
    public static final MapCodec<DeepOnionBlock> CODEC = simpleCodec(DeepOnionBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape[] SHAPES = {
            Block.box(4.0, 0.0, 4.0, 12.0, 6.0, 12.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 9.0, 13.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
    };

    public DeepOnionBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER && below.getValue(AGE) == 3;
        }
        return super.canSurvive(state, level, pos);
    }

    @Nullable
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
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return dir == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
        }
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        SeaFloraFluid.scheduleTick(level, pos);
        // mature lower block lost its upper half (harvested / broken): back to stage 1
        if (dir == Direction.UP && state.getValue(AGE) == 3
                && !(neighbor.is(this) && neighbor.getValue(HALF) == DoubleBlockHalf.UPPER)) {
            return state.setValue(AGE, 1);
        }
        return state;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) return;
        int age = state.getValue(AGE);
        if (age < 2) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        } else if (age == 2) {
            BlockPos above = pos.above();
            if (SeaFloraFluid.isSourceBlock(level, above)) {          // needs a free block of water above
                level.setBlock(pos, state.setValue(AGE, 3), 2);
                level.setBlock(above, state.setValue(AGE, 3).setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 3) return InteractionResult.PASS;
        if (!level.isClientSide) {
            BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            Block.popResource(level, lowerPos.above(), new ItemStack(this.asItem(), 4 + level.random.nextInt(3)));
            level.playSound(null, lowerPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            BlockState young = level.getBlockState(lowerPos).setValue(AGE, 1);
            level.setBlock(lowerPos.above(), SeaFloraFluid.sourceState().createLegacyBlock(), 3);
            level.setBlock(lowerPos, young, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, lowerPos, GameEvent.Context.of(player, young));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this.asItem());
    }
}
