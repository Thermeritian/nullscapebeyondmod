package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.worldgen.CobaltMushroomFeature;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, NullscapeBeyond.MODID);

    public static final DeferredHolder<Feature<?>, CobaltMushroomFeature> COBALT_MUSHROOM =
            FEATURES.register("cobalt_mushroom", CobaltMushroomFeature::new);

    /** Configured feature (data/nscapebeyond/worldgen/configured_feature/cobalt_mushroom.json) used when the small mushroom grows. */
    public static final ResourceKey<ConfiguredFeature<?, ?>> COBALT_MUSHROOM_HUGE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "cobalt_mushroom"));
}
