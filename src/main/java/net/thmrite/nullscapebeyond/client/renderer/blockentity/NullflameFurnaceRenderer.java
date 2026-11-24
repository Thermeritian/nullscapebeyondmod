package net.thmrite.nullscapebeyond.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.entity.NullflameCombustion;


public class NullflameFurnaceRenderer implements BlockEntityRenderer<AbstractFurnaceBlockEntity> {
    private static final ResourceLocation FURNACE = tex("furnace_front_on_nullflame");
    private static final ResourceLocation BLAST_FURNACE = tex("blast_furnace_front_on_nullflame");
    private static final ResourceLocation SMOKER = tex("smoker_front_on_nullflame");

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "block/" + name);
    }

    @Override
    public void render(AbstractFurnaceBlockEntity furnace, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!(furnace instanceof NullflameCombustion combustion) || !combustion.nscapebeyond$isNullflame()) return;
        BlockState state = furnace.getBlockState();
        if (!state.hasProperty(AbstractFurnaceBlock.LIT) || !state.getValue(AbstractFurnaceBlock.LIT)) return;

        Direction facing = state.getValue(AbstractFurnaceBlock.FACING);
        ResourceLocation id = furnace instanceof BlastFurnaceBlockEntity ? BLAST_FURNACE
                : furnace instanceof SmokerBlockEntity ? SMOKER : FURNACE;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(id);
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);
        PoseStack.Pose pose = poseStack.last();
        float z = -0.002F;   // a hair in front of the face so it does not z-fight with the vanilla front
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        vertex(consumer, pose, 1.0F, 1.0F, z, u0, v0);
        vertex(consumer, pose, 1.0F, 0.0F, z, u0, v1);
        vertex(consumer, pose, 0.0F, 0.0F, z, u1, v1);
        vertex(consumer, pose, 0.0F, 1.0F, z, u1, v0);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)     // flames are bright
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
