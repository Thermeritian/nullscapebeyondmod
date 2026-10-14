package net.thmrite.nullscapebeyond.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.hooks.BeaconClientHooks;
import net.thmrite.nullscapebeyond.registry.ModSounds;
import net.thmrite.nullscapebeyond.util.OggDuration;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BeaconEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> OPEN =
            SynchedEntityData.defineId(BeaconEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> OPENED_AT =
            SynchedEntityData.defineId(BeaconEntity.class, EntityDataSerializers.LONG);

    private static final RawAnimation OPEN_THEN_IDLE = RawAnimation.begin().thenPlay("Open").thenLoop("Idle");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("Idle");

    private static final String ENTER_SOUND_FILE =
            "/assets/" + NullscapeBeyond.MODID + "/sounds/entity/beacon/beacon_enter.ogg";
    private static int enterDelayTicks = -1;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private RawAnimation clientAnimation;   // client only
    private boolean idleSoundStarted;       // client only

    private static final int LIGHT_LEVEL = 15;     // vanilla maximum
    private static final int LIGHT_SPACING = 12;   // blocks between light sources
    private static final int MAX_LIGHTS = 100;     // safety cap for very large models

    private final List<BlockPos> lightPositions = new ArrayList<>();

    public BeaconEntity(EntityType<? extends BeaconEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // ---- state ----

    public boolean isOpen() {
        return entityData.get(OPEN);
    }

    /** Server only. Plays the spawn sound; the client picks Open/Idle animations from the synced state. */
    public boolean open() {
        if (level().isClientSide || isOpen()) return false;
        entityData.set(OPEN, true);
        entityData.set(OPENED_AT, level().getGameTime());
        placeLights();
        level().playSound(null, getX(), getY(), getZ(), ModSounds.BEACON_SPAWN.get(),
                SoundSource.AMBIENT, 1.0F, 1.0F);
        return true;
    }

    /** Fills the model's volume with invisible light blocks (only into air, only in loaded chunks). */
    private void placeLights() {
        if (level().isClientSide || !lightPositions.isEmpty()) return;
        BlockState light = Blocks.LIGHT.defaultBlockState().setValue(BlockStateProperties.LEVEL, LIGHT_LEVEL);
        BlockPos base = blockPosition();
        int n = (int) (BeaconGeometry.radius() / LIGHT_SPACING);

        for (int dx = -n; dx <= n; dx++) {
            for (int dz = -n; dz <= n; dz++) {
                for (int dy = (int) Math.floor(BeaconGeometry.minY()); dy <= BeaconGeometry.maxY(); dy += LIGHT_SPACING) {
                    if (lightPositions.size() >= MAX_LIGHTS) return;
                    BlockPos p = base.offset(dx * LIGHT_SPACING, dy, dz * LIGHT_SPACING);
                    if (!level().isInWorldBounds(p) || !level().hasChunkAt(p) || !level().getBlockState(p).isAir()) continue;
                    level().setBlock(p, light, Block.UPDATE_CLIENTS);
                    lightPositions.add(p);
                }
            }
        }
    }

    private void clearLights() {
        for (BlockPos p : lightPositions) {
            if (level().hasChunkAt(p) && level().getBlockState(p).is(Blocks.LIGHT)) {
                level().removeBlock(p, false);
            }
        }
        lightPositions.clear();
    }

    @Override
    public void remove(RemovalReason reason) {
        // Only clean up on real removal (delete/kill), not when the chunk just unloads.
        if (!level().isClientSide && reason.shouldDestroy()) {
            clearLights();
        }
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OPEN, false);
        builder.define(OPENED_AT, -1L);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(OPEN, tag.getBoolean("Open"));

        lightPositions.clear();
        for (long packed : tag.getLongArray("Lights")) {
            lightPositions.add(BlockPos.of(packed));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Open", isOpen());
        tag.putLongArray("Lights", lightPositions.stream().mapToLong(BlockPos::asLong).toArray());
    }

    // ---- no collision, nothing to interact with ----

    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPickable() { return false; }

    // ---- hitbox & culling ----

    /** The "Hitbox" cube in world space (rotated by the entity's yaw like GeckoLib rotates the model). */
    public AABB getWorldHitbox() {
        AABB b = BeaconGeometry.hitbox();
        double theta = Math.toRadians(180.0D - getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (double x : new double[] { b.minX, b.maxX }) {
            for (double z : new double[] { b.minZ, b.maxZ }) {
                double rx = x * cos + z * sin;
                double rz = -x * sin + z * cos;
                minX = Math.min(minX, rx); maxX = Math.max(maxX, rx);
                minZ = Math.min(minZ, rz); maxZ = Math.max(maxZ, rz);
            }
        }
        return new AABB(getX() + minX, getY() + b.minY, getZ() + minZ,
                getX() + maxX, getY() + b.maxY, getZ() + maxZ);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        double r = BeaconGeometry.radius();
        return new AABB(getX() - r, getY() + BeaconGeometry.minY(), getZ() - r,
                getX() + r, getY() + BeaconGeometry.maxY(), getZ() + r);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        double range = Math.max(128.0D, BeaconGeometry.radius() * 4.0D);
        return distanceSqr < range * range;
    }

    // ---- tick ----

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            clientTick();
        } else {
            serverTick();
        }
    }

    private void serverTick() {
        if (!isOpen()) return;
        for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, getWorldHitbox(),
                p -> p.isAlive() && !p.isSpectator() && !BeaconTeleportManager.isPending(p.getUUID()))) {
            enter(player);
        }
    }

    private void enter(ServerPlayer player) {
        if (enterDelayTicks < 0) {
            enterDelayTicks = OggDuration.ticks(ENTER_SOUND_FILE, 40);
        }
        level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.BEACON_ENTER.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                enterDelayTicks + 20, 4, false, false, false));
        BeaconTeleportManager.begin(player, enterDelayTicks);
    }

    private void clientTick() {
        if (isOpen()) {
            if (!idleSoundStarted) {
                idleSoundStarted = true;
                BeaconClientHooks.startIdleSound(this);
            }
        } else {
            idleSoundStarted = false;
        }
    }

    // ---- GeckoLib ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, this::animate));
    }

    private PlayState animate(AnimationState<BeaconEntity> state) {
        if (!isOpen()) {
            clientAnimation = null;
            return PlayState.STOP;
        }
        if (clientAnimation == null) {
            // Just opened -> play Open then Idle. Already open when we first saw it -> Idle only.
            long openedAt = entityData.get(OPENED_AT);
            boolean fresh = openedAt >= 0 && level().getGameTime() - openedAt < 40;
            clientAnimation = fresh ? OPEN_THEN_IDLE : IDLE;
        }
        return state.setAndContinue(clientAnimation);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}