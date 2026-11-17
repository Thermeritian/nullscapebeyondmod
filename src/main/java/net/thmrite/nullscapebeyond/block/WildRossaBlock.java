package net.thmrite.nullscapebeyond.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thmrite.nullscapebeyond.registry.ModItems;

/**
 * Two-stage crop.
 * age 0: single "young" block (half = lower).
 * age 1: two-block "mature" plant (lower + upper). Right-clicking the UPPER block harvests rossa_kale and the
 * plant goes back to young. Breaking the upper block also sends it back to young (the loot table gives the kale).
 */
public class WildRossaBlock extends BushBlock {
    public static final MapCodec<WildRossaBlock> CODEC = simpleCodec(WildRossaBlock::new);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 1);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape YOUNG = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape MATURE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public WildRossaBlock(Properties properties) {
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
        return state.getValue(AGE) == 0 ? YOUNG : MATURE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER && below.getValue(AGE) == 1;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return dir == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
        }
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        // mature lower block lost its upper half (harvested / broken): back to young
        if (dir == Direction.UP && state.getValue(AGE) == 1
                && !(neighbor.is(this) && neighbor.getValue(HALF) == DoubleBlockHalf.UPPER)) {
            return state.setValue(AGE, 0);
        }
        return state;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(AGE) == 0;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) != 0 || level.getMaxLocalRawBrightness(pos.above()) < 9 || random.nextInt(5) != 0) return;
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir()) return;
        level.setBlock(pos, state.setValue(AGE, 1), 2);
        level.setBlock(above, state.setValue(AGE, 1).setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    /** Right-click the top block to harvest. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 1 || state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(ModItems.ROSSA_KALE.get(), 1 + level.random.nextInt(2)));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            BlockPos lower = pos.below();
            BlockState young = level.getBlockState(lower).setValue(AGE, 0);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(lower, young, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, lower, GameEvent.Context.of(player, young));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }
}
