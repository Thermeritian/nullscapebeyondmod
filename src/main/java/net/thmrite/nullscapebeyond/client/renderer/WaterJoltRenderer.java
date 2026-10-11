package net.thmrite.nullscapebeyond.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.WaterJoltEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Loads geo/entity/water_jolt.geo.json (+ animation, + textures/entity/water_jolt.png).
 *
 * The model's front is -Z (Block at -Z, Tail trailing at +Z). GeckoLib 4.x does not apply the Y rotation of
 * non-living entities, so the flight direction (yaw + pitch) is applied here.
 *
 * If the bolt flies backwards or sideways in game, set YAW_OFFSET to 180 (or +-90).
 * If it pitches the wrong way, change PITCH_SIGN to -1.
 */
public class WaterJoltRenderer extends GeoEntityRenderer<WaterJoltEntity> {
    private static final float YAW_OFFSET = 0f;
    private static final float PITCH_SIGN = 1f;

    public WaterJoltRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "water_jolt")));
        this.shadowRadius = 0f;
    }

    @Override
    public void preRender(PoseStack poseStack, WaterJoltEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, colour);
        if (isReRender) return;
        float yaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw + YAW_OFFSET));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch * PITCH_SIGN));
    }

    @Override
    public RenderType getRenderType(WaterJoltEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}