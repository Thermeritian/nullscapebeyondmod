package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.effect.VoidboundEffect;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, NullscapeBeyond.MODID);

    public static final DeferredHolder<MobEffect, VoidboundEffect> VOIDBOUND =
            EFFECTS.register("voidbound", VoidboundEffect::new);

    private ModEffects() {}
}