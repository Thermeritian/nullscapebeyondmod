package net.thmrite.nullscapebeyond.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.MartEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MartRenderer extends GeoEntityRenderer<MartEntity> {
    public MartRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "mart")));
        this.shadowRadius = 0.4f;
    }

    @Override
    public void preRender(PoseStack poseStack, MartEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, colour);
        if (isReRender) return;
        float squish = animatable.getSquish(partialTick);
        if (squish != 0.0f) {
            float xz = 1.0f + MartEntity.SQUISH_XZ * squish;
            float y = 1.0f - MartEntity.SQUISH_Y * squish;
            poseStack.scale(xz, y, xz);
        }
    }

    @Override
    public RenderType getRenderType(MartEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture); // water: allow alpha in the texture
    }
}