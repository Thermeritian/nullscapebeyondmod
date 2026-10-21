package net.thmrite.nullscapebeyond.shaders.emissive.client;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/**
 * Render types that use the neon shader: unlit (fullbright), no lightmap, no hurt-flash overlay.
 * Extends RenderType only to reach its protected render-state constants.
 */
public final class EmissiveRenderTypes extends RenderType {
    private EmissiveRenderTypes() {
        super("nscapebeyond_unused", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
    }

    private static ShaderInstance neonShader;
    private static final RenderStateShard.ShaderStateShard NEON = new RenderStateShard.ShaderStateShard(() -> neonShader);

    static void setShader(ShaderInstance shader) {
        neonShader = shader;
    }

    public static boolean isReady() {
        return neonShader != null;
    }

    /** Solid fullbright pass. Transparent texels of the _e texture are discarded. */
    private static final Function<ResourceLocation, RenderType> EMISSIVE = Util.memoize(texture -> RenderType.create(
            "nscapebeyond_emissive", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(NEON)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)));

    /** Additive halo pass (use with a slightly inflated model and a low-alpha colour). */
    private static final Function<ResourceLocation, RenderType> GLOW = Util.memoize(texture -> RenderType.create(
            "nscapebeyond_emissive_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(NEON)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)));

    public static RenderType emissive(ResourceLocation texture) {
        return EMISSIVE.apply(texture);
    }

    public static RenderType glow(ResourceLocation texture) {
        return GLOW.apply(texture);
    }
}