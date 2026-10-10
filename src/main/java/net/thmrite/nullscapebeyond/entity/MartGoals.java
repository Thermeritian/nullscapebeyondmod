package net.thmrite.nullscapebeyond.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/** All of Mart's survival-context goals. Nullscape-context behavior can live in a sibling class later. */
public final class MartGoals {
    private MartGoals() {}

    /** Hop around in water instead of sinking. */
    public static class FloatGoal extends Goal {
        private final MartEntity mart;

        public FloatGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return mart.isInWater() && mart.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 0.4;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (mart.getRandom().nextFloat() < 0.8f) mart.getJumpControl().jump();
            mart.faceYaw(mart.getYRot(), 1.2, false, true);
        }
    }

    /** Tamed + full of water: drown a small hostile inside the Mart instead of spending water on jolts. */
    public static class SwallowGoal extends Goal {
        private final MartEntity mart;

        public SwallowGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mart.getTarget();
            return mart.isTame() && target != null && target.isAlive() && mart.canSwallow(target)
                    && mart.distanceTo(target) < 1.5f + mart.getBbWidth();
        }

        @Override
        public void start() {
            LivingEntity target = mart.getTarget();
            if (target != null) mart.swallow(target);
        }

        @Override
        public boolean canContinueToUse() {
            return mart.getSwallowed() != null;
        }
    }

    /** Tamed Marts shoot water jolts at hostiles that are going after their owner. */
    public static class ShootGoal extends Goal {
        private final MartEntity mart;

        public ShootGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean valid(double minDist, double maxDist) {
            LivingEntity target = mart.getTarget();
            if (!mart.isTame() || target == null || !target.isAlive() || mart.getWater() < MartEntity.COST_SHOOT) return false;
            if (mart.getSwallowed() != null) return false;
            // Full Marts prefer to save water and swallow swallowable targets.
            if (mart.getWater() >= MartEntity.MAX_WATER && mart.canSwallow(target)) return false;
            double d = mart.distanceTo(target);
            return d >= minDist && d <= maxDist && mart.hasLineOfSight(target);
        }

        @Override
        public boolean canUse() {
            return valid(3.0, 14.0);
        }

        @Override
        public boolean canContinueToUse() {
            return valid(2.5, 15.0);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = mart.getTarget();
            if (target == null) return;
            mart.getLookControl().setLookAt(target);
            mart.faceOnly(target.position());
            if (mart.getShootCooldown() <= 0) {
                mart.shootJolt(target.getBoundingBox().getCenter());
            }
        }
    }

    /** Hop toward / shoot at burning friends (owner, pets) and fire near the owner. */
    public static class FirefightGoal extends Goal {
        private final MartEntity mart;
        @Nullable private Entity burning;
        @Nullable private BlockPos fire;
        private int ticks;

        public FirefightGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!mart.isTame() || mart.getWater() < 1 || mart.getSwallowed() != null) return false;
            if (mart.tickCount % 10 != 0) return false; // throttle the scans
            LivingEntity owner = mart.getOwner();
            if (owner == null || owner.level() != mart.level()) return false;

            burning = null;
            fire = null;
            for (LivingEntity e : mart.level().getEntitiesOfClass(LivingEntity.class,
                    mart.getBoundingBox().inflate(12.0), mart::isFriendly)) {
                if (e.isOnFire() || e.isInLava()) {
                    burning = e;
                    return true;
                }
            }
            BlockPos o = owner.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -1, -4), o.offset(4, 2, 4))) {
                if (mart.level().getBlockState(p).is(BlockTags.FIRE)) {
                    fire = p.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (ticks > 100 || mart.getWater() < 1) return false;
            if (burning != null) return burning.isAlive() && (burning.isOnFire() || burning.isInLava());
            return fire != null && mart.level().getBlockState(fire).is(BlockTags.FIRE);
        }

        @Override
        public void start() {
            ticks = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            ticks++;
            Vec3 pos = burning != null
                    ? burning.position().add(0, burning.getBbHeight() * 0.5, 0)
                    : Vec3.atCenterOf(fire);
            double dist = mart.position().distanceTo(pos);
            boolean inLava = burning != null && burning.isInLava(); // lava needs contact, a jolt can't fix it
            boolean canSee = fire != null || (burning instanceof LivingEntity le && mart.hasLineOfSight(le));

            if (!inLava && canSee && dist >= 2.0 && dist <= 9.0 && mart.getShootCooldown() <= 0) {
                mart.faceOnly(pos);
                mart.shootJolt(pos);
            } else if (dist > 1.2) {
                mart.faceAndHop(pos, 1.3, true);
            } else {
                mart.faceOnly(pos);
            }
        }
    }

    /** Aggravated Marts of the same group hop together and fuse. */
    public static class MergeGoal extends Goal {
        private final MartEntity mart;
        @Nullable private MartEntity partner;

        public MergeGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (mart.getTarget() == null || !mart.canMerge()) return false;
            if (mart.tickCount % 5 != 0) return false;
            partner = mart.findMergePartner();
            return partner != null;
        }

        @Override
        public boolean canContinueToUse() {
            return partner != null && mart.getTarget() != null && mart.canMerge() && mart.canMergeWith(partner);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (partner == null) return;
            mart.faceAndHop(partner.position(), 1.3, true);
            double reach = (mart.getBbWidth() + partner.getBbWidth()) * 0.6;
            if (mart.distanceToSqr(partner) < reach * reach && mart.getId() < partner.getId()) {
                mart.absorb(partner);
                partner = null;
            }
        }

        @Override
        public void stop() {
            partner = null;
        }
    }

    /** Contact attack: face the target and hop into it. */
    public static class ChaseGoal extends Goal {
        private final MartEntity mart;

        public ChaseGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = mart.getTarget();
            return t != null && t.isAlive() && mart.getSwallowed() == null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity t = mart.getTarget();
            if (t == null) return;
            mart.getLookControl().setLookAt(t);
            mart.faceAndHop(t.position(), 1.2, true);
        }
    }

    public static class FollowOwnerGoal extends Goal {
        private final MartEntity mart;

        public FollowOwnerGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = mart.getOwner();
            return mart.isTame() && owner != null && mart.getTarget() == null && owner.level() == mart.level()
                    && mart.distanceToSqr(owner) > 36.0;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity owner = mart.getOwner();
            return mart.isTame() && owner != null && mart.getTarget() == null && mart.distanceToSqr(owner) > 9.0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity owner = mart.getOwner();
            if (owner == null) return;
            if (mart.distanceToSqr(owner) > 576.0 && owner.onGround()) { // > 24 blocks: catch up
                for (int i = 0; i < 10; i++) {
                    double x = owner.getX() + mart.getRandom().nextInt(5) - 2;
                    double z = owner.getZ() + mart.getRandom().nextInt(5) - 2;
                    if (mart.randomTeleport(x, owner.getY(), z, true)) return;
                }
            }
            mart.faceAndHop(owner.position(), 1.2, false);
        }
    }

    public static class RandomHopGoal extends Goal {
        private final MartEntity mart;
        private float direction;
        private int ticks;

        public RandomHopGoal(MartEntity mart) {
            this.mart = mart;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return mart.getTarget() == null && (mart.onGround() || mart.isInWater()) && mart.getRandom().nextInt(30) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return ticks > 0 && mart.getTarget() == null;
        }

        @Override
        public void start() {
            ticks = 40 + mart.getRandom().nextInt(60);
            direction = mart.getRandom().nextInt(360);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            ticks--;
            if (ticks % 40 == 0) direction += mart.getRandom().nextInt(120) - 60;
            mart.faceYaw(direction, 1.0, false, true);
        }
    }
}