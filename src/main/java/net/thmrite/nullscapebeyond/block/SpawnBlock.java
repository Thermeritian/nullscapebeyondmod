package net.thmrite.nullscapebeyond.block;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class SpawnBlock extends FootstepBlock {
    public static final EnumProperty<Pattern> PATTERN = EnumProperty.create("pattern", Pattern.class);

    public SpawnBlock(Properties properties, FootstepSounds sounds) {
        super(properties, sounds);
        registerDefaultState(stateDefinition.any().setValue(PATTERN, Pattern.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PATTERN);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this)) {
            refreshAround(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && !newState.is(this)) {
            refreshAround(level, pos);
        }
    }

    /**
     * Any Spawn block whose group could have been affected by a change at {@code center}
     * is at most 3 blocks away on the same Y level, so we re-evaluate that 7x7 area.
     */
    private static void refreshAround(Level level, BlockPos center) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                BlockState s = level.getBlockState(p);
                if (s.getBlock() instanceof SpawnBlock) {
                    Pattern wanted = computePattern(level, p);
                    if (s.getValue(PATTERN) != wanted) {
                        level.setBlock(p, s.setValue(PATTERN, wanted), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** The symbol forms only if this block's connected group is exactly a filled 3x3 square. */
    private static Pattern computePattern(LevelAccessor level, BlockPos origin) {
        Set<BlockPos> group = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        group.add(origin);
        queue.add(origin);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(dir);
                if (!group.contains(next) && level.getBlockState(next).getBlock() instanceof SpawnBlock) {
                    if (group.size() >= 9) {
                        return Pattern.DEFAULT; // already 9 blocks plus another one: too big
                    }
                    group.add(next);
                    queue.add(next);
                }
            }
        }
        if (group.size() != 9) {
            return Pattern.DEFAULT;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : group) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }
        if (maxX - minX != 2 || maxZ - minZ != 2) {
            return Pattern.DEFAULT; // 9 blocks but not a square (e.g. a 9x1 line)
        }
        // Row = north -> south, column = west -> east (matches how a top face is textured).
        return Pattern.of(origin.getZ() - minZ, origin.getX() - minX);
    }

    //3x3 matrix for the spawn block
    public enum Pattern implements StringRepresentable {
        DEFAULT("default"),
        C00("00"), C01("01"), C02("02"),
        C10("10"), C11("11"), C12("12"),
        C20("20"), C21("21"), C22("22");

        private final String name;

        Pattern(String name) {
            this.name = name;
        }

        public static Pattern of(int row, int col) {
            return values()[1 + row * 3 + col];
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}