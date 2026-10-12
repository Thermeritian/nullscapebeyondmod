package net.thmrite.nullscapebeyond.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock.Mode;
import net.thmrite.nullscapebeyond.block.entity.LevelObjectBlockEntity;
import net.thmrite.nullscapebeyond.block.entity.LevelObjectBlockEntity.Phase;

public class LevelObjectRenderer implements BlockEntityRenderer<LevelObjectBlockEntity> {
    private static final ResourceLocation GIFT = tex("gift/gift");
    private static final ResourceLocation GOLDEN_GIFT = tex("golden_gift/golden_gift");
    private static final ResourceLocation TRIPMINE = tex("tripmine/tripmine");
    private static final ResourceLocation EXPLOSION = tex("tripmine/explosion");

    private static final int FRAMES = 4;
    private static final float IDLE_TICKS_PER_FRAME = 3.0F;
    private static final float ARMED_TICKS_PER_FRAME = 1.0F;   // faster once the tripmine is armed
    private static final float MIN_IDLE_TICKS = 40.0F;         // random pause between plays
    private static final float MAX_IDLE_TICKS = 160.0F;
    private static final float EXPLOSION_TICKS_PER_FRAME = 1.0F;

    public LevelObjectRenderer(BlockEntityRendererProvider.Context context) {}

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "textures/block/" + path + ".png");
    }

    @Override
    public void render(LevelObjectBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        Level level = be.getLevel();
        if (level == null) return;
        BlockState state = be.getBlockState();
        Mode mode = state.getValue(LevelObjectBlock.MODE);
        if (mode == Mode.NONE || state.getValue(LevelObjectBlock.COLLECTED)) return;

        float t = level.getGameTime() + partialTick;

        if (mode == Mode.TRIPMINE && be.getPhase() == Phase.EXPLODING) {
            float elapsed = Math.max(0.0F, t - be.getPhaseStart());
            int frame = Math.min(15, (int) (elapsed / EXPLOSION_TICKS_PER_FRAME));
            draw(pose, buffers, EXPLOSION, (float) LevelObjectBlock.EXPLOSION_RADIUS * 2.0F,
                    frame % 4, frame / 4, 4, 4);   // 4x4 sheet, read left to right, top to bottom
            return;
        }

        ResourceLocation texture = switch (mode) {
            case GIFT -> GIFT;
            case GOLDEN_GIFT -> GOLDEN_GIFT;
            default -> TRIPMINE;
        };

        int frame;
        if (mode == Mode.TRIPMINE && be.getPhase() == Phase.ARMED) {
            float elapsed = Math.max(0.0F, t - be.getPhaseStart());
            frame = (int) (elapsed / ARMED_TICKS_PER_FRAME) % FRAMES;
        } else {
            frame = idleFrame(be, level, t);
        }
        draw(pose, buffers, texture, 1.0F, 0, frame, 1, FRAMES);   // 1 column x 4 rows (50x200)
    }

    /** Plays the 4 frames, then waits a random interval before playing again. */
    private static int idleFrame(LevelObjectBlockEntity be, Level level, float t) {
        float duration = FRAMES * IDLE_TICKS_PER_FRAME;
        if (!be.animInit) {
            be.animInit = true;
            be.animStart = -1000.0F;
            be.nextAnim = t + level.random.nextFloat() * MAX_IDLE_TICKS;
        }
        if (t >= be.nextAnim) {
            be.animStart = t;
            be.nextAnim = t + duration + MIN_IDLE_TICKS
                    + level.random.nextFloat() * (MAX_IDLE_TICKS - MIN_IDLE_TICKS);
        }
        float elapsed = t - be.animStart;
        return elapsed >= 0.0F && elapsed < duration ? (int) (elapsed / IDLE_TICKS_PER_FRAME) : 0;
    }

    private static void draw(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                             float size, int col, int row, int cols, int rows) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(texture));
        float u0 = (float) col / cols, u1 = (float) (col + 1) / cols;
        float v0 = (float) row / rows, v1 = (float) (row + 1) / rows;
        float h = size / 2.0F;

        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation()); // billboard
        PoseStack.Pose last = pose.last();
        vertex(vc, last, -h, -h, u0, v1);
        vertex(vc, last, h, -h, u1, v1);
        vertex(vc, last, h, h, u1, v0);
        vertex(vc, last, -h, h, u0, v0);
        pose.popPose();
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v) {
        vc.addVertex(pose, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)   // emissive
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}