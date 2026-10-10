package net.thmrite.nullscapebeyond.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.registry.ModEntities;
import net.thmrite.nullscapebeyond.registry.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WaterJoltEntity extends Projectile implements GeoEntity {
    public static final float DAMAGE = 3.0f;
    private static final int MAX_LIFETIME = 40;
    private static final int HIT_TICKS = 5; // "hit" animation is 0.25s

    private static final EntityDataAccessor<Boolean> DATA_HIT =
            SynchedEntityData.defineId(WaterJoltEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HIT = RawAnimation.begin().thenPlayAndHold("hit");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int hitTicks;

    public WaterJoltEntity(EntityType<? extends WaterJoltEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public WaterJoltEntity(Level level, LivingEntity owner) {
        this(ModEntities.WATER_JOLT.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HIT, false);
    }

    public boolean isHit() {
        return entityData.get(DATA_HIT);
    }

    @Override
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_HIT.equals(key) && isHit()) setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    /** Only things worth hitting stop the jolt: burning friends, enemies, the Mart's current target. */
    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;
        if (!(getOwner() instanceof MartEntity mart)) return false;
        if (mart.isFriendly(entity)) return entity.isOnFire();
        return entity == mart.getTarget() || entity instanceof Enemy;
    }

    @Override
    public void tick() {
        super.tick();

        if (isHit()) {
            setDeltaMovement(Vec3.ZERO);
            if (!level().isClientSide && ++hitTicks >= HIT_TICKS) discard();
            return;
        }

        if (!level().isClientSide) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                onHit(hit);
                if (isHit()) return;
            }
            extinguishFireAround();
            if (tickCount > MAX_LIFETIME || isInLava()) {
                discard();
                return;
            }
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
    }

    private void extinguishFireAround() {
        AABB box = getBoundingBox().inflate(0.4);
        for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (level().getBlockState(p).is(BlockTags.FIRE)) {
                level().removeBlock(p, false);
                level().playSound(null, p, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.2f);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (getOwner() instanceof MartEntity mart) {
            if (mart.isFriendly(target)) {
                target.clearFire();
            } else {
                float damage = DAMAGE;
                if (target.getType().is(MartEntity.FIRE_CREATURES)) damage *= MartEntity.FIRE_DAMAGE_MULTIPLIER;
                target.hurt(damageSources().mobProjectile(this, mart), damage);
            }
        }
        startHit(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos front = result.getBlockPos().relative(result.getDirection());
        if (level().getBlockState(front).is(BlockTags.FIRE)) level().removeBlock(front, false);
        startHit(result.getLocation());
    }

    private void startHit(Vec3 location) {
        setPos(location);
        setDeltaMovement(Vec3.ZERO);
        entityData.set(DATA_HIT, true);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.WATER_JOLT_HIT.get(), SoundSource.NEUTRAL,
                1.0f, 0.9f + random.nextFloat() * 0.2f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.1);
        }
    }

    // Projectiles of this kind are never saved (registered with noSave), but keep NBT hooks valid.
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    // ---- GeckoLib ----
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0,
                state -> state.setAndContinue(isHit() ? HIT : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}