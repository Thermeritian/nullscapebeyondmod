package net.thmrite.nullscapebeyond.registry;

import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class ModTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> AZURE_TREE_FEATURE = featureKey("azure_tree");

    public static final TreeGrower AZURE = new TreeGrower("azure", Optional.empty(), Optional.of(AZURE_TREE_FEATURE), Optional.empty());

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, name));
    }
}
