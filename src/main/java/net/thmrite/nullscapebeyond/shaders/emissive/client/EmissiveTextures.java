package net.thmrite.nullscapebeyond.shaders.emissive.client;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/**
 * Resolves the "_e" companion of a texture:  textures/entity/foo.png  ->  textures/entity/foo_e.png
 * (and the block-atlas sprite  nscapebeyond:block/foo  ->  nscapebeyond:block/foo_e).
 * Only textures of this mod are ever resolved, so vanilla and other mods are never touched.
 */
public final class EmissiveTextures {
    private EmissiveTextures() {}

    private static final Map<ResourceLocation, Optional<ResourceLocation>> FILES = new ConcurrentHashMap<>();
    private static final Map<TextureAtlasSprite, Optional<TextureAtlasSprite>> SPRITES = new ConcurrentHashMap<>();

    /** Emissive file for a full texture path (".png"), or null when there is none. */
    @Nullable
    public static ResourceLocation emissiveOf(ResourceLocation base) {
        return FILES.computeIfAbsent(base, EmissiveTextures::resolveFile).orElse(null);
    }

    private static Optional<ResourceLocation> resolveFile(ResourceLocation base) {
        String path = base.getPath();
        if (!base.getNamespace().equals(NullscapeBeyond.MODID) || !path.endsWith(".png") || path.endsWith("_e.png")) {
            System.out.println(" \n Emissive Found Texture" + Optional.empty() + "\n");
            return Optional.empty();
        }

        ResourceLocation emissive = base.withPath(path.substring(0, path.length() - 4) + "_e.png");
        System.out.println(" \n Emissive Found Texture" + Optional.of(emissive) + "\n");
        return Minecraft.getInstance().getResourceManager().getResource(emissive).isPresent()
                ? Optional.of(emissive) : Optional.empty();
    }

    /** Emissive block-atlas sprite for a block sprite, or null. Call at render time (atlas must be stitched). */
    @Nullable
    public static TextureAtlasSprite emissiveSprite(TextureAtlasSprite base) {
        return SPRITES.computeIfAbsent(base, s -> {
            ResourceLocation name = s.contents().name();
            if (!name.getNamespace().equals(NullscapeBeyond.MODID) || name.getPath().endsWith("_e")) return Optional.empty();
            TextureAtlasSprite es = Minecraft.getInstance().getModelManager()
                    .getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(name.withSuffix("_e"));
            return es.contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? Optional.empty() : Optional.of(es);
        }).orElse(null);
    }

    /** Called on resource reload. */
    public static void clear() {
        FILES.clear();
        SPRITES.clear();
    }
}