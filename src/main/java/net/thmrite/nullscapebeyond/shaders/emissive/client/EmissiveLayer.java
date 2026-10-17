package net.thmrite.nullscapebeyond.shaders.emissive.client;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.thmrite.nullscapebeyond.shaders.emissive.EmissiveSettings;

/**
 * Same idea for non-GeckoLib entity renderers (plain EntityModel):
 *   this.addLayer(new EmissiveLayer<>(this, e -> TEXTURE));   // TEXTURE = ".../foo.png", emissive is ".../foo_e.png"
 */
public class EmissiveLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final Function<T, ResourceLocation> baseTexture;

    public EmissiveLayer(RenderLayerParent<T, M> parent, Function<T, ResourceLocation> baseTexture) {
        super(parent);
        this.baseTexture = baseTexture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!EmissiveSettings.enabled() || !EmissiveRenderTypes.isReady() || entity.isInvisible()) return;
        ResourceLocation emissive = EmissiveTextures.emissiveOf(baseTexture.apply(entity));
        if (emissive == null) return;
        getParentModel().renderToBuffer(poseStack, buffers.getBuffer(EmissiveRenderTypes.emissive(emissive)),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}