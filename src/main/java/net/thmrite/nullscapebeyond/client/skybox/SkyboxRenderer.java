package net.thmrite.nullscapebeyond.client.skybox;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;


@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class SkyboxRenderer {
    private static final int SEGMENTS = 64;
    private static final int RINGS = 32;
    private static final float RADIUS = 100f;

    private static VertexBuffer sphere;

    private SkyboxRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        ResourceLocation texture = SkyboxManager.texture();
        if (texture == null) return;

        SkyboxManager.advance();
        if (sphere == null) sphere = buildSphere();

        // Yaw is applied outermost so "left/right" always spins around the world's vertical axis.
        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix())
                .rotateY((float) Math.toRadians(SkyboxManager.yaw()))
                .rotateX((float) Math.toRadians(SkyboxManager.pitch()))
                .rotateZ((float) Math.toRadians(SkyboxManager.roll()));
        float[] tint = SkyboxManager.tint();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(tint[0], tint[1], tint[2], 1f);

        sphere.bind();
        sphere.drawWithShader(modelView, event.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SkyboxManager.reset();
    }

    private static VertexBuffer buildSphere() {
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < RINGS; i++) {
            float v0 = i / (float) RINGS;
            float v1 = (i + 1) / (float) RINGS;
            for (int j = 0; j < SEGMENTS; j++) {
                float u0 = j / (float) SEGMENTS;
                float u1 = (j + 1) / (float) SEGMENTS;
                vertex(b, u0, v0);
                vertex(b, u0, v1);
                vertex(b, u1, v1);
                vertex(b, u1, v0);
            }
        }
        VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vb.bind();
        vb.upload(b.buildOrThrow());
        VertexBuffer.unbind();
        return vb;
    }

    /** u: 0..1 around the horizon, v: 0 (zenith) .. 1 (nadir). */
    private static void vertex(BufferBuilder b, float u, float v) {
        double theta = u * 2.0 * Math.PI;
        double phi = Math.PI / 2.0 - v * Math.PI;
        float x = (float) (RADIUS * Math.cos(phi) * Math.sin(theta));
        float y = (float) (RADIUS * Math.sin(phi));
        float z = (float) (-RADIUS * Math.cos(phi) * Math.cos(theta));
        b.addVertex(x, y, z).setUv(u, v);
    }
}