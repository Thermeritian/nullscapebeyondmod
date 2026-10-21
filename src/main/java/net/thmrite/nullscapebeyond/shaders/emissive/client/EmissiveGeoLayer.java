package net.thmrite.nullscapebeyond.shaders.emissive.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.shaders.emissive.EmissiveSettings;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * GeckoLib layer: draws foo_e.png over the normal foo.png render, fullbright and with the neon shader,
 * plus an optional additive halo shell. Works for GeoEntityRenderer and GeoBlockRenderer alike.
 *
 *   addRenderLayer(new EmissiveGeoLayer<>(this));
 */
public class EmissiveGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final Set<Object> WARNED = ConcurrentHashMap.newKeySet();

    private final boolean glowShell;
    private final float shellScale;
    private final int shellColor;

    public EmissiveGeoLayer(GeoRenderer<T> renderer) {
        this(renderer, true, 1.06f, 0.35f);
    }

    public EmissiveGeoLayer(GeoRenderer<T> renderer, boolean glowShell, float shellScale, float shellAlpha) {
        super(renderer);
        NullscapeBeyond.LOGGER.info("[emissive] EmissiveGeoLayer attached to {}", renderer.getClass().getName());
        this.glowShell = glowShell;
        this.shellScale = shellScale;
        int a = Math.max(30, Math.min(255, (int) (shellAlpha * 255f)));
        this.shellColor = (a << 24) | 0x00FFFFFF;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                       MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (WARNED.add("called")) {
            NullscapeBeyond.LOGGER.info("[emissive] layer render() reached: enabled={}, shaderReady={}, texture={}",
                    EmissiveSettings.enabled(), EmissiveRenderTypes.isReady(), this.renderer.getTextureLocation(animatable));
        }
        if (!EmissiveSettings.enabled()) return;
        if (!EmissiveRenderTypes.isReady()) {
            if (WARNED.add("shader")) NullscapeBeyond.LOGGER.warn("[emissive] neon shader not loaded; emissive layer skipped");
            return;
        }

        ResourceLocation base = this.renderer.getTextureLocation(animatable);
        ResourceLocation emissive = EmissiveTextures.emissiveOf(base);
        if (emissive == null) {
            if (WARNED.add(base)) NullscapeBeyond.LOGGER.warn("[emissive] no _e texture found for {} (expected {}_e.png)", base, base.toString().replace(".png", ""));
            return;
        }

        RenderType solid = EmissiveRenderTypes.emissive(emissive);
        this.renderer.reRender(bakedModel, poseStack, bufferSource, animatable, solid,
                bufferSource.getBuffer(solid), partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        if (glowShell) {
            // Model origin is the feet (entities) / bottom centre (blocks): inflate around the vertical middle.
            float pivotY = animatable instanceof Entity e ? e.getBbHeight() * 0.5f : 0.5f;
            RenderType halo = EmissiveRenderTypes.glow(emissive);
            poseStack.pushPose();
            poseStack.translate(0f, pivotY, 0f);
            poseStack.scale(shellScale, shellScale, shellScale);
            poseStack.translate(0f, -pivotY, 0f);
            this.renderer.reRender(bakedModel, poseStack, bufferSource, animatable, halo,
                    bufferSource.getBuffer(halo), partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, shellColor);
            poseStack.popPose();
        }
    }
}