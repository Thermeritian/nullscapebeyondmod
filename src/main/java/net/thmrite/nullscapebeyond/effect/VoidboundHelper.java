package net.thmrite.nullscapebeyond.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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

    /**
     * Do NOT use entity.hasEffect(ModEffects.VOIDBOUND): effects are stored in a map keyed by Holder, and the
     * DeferredHolder is not the same key as the registry Holder the client / saved data use, so it fails there.
     * Check the effect type instead.
     */
    public static boolean hasVoidbound(LivingEntity e) {
        for (MobEffectInstance instance : e.getActiveEffects()) {
            if (instance.getEffect().value() instanceof VoidboundEffect) return true;
        }
        return false;
    }

    /** The canonical registry holder: use this (not the DeferredHolder) when creating a MobEffectInstance. */
    public static Holder<MobEffect> voidboundHolder() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.VOIDBOUND.get());
    }

    /** Infestors and voidbound entities are all on the same side. */
    public static boolean isVoidAligned(Entity e) {
        return e instanceof InfestorEntity || (e instanceof LivingEntity l && hasVoidbound(l));
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
                () -> hasVoidbound(mob)));
    }
}