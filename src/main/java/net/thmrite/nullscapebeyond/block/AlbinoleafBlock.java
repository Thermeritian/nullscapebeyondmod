package net.thmrite.nullscapebeyond.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thmrite.nullscapebeyond.fluid.SeaFloraFluid;

public class AlbinoleafBlock extends WaterlilyBlock {
    public static final EnumProperty<Quadrant> QUADRANT = EnumProperty.create("quadrant", Quadrant.class);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);

    public enum Quadrant implements StringRepresentable {
        NW("nw", 0, 0), NE("ne", 1, 0), SW("sw", 0, 1), SE("se", 1, 1);

        public final String id;
        public final int dx;
        public final int dz;

        Quadrant(String id, int dx, int dz) {
            this.id = id;
            this.dx = dx;
            this.dz = dz;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    public AlbinoleafBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(QUADRANT, Quadrant.NW));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(QUADRANT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        FluidState below = level.getFluidState(pos);
        FluidState above = level.getFluidState(pos.above());
        return below.getType() == SeaFloraFluid.source() && above.isEmpty();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos origin = context.getClickedPos();
        for (Quadrant q : Quadrant.values()) {
            BlockPos p = origin.offset(q.dx, 0, q.dz);
            if (!context.getLevel().getBlockState(p).canBeReplaced(context)
                    || !canSurvive(defaultBlockState().setValue(QUADRANT, q), context.getLevel(), p)) {
                return null;
            }
        }
        return defaultBlockState().setValue(QUADRANT, Quadrant.NW);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        for (Quadrant q : Quadrant.values()) {
            if (q == Quadrant.NW) continue;
            // UPDATE_KNOWN_SHAPE: don't run updateShape while the pad is still incomplete
            level.setBlock(pos.offset(q.dx, 0, q.dz), defaultBlockState().setValue(QUADRANT, q),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Quadrant self = state.getValue(QUADRANT);
        BlockPos origin = pos.offset(-self.dx, 0, -self.dz);
        for (Quadrant q : Quadrant.values()) {
            BlockState s = level.getBlockState(origin.offset(q.dx, 0, q.dz));
            if (!s.is(this) || s.getValue(QUADRANT) != q) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }
}
