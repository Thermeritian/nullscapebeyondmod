package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, NullscapeBeyond.MODID);

    /** Little water drops thrown up when a Mart lands (same idea as the slime's landing particles). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MART_DROP =
            PARTICLES.register("mart_drop", () -> new SimpleParticleType(false));

    private ModParticles() {}
}