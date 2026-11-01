package net.thmrite.nullscapebeyond.shaders.lighting.client;

import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.thmrite.nullscapebeyond.shaders.compat.IrisCompat;
import net.thmrite.nullscapebeyond.shaders.lighting.LightingSettings;
import net.thmrite.nullscapebeyond.shaders.lighting.client.LightGridBuilder.Point;

final class LightGlowRenderer {
    private LightGlowRenderer() {}

    private static final float MAX_DISTANCE = 64f;

    static void render(RenderLevelStageEvent event) {
        if (!LightingSettings.enabled() || !IrisCompat.isShaderPackActive() || IrisCompat.isRenderingShadowPass()) return;

        List<Point> points = DynamicLightEngine.INSTANCE.glowPoints();
        if (points.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ensureTexture(mc.getTextureManager());

        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        float strength = LightingSettings.strength();

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(GlowRenderTypes.GLOW);

        for (Point p : points) {
            float x = (float) (p.x() - cam.x), y = (float) (p.y() - cam.y), z = (float) (p.z() - cam.z);
            float dist = Mth.sqrt(x * x + y * y + z * z);
            if (dist > MAX_DISTANCE) continue;

            float near = Mth.clamp((dist - 0.8f) / 2.5f, 0f, 1f);                // no screen-filling glare up close
            float far = 1f - Mth.clamp((dist - 40f) / (MAX_DISTANCE - 40f), 0f, 1f);
            float alpha = Mth.clamp(p.intensity() * 0.5f * strength * near * far, 0f, 1f);
            if (alpha < 0.02f) continue;

            float size = 0.25f + p.radius() * 0.06f; // half-extent in blocks
            int r = (int) (p.r() * 255f), g = (int) (p.g() * 255f), b = (int) (p.b() * 255f), a = (int) (alpha * 255f);

            float lx = left.x * size, ly = left.y * size, lz = left.z * size;
            float ux = up.x * size, uy = up.y * size, uz = up.z * size;
            vertex(vc, x + lx - ux, y + ly - uy, z + lz - uz, 0f, 1f, r, g, b, a);
            vertex(vc, x - lx - ux, y - ly - uy, z - lz - uz, 1f, 1f, r, g, b, a);
            vertex(vc, x - lx + ux, y - ly + uy, z - lz + uz, 1f, 0f, r, g, b, a);
            vertex(vc, x + lx + ux, y + ly + uy, z + lz + uz, 0f, 0f, r, g, b, a);
        }
        buffers.endBatch(GlowRenderTypes.GLOW);
    }

    private static void vertex(VertexConsumer vc, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
        vc.addVertex(x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);
    }

    /** Builds a 64x64 white radial gradient (alpha = falloff) the first time it is needed. */
    private static void ensureTexture(TextureManager textures) {
        if (textures.getTexture(GlowRenderTypes.TEXTURE, null) != null) return;
        int size = 64;
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, size, size, false);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x + 0.5f) / (size / 2f) - 1f;
                float dy = (y + 0.5f) / (size / 2f) - 1f;
                float d = Math.min(1f, Mth.sqrt(dx * dx + dy * dy));
                float falloff = (float) Math.pow(1f - d, 2.2) + 0.35f * (float) Math.pow(1f - d, 6.0); // soft body + hot core
                int alpha = Mth.clamp((int) (falloff * 255f), 0, 255);
                image.setPixelRGBA(x, y, (alpha << 24) | 0x00FFFFFF); // ABGR, white
            }
        }
        DynamicTexture texture = new DynamicTexture(image);
        texture.setFilter(true, false);
        textures.register(GlowRenderTypes.TEXTURE, texture);
    }
}