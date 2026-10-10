package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class ModTags {
    public static final TagKey<Block> CHARGER_PASSABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "charger_passable"));

    private ModTags() {}
}
