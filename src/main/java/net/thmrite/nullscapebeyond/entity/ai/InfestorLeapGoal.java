package net.thmrite.nullscapebeyond.entity.ai;

import java.util.EnumSet;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.entity.InfestorEntity;
import net.thmrite.nullscapebeyond.registry.ModSounds;

/** Step back (leap animation) -> launch at the target -> on touch: corrupt or damage (see InfestorEntity#doHurtTarget). */
public class InfestorLeapGoal extends Goal {
    /** Keep roughly equal to the length of the "leap" animation, in ticks. */
    public static final int WINDUP_TICKS = 14;

    private final InfestorEntity mob;
    private LivingEntity target;
    private boolean airborne;
    private boolean hit;
    private int windup;
    private int airTicks;
    private int nextLeapTick;

    public InfestorLeapGoal(InfestorEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (mob.tickCount < nextLeapTick || !mob.onGround() || mob.isPassenger()) return false;
        target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        double d = mob.distanceToSqr(target);
        if (d < 9.0D || d > 144.0D) return false; // 3 to 12 blocks
        if (!mob.hasLineOfSight(target)) return false;
        return mob.getRandom().nextInt(reducedTickDelay(4)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive() || hit) return false;
        return !airborne || (airTicks < 40 && (airTicks < 4 || !mob.onGround()));
    }

    @Override
    public void start() {
        airborne = false;
        hit = false;
        airTicks = 0;
        windup = WINDUP_TICKS;
        mob.getNavigation().stop();
        mob.setDeltaMovement(mob.getDeltaMovement().multiply(0, 1, 0));
        mob.playSound(ModSounds.INFESTOR_LEAP.get(), 1.0F, 1.0F);
        mob.triggerAnim("main", "leap");
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        if (!airborne) {
            mob.getNavigation().stop();
            if (--windup <= 0) launch();
            return;
        }
        airTicks++;
        if (airTicks >= 2 && mob.getBoundingBox().inflate(0.3D).intersects(target.getBoundingBox())) {
            hit = true;
            mob.doHurtTarget(target);
        }
    }

    private void launch() {
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.5D, 0)
                .subtract(mob.position().add(0, mob.getBbHeight() * 0.5D, 0));
        Vec3 flat = new Vec3(to.x, 0, to.z);
        double dist = flat.length();
        Vec3 dir = dist < 1.0E-4 ? mob.getLookAngle().multiply(1, 0, 1).normalize() : flat.normalize();
        double speed = Mth.clamp(dist / 6.0D, 0.45D, 1.5D); // tune these to taste
        double up = 0.42D + Mth.clamp(to.y * 0.05D, 0.0D, 0.3D);
        mob.setDeltaMovement(dir.x * speed, up, dir.z * speed);
        mob.hasImpulse = true;
        airborne = true;
        airTicks = 0;
    }

    @Override
    public void stop() {
        nextLeapTick = mob.tickCount + 40;
        target = null;
    }
}