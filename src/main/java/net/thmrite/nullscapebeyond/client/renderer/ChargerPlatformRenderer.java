package net.thmrite.nullscapebeyond.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.thmrite.nullscapebeyond.client.classcontrol.ChargerController;
import net.thmrite.nullscapebeyond.entity.ChargerPlatformEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ChargerPlatformRenderer extends GeoEntityRenderer<ChargerPlatformEntity> {
    private static final double Y_OFFSET = 0.0;

    public ChargerPlatformRenderer(EntityRendererProvider.Context context) {
        super(context, new ChargerPlatformModel());
        this.shadowRadius = 0f;
    }

    @Override
    public void render(ChargerPlatformEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        Player owner = entity.getOwner();
        if (owner != null) {
            double dx = Mth.lerp(partialTick, owner.xo, owner.getX()) - Mth.lerp(partialTick, entity.xo, entity.getX());
            double dz = Mth.lerp(partialTick, owner.zo, owner.getZ()) - Mth.lerp(partialTick, entity.zo, entity.getZ());
            double dy = 0;
            if (owner == net.minecraft.client.Minecraft.getInstance().player && ChargerController.platformRising) {
                dy = Mth.lerp(partialTick, owner.yo, owner.getY()) - Mth.lerp(partialTick, entity.yo, entity.getY());
            }
            poseStack.translate(dx, dy + Y_OFFSET, dz);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
