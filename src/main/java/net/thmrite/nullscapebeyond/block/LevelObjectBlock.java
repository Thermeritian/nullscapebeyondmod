package net.thmrite.nullscapebeyond.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.thmrite.nullscapebeyond.block.entity.LevelObjectBlockEntity;
import net.thmrite.nullscapebeyond.block.entity.LevelObjectBlockEntity.Phase;
import net.thmrite.nullscapebeyond.event.LevelObjectCollectedEvent;
import net.thmrite.nullscapebeyond.registry.ModBlockSounds;

import java.util.ArrayDeque;

public class LevelObjectBlock extends Block implements EntityBlock {
    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);
    public static final BooleanProperty COLLECTED = BooleanProperty.create("collected");

    public static final int ARM_TICKS = 30;                  // 1.5 s
    public static final int EXPLOSION_TICKS = 16;            // 16 frames, 1 tick each
    public static final double EXPLOSION_RADIUS = 4.0D;
    public static final float EXPLOSION_DAMAGE = 50.0F;
    public static final boolean EXPLOSION_COLLECTS_GOLDEN = true;

    public LevelObjectBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MODE, Mode.NONE).setValue(COLLECTED, false));
    }

    public static int lightFor(BlockState state) {
        return state.getValue(COLLECTED) ? 0 : state.getValue(MODE).light;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE, COLLECTED);
    }

    // ---- shape / rendering ----

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(MODE).shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE; // drawn by the block entity renderer
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LevelObjectBlockEntity(pos, state);
    }

    // ---- touch detection ----

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isSpectator() || state.getValue(COLLECTED)) return;
        Mode mode = state.getValue(MODE);
        if (mode == Mode.NONE) return;

        // Vanilla calls this for the whole cell, so test the real (smaller) hitbox ourselves.
        AABB hitbox = state.getShape(level, pos).bounds().move(pos);
        if (!entity.getBoundingBox().intersects(hitbox)) return;

        switch (mode) {
            case GIFT, GOLDEN_GIFT -> {
                if (entity instanceof ServerPlayer player) {
                    collect((ServerLevel) level, pos, state, player);
                }
            }
            case TRIPMINE -> arm(level, pos);
            default -> {}
        }
    }

    private void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable ServerPlayer player) {
        Mode mode = state.getValue(MODE);
        boolean golden = mode == Mode.GOLDEN_GIFT;
        level.playSound(null, pos,
                (golden ? ModBlockSounds.GOLDEN_GIFT_COLLECT : ModBlockSounds.GIFT_COLLECT).get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        NeoForge.EVENT_BUS.post(new LevelObjectCollectedEvent(level, pos.immutable(), mode, player));
        if (golden) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(COLLECTED, true), Block.UPDATE_ALL);
        }
    }

    // ---- tripmine ----

    private void arm(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof LevelObjectBlockEntity be) || be.getPhase() != Phase.IDLE) return;
        be.setPhase(Phase.ARMED, level.getGameTime());
        level.playSound(null, pos, ModBlockSounds.TRIPMINE_ACTIVATE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.scheduleTick(pos, this, ARM_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(MODE) != Mode.TRIPMINE
                || !(level.getBlockEntity(pos) instanceof LevelObjectBlockEntity be)) return;
        switch (be.getPhase()) {
            case ARMED -> detonate(level, pos);
            case EXPLODING -> {
                long elapsed = level.getGameTime() - be.getPhaseStart();
                if (elapsed >= EXPLOSION_TICKS) {
                    level.removeBlock(pos, false);
                } else {
                    // Leftover fuse tick from a mine that was chain-detonated early: wait for the visual to finish.
                    level.scheduleTick(pos, this, (int) (EXPLOSION_TICKS - elapsed));
                }
            }
            default -> {}
        }
    }

    private void detonate(ServerLevel level, BlockPos start) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof LevelObjectBlock)
                    || state.getValue(MODE) != Mode.TRIPMINE
                    || !(level.getBlockEntity(pos) instanceof LevelObjectBlockEntity be)
                    || be.getPhase() == Phase.EXPLODING) continue;   // already blown up
            explode(level, pos, be, queue);
        }
    }

    private void explode(ServerLevel level, BlockPos pos, LevelObjectBlockEntity be, ArrayDeque<BlockPos> chain) {
        be.setPhase(Phase.EXPLODING, level.getGameTime());
        level.scheduleTick(pos, this, EXPLOSION_TICKS);
        level.playSound(null, pos, ModBlockSounds.TRIPMINE_DETONATE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        Vec3 center = Vec3.atCenterOf(pos);
        double r2 = EXPLOSION_RADIUS * EXPLOSION_RADIUS;

        // Massive damage, no block damage.
        DamageSource source = level.damageSources().explosion(null, null);
        for (Entity e : level.getEntities((Entity) null, new AABB(pos).inflate(EXPLOSION_RADIUS),
                e -> !e.isSpectator())) {
            if (e.distanceToSqr(center) <= r2) {
                e.hurt(source, EXPLOSION_DAMAGE);
            }
        }

        // Collect nearby gifts and queue nearby tripmines.
        int r = (int) Math.ceil(EXPLOSION_RADIUS);
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r))) {
            if (p.equals(pos) || Vec3.atCenterOf(p).distanceToSqr(center) > r2) continue;
            BlockState s = level.getBlockState(p);
            if (!(s.getBlock() instanceof LevelObjectBlock) || s.getValue(COLLECTED)) continue;
            Mode m = s.getValue(MODE);
            if (m == Mode.TRIPMINE) {
                chain.add(p.immutable());
            } else if (m == Mode.GIFT || (m == Mode.GOLDEN_GIFT && EXPLOSION_COLLECTS_GOLDEN)) {
                collect(level, p.immutable(), s, null);
            }
        }
    }

    public enum Mode implements StringRepresentable {
        NONE("none", 0, Block.box(4, 4, 4, 12, 12, 12)),
        GIFT("gift", 12, Block.box(3, 3, 3, 13, 13, 13)),
        GOLDEN_GIFT("golden_gift", 15, Block.box(3, 3, 3, 13, 13, 13)),
        TRIPMINE("tripmine", 8, Block.box(6, 6, 6, 10, 10, 10));

        public final int light;
        public final VoxelShape shape;
        private final String name;

        Mode(String name, int light, VoxelShape shape) {
            this.name = name;
            this.light = light;
            this.shape = shape;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}