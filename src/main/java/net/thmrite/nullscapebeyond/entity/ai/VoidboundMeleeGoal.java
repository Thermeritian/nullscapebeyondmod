package net.thmrite.nullscapebeyond.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.thmrite.nullscapebeyond.registry.ModEffects;

public class VoidboundMeleeGoal extends MeleeAttackGoal {
    public VoidboundMeleeGoal(PathfinderMob mob, double speed) {
        super(mob, speed, true);
    }

    @Override
    public boolean canUse() {
        return mob.hasEffect(ModEffects.VOIDBOUND) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return mob.hasEffect(ModEffects.VOIDBOUND) && super.canContinueToUse();
    }
}