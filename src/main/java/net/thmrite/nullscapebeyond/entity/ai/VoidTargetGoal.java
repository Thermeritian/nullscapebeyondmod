package net.thmrite.nullscapebeyond.entity.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Player-first targeting. If no player is in range, falls back to the nearest entity accepted by {@code secondary}.
 * While chasing something that is not a player, it drops the target as soon as a player shows up.
 */
public class VoidTargetGoal extends TargetGoal {
    private final Predicate<LivingEntity> secondary;
    private final BooleanSupplier active;
    private final boolean mustSee;

    public VoidTargetGoal(Mob mob, boolean mustSee, Predicate<LivingEntity> secondary, BooleanSupplier active) {
        super(mob, mustSee);
        this.mustSee = mustSee;
        this.secondary = secondary;
        this.active = active;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private TargetingConditions conditions() {
        TargetingConditions c = TargetingConditions.forCombat().range(getFollowDistance());
        return mustSee ? c : c.ignoreLineOfSight();
    }

    private Player findPlayer() {
        return mob.level().getNearestPlayer(conditions(), mob);
    }

    private LivingEntity findTarget() {
        Player player = findPlayer();
        if (player != null) return player;
        double range = getFollowDistance();
        AABB box = mob.getBoundingBox().inflate(range, 4.0D, range);
        List<LivingEntity> candidates = mob.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != mob && !(e instanceof Player) && secondary.test(e));
        return mob.level().getNearestEntity(candidates, conditions(), mob, mob.getX(), mob.getEyeY(), mob.getZ());
    }

    @Override
    public boolean canUse() {
        if (!active.getAsBoolean()) return false;
        if (mob.getRandom().nextInt(reducedTickDelay(10)) != 0) return false;
        targetMob = findTarget();
        return targetMob != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity current = mob.getTarget();
        if (current != null && !(current instanceof Player) && mob.tickCount % 10 == 0 && findPlayer() != null) {
            return false; // re-evaluate, a player is around
        }
        return active.getAsBoolean() && super.canContinueToUse();
    }

    @Override
    public void start() {
        mob.setTarget(targetMob);
        super.start();
    }
}