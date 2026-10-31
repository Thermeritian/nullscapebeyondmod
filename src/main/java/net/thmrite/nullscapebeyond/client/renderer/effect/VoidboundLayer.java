package net.thmrite.nullscapebeyond.client.renderer.effect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.thmrite.nullscapebeyond.debug.VoidboundDebug;
import net.thmrite.nullscapebeyond.effect.VoidboundHelper;
import net.thmrite.nullscapebeyond.registry.ModEffects;

public class VoidboundLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private enum Channel { RED, GREEN, BLUE }

    private static final Channel PULSE_CHANNEL = Channel.RED;
    private static final float PULSE_MIN = 0.0F;

    public VoidboundLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        String type = entity.getType().toShortString();

        boolean has = VoidboundHelper.hasVoidbound(entity) || ClientVoidboundTracker.contains(entity.getId());
        VoidboundDebug.once("layer.called." + type + "." + has + "." + entity.hasEffect(ModEffects.VOIDBOUND),
                "Layer called for {}: hasVoidbound(type check)={}, hasEffect(DeferredHolder)={}",
                type, has, entity.hasEffect(ModEffects.VOIDBOUND));
        if (!has) return;

        VoidboundDebug.once("layer.effect." + type,
                "Layer render: {} (id {}) has voidbound on the CLIENT, invisible={}", type, entity.getId(), entity.isInvisible());
        if (entity.isInvisible()) return;

        ResourceLocation source = getTextureLocation(entity);
        VoidTextures.Entry tex = VoidTextures.get(source);
        if (tex == null) {
            VoidboundDebug.once("layer.notex." + source, "Layer render: no void texture for {} (source {}), see VoidTextures warnings", type, source);
            return;
        }
        VoidboundDebug.once("layer.draw." + source, "Layer render: drawing {} with base={} glow={}", type, tex.base(), tex.glow());

        M model = getParentModel();
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);

        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(tex.base())),
                packedLight, overlay, -1);

        float wave = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.15F + entity.getId() * 1.7F);
        int p = (int) (255 * Mth.lerp(wave, PULSE_MIN, 1.0F));
        boolean pr = PULSE_CHANNEL == Channel.RED, pg = PULSE_CHANNEL == Channel.GREEN, pb = PULSE_CHANNEL == Channel.BLUE;

        RenderType glow = RenderType.eyes(tex.glow());
        model.renderToBuffer(poseStack, buffers.getBuffer(glow), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.color(255, pr ? 0 : 255, pg ? 0 : 255, pb ? 0 : 255));
        model.renderToBuffer(poseStack, buffers.getBuffer(glow), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.color(255, pr ? p : 0, pg ? p : 0, pb ? p : 0));
    }
}