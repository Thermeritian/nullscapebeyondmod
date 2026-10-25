package net.thmrite.nullscapebeyond.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.thmrite.nullscapebeyond.effect.VoidboundHelper;

public class VoidboundMeleeGoal extends MeleeAttackGoal {
    public VoidboundMeleeGoal(PathfinderMob mob, double speed) {
        super(mob, speed, true);
    }

    @Override
    public boolean canUse() {
        return VoidboundHelper.hasVoidbound(mob) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return VoidboundHelper.hasVoidbound(mob) && super.canContinueToUse();
    }
}