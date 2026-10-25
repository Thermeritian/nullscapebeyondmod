package net.thmrite.nullscapebeyond.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.common.Tags;
import net.thmrite.nullscapebeyond.entity.InfestorEntity;
import net.thmrite.nullscapebeyond.entity.ai.VoidTargetGoal;
import net.thmrite.nullscapebeyond.entity.ai.VoidboundMeleeGoal;
import net.thmrite.nullscapebeyond.registry.ModEffects;

public final class VoidboundHelper {
    /** Persistent-data key on the host that stores the infestor's health while it is "inside". */
    public static final String KEY_INFESTOR_HEALTH = "nscapebeyond_infestor_health";

    private VoidboundHelper() {}

    /** Infestors and voidbound entities are all on the same side. */
    public static boolean isVoidAligned(Entity e) {
        return e instanceof InfestorEntity || (e instanceof LivingEntity l && l.hasEffect(ModEffects.VOIDBOUND));
    }

    public static boolean isPet(Entity e) {
        return e instanceof OwnableEntity o && o.getOwnerUUID() != null;
    }

    /** Valid for voidification: any non-boss mob that is not a player, a pet or already void-aligned. */
    public static boolean canCorrupt(Entity e) {
        return e instanceof Mob m && m.isAlive() && !isVoidAligned(m) && !isPet(m)
                && !m.getType().is(Tags.EntityTypes.BOSSES);
    }

    /** Makes any mob hostile (players first). Safe to call repeatedly: it only installs the goals once. */
    public static void applyAggression(Mob mob) {
        if (mob instanceof InfestorEntity) return;
        for (WrappedGoal g : mob.targetSelector.getAvailableGoals()) {
            if (g.getGoal() instanceof VoidTargetGoal) return;
        }
        mob.goalSelector.removeAllGoals(g -> g instanceof PanicGoal || g instanceof AvoidEntityGoal<?>
                || g instanceof TemptGoal || g instanceof BreedGoal || g instanceof FollowParentGoal
                || g instanceof RunAroundLikeCrazyGoal);
        if (mob instanceof PathfinderMob pm) {
            mob.goalSelector.addGoal(1, new VoidboundMeleeGoal(pm, 1.3D));
        }
        mob.targetSelector.addGoal(0, new VoidTargetGoal(mob, false,
                e -> !isVoidAligned(e) && !(e instanceof ArmorStand),
                () -> mob.hasEffect(ModEffects.VOIDBOUND)));
    }
}