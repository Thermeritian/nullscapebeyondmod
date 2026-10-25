package net.thmrite.nullscapebeyond.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.thmrite.nullscapebeyond.registry.ModEffects;


/**
 * Added to every LivingEntityRenderer. While the entity has Voidbound it re-draws the model on top of itself:
 * 1) with the void-recoloured texture (same geometry, same depth -> fully covers the original)
 * 2) with the white highlights, fullbright, brightness pulsing over time
 */
public class VoidboundLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public VoidboundLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !entity.hasEffect(ModEffects.VOIDBOUND)) return;

        VoidTextures.Entry tex = VoidTextures.get(getTextureLocation(entity));
        if (tex == null) return;

        M model = getParentModel();
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);

        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(tex.base())),
                packedLight, overlay, -1);

        // pulse: 0.35..1.0, desynchronised per entity
        float pulse = 0.675F + 0.325F * Mth.sin(ageInTicks * 0.15F + entity.getId() * 1.7F);
        int v = (int) (255 * pulse);
        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.eyes(tex.glow())),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.color(255, v, v, v));
    }
}