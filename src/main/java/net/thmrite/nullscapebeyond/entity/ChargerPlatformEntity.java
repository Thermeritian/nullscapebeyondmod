package net.thmrite.nullscapebeyond.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;


public class ChargerPlatformEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(ChargerPlatformEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    /** Must match the animation's name inside charger_platform.animation.json. */
    public static final String IDLE_ANIMATION = "idle";
    private static final int MAX_AGE = 600; // safety net if the "end" message is ever lost

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ChargerPlatformEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
        setNoGravity(true);
    }

    public void setOwner(Player owner) {
        this.entityData.set(OWNER, Optional.of(owner.getUUID()));
    }

    @Nullable
    public Player getOwner() {
        return this.entityData.get(OWNER).map(level()::getPlayerByUUID).orElse(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        Player owner = getOwner();
        if (owner == null || !owner.isAlive() || !ChargerBoots.isEquipped(owner) || tickCount > MAX_AGE) {
            discard();
            return;
        }
        setPos(owner.getX(), getY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop(IDLE_ANIMATION))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
