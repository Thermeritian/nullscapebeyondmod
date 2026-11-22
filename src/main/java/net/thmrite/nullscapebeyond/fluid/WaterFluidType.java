package net.thmrite.nullscapebeyond.fluid;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;


public class WaterFluidType extends FluidType {
    @Nullable
    private final Supplier<? extends Item> bottle;
    private final ParticleOptions bubbleParticle;
    private final ParticleOptions splashParticle;

    public WaterFluidType(Properties properties, @Nullable Supplier<? extends Item> bottle,
                          ParticleOptions bubbleParticle, ParticleOptions splashParticle) {
        super(properties);
        this.bottle = bottle;
        this.bubbleParticle = bubbleParticle;
        this.splashParticle = splashParticle;
    }

    public static Properties waterProperties(String descriptionId) {
        return Properties.create()
                .descriptionId(descriptionId)
                .fallDistanceModifier(0.0F)
                .canExtinguish(true)
                .canConvertToSource(true)
                .supportsBoating(true)
                .canHydrate(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }

    @Nullable
    public Item getBottle() {
        return bottle == null ? null : bottle.get();
    }

    public ParticleOptions getBubbleParticle() {
        return bubbleParticle;
    }

    public ParticleOptions getSplashParticle() {
        return splashParticle;
    }
}
