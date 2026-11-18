package net.thmrite.nullscapebeyond.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thmrite.nullscapebeyond.registry.ModBlocks;
import net.thmrite.nullscapebeyond.registry.ModItems;

public class ViridianResinHangingBlock extends Block {
    public static final MapCodec<ViridianResinHangingBlock> CODEC = simpleCodec(ViridianResinHangingBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final BooleanProperty RIPE = BooleanProperty.create("ripe");
    private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);

    public enum Part implements StringRepresentable {
        TOP("top"), MIDDLE("middle"), BOTTOM("bottom");

        private final String id;

        Part(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    public ViridianResinHangingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, Part.BOTTOM).setValue(RIPE, true));
    }

    @Override
    public MapCodec<ViridianResinHangingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, RIPE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static Part partFor(LevelReader level, BlockPos pos, Block self) {
        boolean below = level.getBlockState(pos.below()).is(self);
        boolean above = level.getBlockState(pos.above()).is(self);
        return !below ? Part.BOTTOM : (above ? Part.MIDDLE : Part.TOP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.is(ModBlocks.VIRIDIAN_LEAVES_RESIN.get()) || above.is(this);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(PART, partFor(context.getLevel(), context.getClickedPos(), this));
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return state.setValue(PART, partFor(level, pos, this));
    }

    /** Punch the ripe bottom to collect resin. */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide || state.getValue(PART) != Part.BOTTOM || !state.getValue(RIPE)) return;
        level.setBlock(pos, state.setValue(RIPE, false), 3);
        Block.popResource(level, pos, new ItemStack(ModItems.VIRIDIAN_RESIN.get()));
        level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 0.8F, 1.2F);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != Part.BOTTOM) return;
        if (!state.getValue(RIPE)) {
            if (random.nextInt(4) == 0) level.setBlock(pos, state.setValue(RIPE, true), 2);
            return;
        }
        if (random.nextInt(4) != 0) return;
        for (int i = 1; i <= 12; i++) {
            BlockPos p = pos.below(i);
            BlockState s = level.getBlockState(p);
            if (!s.isAir()) {
                BlockPos above = p.above();
                if (i >= 2 && s.isFaceSturdy(level, p, Direction.UP) && level.getBlockState(above).isAir()) {
                    level.setBlockAndUpdate(above, ModBlocks.VIRIDIAN_RESIN_DROPLETS.get().defaultBlockState());
                }
                break;
            }
        }
    }
}
