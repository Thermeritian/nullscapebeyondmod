package net.thmrite.nullscapebeyond.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.effect.VoidboundEffect;
import net.thmrite.nullscapebeyond.effect.VoidboundHelper;
import net.thmrite.nullscapebeyond.entity.ai.InfestorLeapGoal;
import net.thmrite.nullscapebeyond.entity.ai.VoidTargetGoal;
import net.thmrite.nullscapebeyond.registry.ModEffects;
import net.thmrite.nullscapebeyond.registry.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;

public class InfestorEntity extends Monster implements GeoEntity {
    public static final int EXIT_COOLDOWN_TICKS = 20 * 20;

    // Change these if the names inside infestor.animation.json differ.
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation LEAP = RawAnimation.begin().then("leap", Animation.LoopType.PLAY_ONCE);

    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(InfestorEntity.class, EntityDataSerializers.BYTE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int infestCooldown;

    public InfestorEntity(EntityType<? extends InfestorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    // ---------------------------------------------------------------- AI

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new InfestorLeapGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Players first, then the nearest mob it can corrupt (only when off cooldown) or a pet to bite.
        targetSelector.addGoal(2, new VoidTargetGoal(this, true,
                e -> !VoidboundHelper.isVoidAligned(e)
                        && ((isReadyToInfest() && VoidboundHelper.canCorrupt(e)) || VoidboundHelper.isPet(e)),
                () -> true));
    }

    public boolean isReadyToInfest() {
        return infestCooldown <= 0;
    }

    public void setInfestCooldown(int ticks) {
        this.infestCooldown = ticks;
    }

    /** Touch / melee: corrupt valid mobs, otherwise (players, pets, cooldown) plain damage. */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof Mob host && tryInfest(host)) return true;
        return super.doHurtTarget(target);
    }

    private boolean tryInfest(Mob host) {
        if (level().isClientSide || !isReadyToInfest() || !VoidboundHelper.canCorrupt(host)) return false;
        boolean applied = host.addEffect(new MobEffectInstance(ModEffects.VOIDBOUND,
                MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
        if (!applied) return false;

        host.getPersistentData().putFloat(VoidboundHelper.KEY_INFESTOR_HEALTH, getHealth());
        host.setPersistenceRequired(); // a despawning host would swallow the infestor

        if (level() instanceof ServerLevel sl) {
            sl.playSound(null, host.blockPosition(), ModSounds.INFESTOR_CORRUPT.get(), SoundSource.HOSTILE, 1.2F, 1.0F);
            sl.sendParticles(ParticleTypes.PORTAL, host.getX(), host.getY(0.5D), host.getZ(), 30,
                    host.getBbWidth() * 0.5D, host.getBbHeight() * 0.5D, host.getBbWidth() * 0.5D, 0.2D);
        }
        discard(); // it lives inside the host now; VoidboundEvents#onDeath brings it back
        return true;
    }

    // ---------------------------------------------------------------- tick / data

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            setClimbing(horizontalCollision);
            if (infestCooldown > 0) infestCooldown--;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("InfestCooldown", infestCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        infestCooldown = tag.getInt("InfestCooldown");
    }

    // ---------------------------------------------------------------- spider traits

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public boolean onClimbable() {
        return isClimbing();
    }

    public boolean isClimbing() {
        return (entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte b = entityData.get(DATA_FLAGS_ID);
        b = climbing ? (byte) (b | 1) : (byte) (b & ~1);
        entityData.set(DATA_FLAGS_ID, b);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.is(MobEffects.POISON) || effect.getEffect().value() instanceof VoidboundEffect) return false;
        return super.canBeAffected(effect);
    }

    // ---------------------------------------------------------------- sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.INFESTOR_HISS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.INFESTOR_HISS.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.INFESTOR_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(ModSounds.INFESTOR_STEP.get(), 0.15F, 1.0F);
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::movementPredicate)
                .triggerableAnim("leap", LEAP));
    }

    private PlayState movementPredicate(AnimationState<InfestorEntity> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}