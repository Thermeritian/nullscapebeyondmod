package net.thmrite.nullscapebeyond.client.renderer.player;

import net.thmrite.nullscapebeyond.client.classcontrol.DiverController;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;


@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class DiverTiltRenderer {
    private static final double PIVOT_Y = 0.3;      // centre of the swimming hitbox (0.6 high)
    private static final float MAX_TILT = 80f;
    private static Quaternionf applied;

    @SubscribeEvent
    public static void onPre(RenderPlayerEvent.Pre event) {
        applied = null;
        Player p = event.getEntity();
        // Only the out-of-water swimming pose (dive / slide / crawling); real swimming already follows the camera
        if (!p.isVisuallySwimming() || p.isInWater()) return;

        float pt = event.getPartialTick();
        float tilt = Mth.clamp(tiltDegrees(p, pt), -MAX_TILT, MAX_TILT); // + = nose up
        if (Math.abs(tilt) < 0.5f) return;

        // Horizontal axis perpendicular to where the body faces; rotating about it pitches the body
        float yaw = Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot) * Mth.DEG_TO_RAD;
        Quaternionf q = new Quaternionf().rotationAxis(-tilt * Mth.DEG_TO_RAD, Mth.cos(yaw), 0f, Mth.sin(yaw));

        // Adjusts the stack in place (no push) so that, even if another mod cancels the render, the dispatcher's own
        // pop restores it. Post undoes it so the shadow and fire are not tilted.
        PoseStack poseStack = event.getPoseStack();
        poseStack.translate(0, PIVOT_Y, 0);
        poseStack.mulPose(q);
        poseStack.translate(0, -PIVOT_Y, 0);
        applied = q;
    }

    @SubscribeEvent
    public static void onPost(RenderPlayerEvent.Post event) {
        if (applied == null) return;
        PoseStack poseStack = event.getPoseStack();
        poseStack.translate(0, PIVOT_Y, 0);
        poseStack.mulPose(new Quaternionf(applied).conjugate());
        poseStack.translate(0, -PIVOT_Y, 0);
        applied = null;
    }

    private static float tiltDegrees(Player p, float partialTick) {
        if (p == Minecraft.getInstance().player) return DiverController.visualPitch(partialTick);
        double dx = p.getX() - p.xo, dy = p.getY() - p.yo, dz = p.getZ() - p.zo;
        double horizontal = Math.hypot(dx, dz);
        if (horizontal < 0.05) return 0f;
        return (float) Math.toDegrees(Math.atan2(dy, horizontal));
    }

    private DiverTiltRenderer() {}
}
