package net.thmrite.nullscapebeyond.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.item.MartBucketItem;
import net.thmrite.nullscapebeyond.registry.ModEntities;
import net.thmrite.nullscapebeyond.registry.ModSounds;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class MartEntity extends TamableAnimal implements GeoEntity {

    // ---- Tuning ---------------------------------------------------------------------------
    public static final int MAX_WATER = 25;
    public static final int MAX_MERGE = 4;
    public static final int BUCKET_WATER = 5;   // water bucket <-> 5 units
    public static final int BOTTLE_WATER = 2;   // water bottle <-> 2 units
    public static final int COST_SHOOT = 1;
    public static final int COST_EXTINGUISH = 1;
    public static final int COST_LAVA = 2;
    public static final int SWALLOW_UPKEEP_TICKS = 100;  // 1 water per 5s while holding a victim
    public static final int SWALLOW_MIN_WATER = 3;       // releases victim below this
    public static final float SWALLOW_DAMAGE = 2.0f;     // per second
    public static final int MERGE_COOLDOWN_AFTER_DAMAGE = 6000; // "a long time" (5 min)
    public static final int MERGE_COOLDOWN_CALM = 100;
    public static final float MERGE_DISSOLVE_DAMAGE = 12.0f;    // damage taken while merged before it falls apart
    public static final float FIRE_DAMAGE_MULTIPLIER = 2.0f;
    public static final float WATER_HEALTH_PER_UNIT = 1.0f;
    public static final float MERGE_HEALTH_PER_MEMBER = 6.0f;
    public static final float WATER_SCALE_PER_UNIT = 0.1f;
    public static final float MERGE_SCALE_PER_MEMBER = 0.3f;
    public static final float MERGE_DAMAGE_PER_MEMBER = 1.5f;
    public static final double BASE_SPEED = 0.4;        // speed at scale 1.0 (was 0.3)
    public static final double SPEED_PER_SCALE = 0.15;  // extra speed per +1.0 of scale, like slimes (0.2 + 0.1 * size)

    public static final TagKey<EntityType<?>> FIRE_CREATURES = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "fire_creatures"));

    private static final ResourceLocation WATER_HEALTH_ID = id("mart_water_health");
    private static final ResourceLocation MERGE_HEALTH_ID = id("mart_merge_health");
    private static final ResourceLocation WATER_SCALE_ID = id("mart_water_scale");
    private static final ResourceLocation MERGE_SCALE_ID = id("mart_merge_scale");
    private static final ResourceLocation MERGE_DAMAGE_ID = id("mart_merge_damage");
    private static final ResourceLocation SIZE_SPEED_ID = id("mart_size_speed");

    // ---- Synced data ----------------------------------------------------------------------
    private static final EntityDataAccessor<Integer> DATA_WATER =
            SynchedEntityData.defineId(MartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MERGE_COUNT =
            SynchedEntityData.defineId(MartEntity.class, EntityDataSerializers.INT);

    // ---- Animations -----------------------------------------------------------------------
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation AIR_IDLE = RawAnimation.begin().thenLoop("air_idle");
    private static final RawAnimation JUMP = RawAnimation.begin().thenPlayAndHold("jump");
    private static final RawAnimation LAND_THEN_IDLE = RawAnimation.begin().thenPlay("land").thenLoop("idle");
    private static final RawAnimation MERGE = RawAnimation.begin().thenPlay("merge");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean airborne;
    private boolean landing;

    // ---- Server state ---------------------------------------------------------------------
    private final IntArrayList mergedWater = new IntArrayList(); // water of absorbed members (size == mergeCount - 1)
    private int mergeCooldown;
    private float mergeDamage;
    private int noTargetTicks;
    private int shootCooldown;
    private int attackCooldown;
    private int extinguishCooldown;
    private int lavaCooldown;
    private int swallowCooldown;
    @Nullable private LivingEntity swallowed;
    private int swallowTicks;

    public MartEntity(EntityType<? extends MartEntity> type, Level level) {
        super(type, level);
        this.moveControl = new MartMoveControl(this);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, path);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    public static boolean checkMartSpawnRules(EntityType<MartEntity> type, LevelAccessor level, MobSpawnType reason,
                                              BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isValidSpawn(level, below, type) && Animal.isBrightEnoughToSpawn(level, pos);
    }

    // ---------------------------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------------------------
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WATER, 0);
        builder.define(DATA_MERGE_COUNT, 1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MartGoals.FloatGoal(this));
        this.goalSelector.addGoal(1, new MartGoals.SwallowGoal(this));
        this.goalSelector.addGoal(2, new MartGoals.ShootGoal(this));
        this.goalSelector.addGoal(3, new MartGoals.FirefightGoal(this));
        this.goalSelector.addGoal(4, new MartGoals.MergeGoal(this));
        this.goalSelector.addGoal(5, new MartGoals.ChaseGoal(this));
        this.goalSelector.addGoal(6, new MartGoals.FollowOwnerGoal(this));
        this.goalSelector.addGoal(7, new MartGoals.RandomHopGoal(this));

        this.targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers(MartEntity.class));
        // Tamed Marts also help against mobs that are going after their owner.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> isTame() && getOwner() != null && e instanceof Mob m && m.getTarget() == getOwner()));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        setWater(random.nextInt(4)); // wild Marts start with 0-3 units
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    // ---------------------------------------------------------------------------------------
    // Water + stats
    // ---------------------------------------------------------------------------------------
    public int getWater() {
        return entityData.get(DATA_WATER);
    }

    public void setWater(int water) {
        entityData.set(DATA_WATER, Mth.clamp(water, 0, MAX_WATER));
        if (!level().isClientSide) refreshStats();
    }

    public boolean consumeWater(int amount) {
        if (getWater() < amount) return false;
        setWater(getWater() - amount);
        return true;
    }

    public int getMergeCount() {
        return entityData.get(DATA_MERGE_COUNT);
    }

    private void setMergeCount(int count) {
        entityData.set(DATA_MERGE_COUNT, Mth.clamp(count, 1, MAX_MERGE));
        if (!level().isClientSide) refreshStats();
    }

    public boolean isMerged() {
        return getMergeCount() > 1;
    }

    /** Max health, size and contact damage all derive from water level + merge count. Modifiers are transient. */
    private void refreshStats() {
        int merges = getMergeCount() - 1;
        applyModifier(Attributes.MAX_HEALTH, WATER_HEALTH_ID, getWater() * WATER_HEALTH_PER_UNIT, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(Attributes.MAX_HEALTH, MERGE_HEALTH_ID, merges * MERGE_HEALTH_PER_MEMBER, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(Attributes.SCALE, WATER_SCALE_ID, getWater() * WATER_SCALE_PER_UNIT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(Attributes.SCALE, MERGE_SCALE_ID, merges * MERGE_SCALE_PER_MEMBER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(Attributes.ATTACK_DAMAGE, MERGE_DAMAGE_ID, merges * MERGE_DAMAGE_PER_MEMBER, AttributeModifier.Operation.ADD_VALUE);
        // Bigger Mart = faster hops (hop distance comes from movement speed), like vanilla slimes.
        applyModifier(Attributes.MOVEMENT_SPEED, SIZE_SPEED_ID,
                (getAttributeValue(Attributes.SCALE) - 1.0) * SPEED_PER_SCALE, AttributeModifier.Operation.ADD_VALUE);
        if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
        refreshDimensions();
    }

    private void applyModifier(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, amount, op));
    }

    /** Higher level -> lower pitch (mart_merge.ogg is reused for every merge / fill step). */
    public static float pitchForLevel(int level) {
        return Math.max(0.5f, 1.4f - level * 0.09f);
    }

    // ---------------------------------------------------------------------------------------
    // Interaction
    // ---------------------------------------------------------------------------------------
    private static int waterAmount(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) return BUCKET_WATER;
        if (stack.is(Items.POTION)
                && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            return BOTTLE_WATER;
        }
        return 0;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean owner = isTame() && isOwnedBy(player);
        boolean wild = !isTame();
        boolean client = level().isClientSide;

        if (player.isSecondaryUseActive() && (stack.is(Items.BUCKET) || stack.is(Items.WATER_BUCKET)) && (owner || wild)) {
            if (isMerged()) return InteractionResult.PASS;
            if (!client) {
                releaseSwallowed();
                ItemStack bucket = MartBucketItem.fromMart(this);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, bucket));
                playSound(SoundEvents.BUCKET_FILL_FISH, 1.0f, 1.0f);
                discard();
            }
            return InteractionResult.sidedSuccess(client);
        }

        // Right click with a water container -> store water (and, for wild Marts, try to befriend)
        int amount = waterAmount(stack);
        if (amount > 0 && (owner || wild)) {
            if (owner && getWater() + amount > MAX_WATER) {
                if (!client) player.displayClientMessage(Component.translatable("message.nscapebeyond.mart.full"), true);
                return InteractionResult.sidedSuccess(client);
            }
            if (!client) {
                setWater(getWater() + amount);
                heal(amount * WATER_HEALTH_PER_UNIT);
                playSound(ModSounds.MART_MERGE.get(), 1.0f, pitchForLevel(getWater()));
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(stack.is(Items.WATER_BUCKET) ? Items.BUCKET : Items.GLASS_BOTTLE)));
                if (wild) {
                    if (random.nextInt(3) == 0) {
                        tame(player);
                        setTarget(null);
                        level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            }
            return InteractionResult.sidedSuccess(client);
        }

        // Right click with an empty container (owner only) -> withdraw water
        if (owner && !player.isSecondaryUseActive()) {
            ItemStack filled = ItemStack.EMPTY;
            int cost = 0;
            if (stack.is(Items.BUCKET) && getWater() >= BUCKET_WATER) {
                filled = new ItemStack(Items.WATER_BUCKET);
                cost = BUCKET_WATER;
            } else if (stack.is(Items.GLASS_BOTTLE) && getWater() >= BOTTLE_WATER) {
                filled = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                cost = BOTTLE_WATER;
            }
            if (!filled.isEmpty()) {
                if (!client) {
                    setWater(getWater() - cost);
                    playSound(ModSounds.MART_MERGE.get(), 1.0f, pitchForLevel(getWater()));
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
                }
                return InteractionResult.sidedSuccess(client);
            }
        }
        return super.mobInteract(player, hand);
    }

    public void applyBucketData(CompoundTag tag) {
        setWater(tag.getInt(MartBucketItem.TAG_WATER));
        if (tag.hasUUID(MartBucketItem.TAG_OWNER)) {
            setOwnerUUID(tag.getUUID(MartBucketItem.TAG_OWNER));
            setTame(true, true);
        }
        if (tag.contains(MartBucketItem.TAG_HEALTH)) {
            setHealth(Math.min(tag.getFloat(MartBucketItem.TAG_HEALTH), getMaxHealth()));
        }
    }

    // ---------------------------------------------------------------------------------------
    // Friend / foe helpers
    // ---------------------------------------------------------------------------------------
    /** Friendly = the owner and the owner's tamed pets (including other Marts). */
    public boolean isFriendly(Entity entity) {
        if (!isTame() || getOwnerUUID() == null) return false;
        if (entity.getUUID().equals(getOwnerUUID())) return true;
        return entity instanceof TamableAnimal pet && pet.isTame() && getOwnerUUID().equals(pet.getOwnerUUID());
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isTame() && isFriendly(target)) return false;
        return super.canAttack(target);
    }

    private boolean sameGroup(MartEntity other) {
        return isTame() == other.isTame() && (!isTame() || Objects.equals(getOwnerUUID(), other.getOwnerUUID()));
    }

    // ---------------------------------------------------------------------------------------
    // Merge system
    // ---------------------------------------------------------------------------------------
    public boolean canMerge() {
        return mergeCooldown <= 0 && getMergeCount() < MAX_MERGE && swallowed == null && isAlive();
    }

    public boolean canMergeWith(MartEntity other) {
        return other != this && other.isAlive() && other.canMerge() && sameGroup(other)
                && getMergeCount() + other.getMergeCount() <= MAX_MERGE && other.getTarget() != null;
    }

    @Nullable
    public MartEntity findMergePartner() {
        MartEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (MartEntity m : level().getEntitiesOfClass(MartEntity.class, getBoundingBox().inflate(12.0), this::canMergeWith)) {
            double d = distanceToSqr(m);
            if (d < bestDist) {
                bestDist = d;
                best = m;
            }
        }
        return best;
    }

    /** Called on the member with the lower entity id; the other one is absorbed. */
    public void absorb(MartEntity other) {
        if (level().isClientSide || !other.isAlive()) return;
        mergedWater.add(other.getWater());
        mergedWater.addAll(other.mergedWater);
        int gained = other.getMergeCount();
        setMergeCount(getMergeCount() + gained);
        heal(MERGE_HEALTH_PER_MEMBER * gained);
        mergeDamage = 0;
        other.releaseSwallowed();
        other.discard();
        playSound(ModSounds.MART_MERGE.get(), 1.0f, pitchForLevel((getMergeCount() - 1) * 3));
        triggerAnim("action", "merge");
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, getX(), getY() + getBbHeight() * 0.5, getZ(), 20, 0.4, 0.3, 0.4, 0.1);
        }
    }

    public void dissolve(boolean byDamage) {
        if (level().isClientSide || !isMerged()) return;
        int members = getMergeCount() - 1;
        int cooldown = byDamage ? MERGE_COOLDOWN_AFTER_DAMAGE : MERGE_COOLDOWN_CALM;
        float healthFraction = Math.max(0.2f, getHealth() / getMaxHealth());
        for (int i = 0; i < members; i++) {
            MartEntity child = ModEntities.MART.get().create(level());
            if (child == null) continue;
            child.moveTo(getX() + (random.nextDouble() - 0.5) * 1.5, getY(), getZ() + (random.nextDouble() - 0.5) * 1.5,
                    random.nextFloat() * 360f, 0f);
            if (isTame() && getOwnerUUID() != null) {
                child.setOwnerUUID(getOwnerUUID());
                child.setTame(true, true);
            }
            child.setWater(i < mergedWater.size() ? mergedWater.getInt(i) : 0);
            child.setHealth(Math.max(1f, child.getMaxHealth() * healthFraction));
            child.mergeCooldown = cooldown;
            child.setDeltaMovement((random.nextDouble() - 0.5) * 0.4, 0.35, (random.nextDouble() - 0.5) * 0.4);
            level().addFreshEntity(child);
        }
        mergedWater.clear();
        setMergeCount(1);
        mergeCooldown = cooldown;
        mergeDamage = 0;
        noTargetTicks = 0;
        playSound(ModSounds.MART_MERGE.get(), 1.0f, 1.4f); // reverse of the merge: high pitch
        triggerAnim("action", "merge");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Anything swallowed takes the hit instead of the Mart.
        if (swallowed != null && swallowed.isAlive() && source.getEntity() instanceof Player) {
            swallowed.hurt(source, amount);
            return false;
        }
        float before = getHealth();
        boolean result = super.hurt(source, amount);
        if (result && !level().isClientSide && isMerged() && isAlive()) {
            mergeDamage += before - getHealth();
            if (mergeDamage >= MERGE_DISSOLVE_DAMAGE) dissolve(true);
        }
        return result;
    }

    // ---------------------------------------------------------------------------------------
    // Combat
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (target.getType().is(FIRE_CREATURES)) damage *= FIRE_DAMAGE_MULTIPLIER;
        boolean hit = target.hurt(damageSources().mobAttack(this), damage);
        if (hit && target instanceof LivingEntity living) {
            living.knockback(0.4, getX() - target.getX(), getZ() - target.getZ());
            playSound(SoundEvents.GENERIC_SPLASH, 0.6f, 1.2f);
        }
        return hit;
    }

    public int getShootCooldown() {
        return shootCooldown;
    }

    /** Fires a water jolt at the given point. Costs water. */
    public void shootJolt(Vec3 targetPos) {
        if (level().isClientSide || shootCooldown > 0 || !consumeWater(COST_SHOOT)) return;
        WaterJoltEntity jolt = new WaterJoltEntity(level(), this);
        Vec3 origin = position().add(0, getBbHeight() * 0.6, 0);
        jolt.setPos(origin);
        Vec3 dir = targetPos.subtract(origin);
        jolt.shoot(dir.x, dir.y, dir.z, 1.4f, 0.5f);
        level().addFreshEntity(jolt);
        triggerAnim("action", "shoot");
        playSound(ModSounds.MART_SHOOT.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
        shootCooldown = 25;
    }

    // ---- swallow (drowning) ----
    @Nullable
    public LivingEntity getSwallowed() {
        return swallowed;
    }

    public boolean canSwallow(LivingEntity target) {
        return swallowed == null && swallowCooldown <= 0 && !isMerged() && getWater() >= MAX_WATER
                && target instanceof Enemy && !target.canBreatheUnderwater()
                && target.getBbWidth() <= 1.0f && target.getBbHeight() <= 2.1f;
    }

    public void swallow(LivingEntity target) {
        if (level().isClientSide || !canSwallow(target)) return;
        swallowed = target;
        swallowTicks = 0;
        playSound(SoundEvents.PLAYER_SPLASH, 1.0f, 0.8f);
    }

    public void releaseSwallowed() {
        if (swallowed != null) {
            if (swallowed.isAlive()) swallowed.setDeltaMovement(0, 0.3, 0);
            swallowed = null;
            swallowCooldown = 60;
        }
    }

    private void tickSwallow() {
        if (swallowed == null) return;
        if (!swallowed.isAlive() || swallowed.isRemoved() || getWater() < SWALLOW_MIN_WATER || distanceToSqr(swallowed) > 16.0) {
            releaseSwallowed();
            return;
        }
        // Hold the victim inside the Mart. (Visually they just overlap; a render layer can come later.)
        swallowed.setPos(getX(), getY() + 0.1, getZ());
        swallowed.setDeltaMovement(Vec3.ZERO);
        swallowed.fallDistance = 0;
        if (swallowed instanceof Mob mob) mob.setTarget(null);
        swallowTicks++;
        if (swallowTicks % 20 == 0) swallowed.hurt(damageSources().drown(), SWALLOW_DAMAGE);
        if (swallowTicks % SWALLOW_UPKEEP_TICKS == 0) consumeWater(1);
    }

    // ---------------------------------------------------------------------------------------
    // Hop / movement helpers (used by goals)
    // ---------------------------------------------------------------------------------------
    protected int getJumpDelay() {
        return random.nextInt(20) + 10;
    }

    @Override
    public void jumpFromGround() {
        Vec3 v = getDeltaMovement();
        setDeltaMovement(v.x, getJumpPower(), v.z);
        hasImpulse = true;
    }

    public void faceYaw(float yaw, double speed, boolean aggressive, boolean move) {
        MartMoveControl control = (MartMoveControl) getMoveControl();
        control.setDirection(yaw, aggressive);
        if (move) control.setWantedMovement(speed);
    }

    public void faceAndHop(Vec3 pos, double speed, boolean aggressive) {
        faceYaw(yawTo(pos), speed, aggressive, true);
    }

    public void faceOnly(Vec3 pos) {
        faceYaw(yawTo(pos), 0, false, false);
    }

    private float yawTo(Vec3 pos) {
        return (float) (Mth.atan2(pos.z - getZ(), pos.x - getX()) * (180.0 / Math.PI)) - 90.0f;
    }

    /** True when lava or fire is within hop range in that direction. */
    public boolean isDangerAhead(float yawDeg) {
        double rad = Math.toRadians(yawDeg);
        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);
        for (double d = 1.0; d <= 2.5; d += 0.75) {
            BlockPos base = BlockPos.containing(getX() + dx * d * getScale(), getY(), getZ() + dz * d * getScale());
            for (int dy = 0; dy >= -2; dy--) {
                BlockPos p = base.offset(0, dy, 0);
                BlockState state = level().getBlockState(p);
                if (level().getFluidState(p).is(FluidTags.LAVA) || state.is(BlockTags.FIRE)) return true;
                if (!state.getCollisionShape(level(), p).isEmpty()) break;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------
    // Tick
    // ---------------------------------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (mergeCooldown > 0) mergeCooldown--;
        if (shootCooldown > 0) shootCooldown--;
        if (attackCooldown > 0) attackCooldown--;
        if (extinguishCooldown > 0) extinguishCooldown--;
        if (lavaCooldown > 0) lavaCooldown--;
        if (swallowCooldown > 0) swallowCooldown--;

        // Accidentally fell into lava: spend water to turn it into stone.
        if (isInLava() && lavaCooldown <= 0 && getWater() >= COST_LAVA && convertLavaAround(this)) {
            consumeWater(COST_LAVA);
            lavaCooldown = 20;
        }

        tickFirefighting();
        tickSwallow();

        // Merged Marts relax and fall apart once the threat is gone.
        if (isMerged()) {
            noTargetTicks = getTarget() == null ? noTargetTicks + 1 : 0;
            if (noTargetTicks > 100) dissolve(false);
        }

        // Contact damage.
        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && attackCooldown <= 0 && swallowed == null
                && getBoundingBox().inflate(0.2).intersects(target.getBoundingBox())) {
            doHurtTarget(target);
            attackCooldown = 20;
        }
    }

    private void tickFirefighting() {
        if (!isTame() || extinguishCooldown > 0 || getWater() < COST_EXTINGUISH) return;

        // Burning friends (owner, owner's pets) in contact.
        for (LivingEntity friend : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.4), this::isFriendly)) {
            if (friend.isInLava()) {
                if (getWater() >= COST_LAVA && convertLavaAround(friend)) {
                    consumeWater(COST_LAVA);
                    extinguishCooldown = 20;
                    return;
                }
            } else if (friend.isOnFire()) {
                friend.clearFire();
                splashAt(friend);
                consumeWater(COST_EXTINGUISH);
                extinguishCooldown = 10;
                return;
            }
        }

        // Burning blocks by contact.
        BlockPos here = blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(here.offset(-1, 0, -1), here.offset(1, 1, 1))) {
            if (level().getBlockState(p).is(BlockTags.FIRE)) {
                level().removeBlock(p, false);
                level().playSound(null, p, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.0f);
                consumeWater(COST_EXTINGUISH);
                extinguishCooldown = 10;
                return;
            }
        }
    }

    private void splashAt(Entity entity) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                    15, 0.3, 0.4, 0.3, 0.1);
        }
        level().playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.NEUTRAL, 0.6f, 1.0f);
    }

    /**
     * Lava in a 3x3x3 cube around {@code who} becomes obsidian (source) or cobblestone (flowing).
     * The column the entity stands in is cleared to air instead, so it never ends up inside a block.
     */
    private boolean convertLavaAround(Entity who) {
        Level level = level();
        BlockPos feet = who.blockPosition();
        boolean changed = false;
        for (BlockPos p : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 1, 1))) {
            FluidState fluid = level.getFluidState(p);
            if (!fluid.is(FluidTags.LAVA)) continue;
            BlockPos pos = p.immutable();
            boolean ownColumn = pos.getX() == feet.getX() && pos.getZ() == feet.getZ() && pos.getY() >= feet.getY();
            BlockState result = ownColumn
                    ? Blocks.AIR.defaultBlockState()
                    : (fluid.isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE).defaultBlockState();
            level.setBlockAndUpdate(pos, result);
            changed = true;
        }
        if (changed) {
            who.clearFire();
            level.playSound(null, feet, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 1.0f);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.LARGE_SMOKE, who.getX(), who.getY() + 0.5, who.getZ(), 12, 0.4, 0.3, 0.4, 0.02);
            }
        }
        return changed;
    }

    // ---------------------------------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------------------------------
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Water", getWater());
        tag.putInt("MergeCount", getMergeCount());
        tag.putIntArray("MergedWater", mergedWater.toIntArray());
        tag.putInt("MergeCooldown", mergeCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_WATER, Mth.clamp(tag.getInt("Water"), 0, MAX_WATER));
        entityData.set(DATA_MERGE_COUNT, Mth.clamp(Math.max(1, tag.getInt("MergeCount")), 1, MAX_MERGE));
        mergedWater.clear();
        for (int w : tag.getIntArray("MergedWater")) mergedWater.add(w);
        mergeCooldown = tag.getInt("MergeCooldown");
        refreshStats();
    }

    // ---------------------------------------------------------------------------------------
    // Misc vanilla overrides
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_SPLASH;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MART_DEATH.get();
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.SLIME_JUMP_SMALL;
    }

    // ---------------------------------------------------------------------------------------
    // GeckoLib
    // ---------------------------------------------------------------------------------------
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 2, this::locomotion));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("merge", MERGE)
                .triggerableAnim("shoot", SHOOT));
    }

    private PlayState locomotion(AnimationState<MartEntity> state) {
        if (onGround() || isInWater()) {
            if (airborne) {
                airborne = false;
                landing = true;
            }
            return state.setAndContinue(landing ? LAND_THEN_IDLE : IDLE);
        }
        airborne = true;
        landing = false;
        return state.setAndContinue(getDeltaMovement().y > 0.05 ? JUMP : AIR_IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ---------------------------------------------------------------------------------------
    // Slime-style move control: face a direction, then hop toward it.
    // ---------------------------------------------------------------------------------------
    static class MartMoveControl extends MoveControl {
        private final MartEntity mart;
        private float yRot;
        private int jumpDelay;
        private boolean aggressive;

        MartMoveControl(MartEntity mart) {
            super(mart);
            this.mart = mart;
            this.yRot = mart.getYRot();
            this.jumpDelay = mart.getJumpDelay();
        }

        void setDirection(float yRot, boolean aggressive) {
            this.yRot = yRot;
            this.aggressive = aggressive;
        }

        void setWantedMovement(double speed) {
            this.speedModifier = speed;
            this.operation = Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            mob.setYRot(rotlerp(mob.getYRot(), yRot, 90.0f));
            mob.yHeadRot = mob.getYRot();
            mob.yBodyRot = mob.getYRot();
            if (operation != Operation.MOVE_TO) {
                mob.setZza(0.0f);
                return;
            }
            operation = Operation.WAIT;
            float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            if (mob.onGround()) {
                mob.setSpeed(speed);
                if (jumpDelay-- <= 0) {
                    if (mart.isDangerAhead(yRot)) {
                        // Mart avoids lava at all cost: turn away instead of hopping in.
                        yRot += 90.0f + mart.getRandom().nextInt(180);
                        jumpDelay = 5;
                        mob.xxa = 0;
                        mob.zza = 0;
                        mob.setSpeed(0);
                        return;
                    }
                    jumpDelay = mart.getJumpDelay();
                    if (aggressive) jumpDelay /= 3;
                    mob.getJumpControl().jump();
                    mart.playSound(mart.getJumpSound(), 0.5f, 1.0f + (mart.getRandom().nextFloat() - 0.5f) * 0.3f);
                } else {
                    mob.xxa = 0.0f;
                    mob.zza = 0.0f;
                    mob.setSpeed(0.0f);
                }
            } else {
                mob.setSpeed(speed);
            }
        }
    }
}