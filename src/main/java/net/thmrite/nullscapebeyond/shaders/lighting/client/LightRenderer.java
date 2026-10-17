package net.thmrite.nullscapebeyond.shaders.lighting.client;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.thmrite.nullscapebeyond.shaders.lighting.LightingSettings;
import net.thmrite.nullscapebeyond.shaders.lighting.client.LightGridBuilder.Result;

/**
 * Applies the coloured light as a depth-based post pass over the finished frame. Because it only
 * needs the depth buffer it works the same for terrain (vanilla or Sodium), block entities,
 * entities and particles.
 */
final class LightRenderer {
    private LightRenderer() {}

    private static final int ATLAS = LightGridBuilder.ATLAS;

    private static ShaderInstance shader;
    private static TextureTarget sceneCopy;
    private static int gridTexture = -1;
    private static boolean gridEmpty = true;
    private static int gridOx, gridOy, gridOz;
    private static ByteBuffer uploadBuffer;

    static void setShader(ShaderInstance instance) {
        shader = instance;
    }

    static void render(RenderLevelStageEvent event) {
        if (!LightingSettings.enabled()) return;

        Result fresh = DynamicLightEngine.INSTANCE.pollResult();
        if (fresh != null) upload(fresh);
        if (gridEmpty || shader == null || shaderPackActive()) return;

        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();

        if (sceneCopy == null) {
            sceneCopy = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        } else if (sceneCopy.width != main.width || sceneCopy.height != main.height) {
            sceneCopy.resize(main.width, main.height, Minecraft.ON_OSX);
        }

        // Snapshot colour + depth so the shader can read them while we write back into the main target.
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneCopy.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, sceneCopy.width, sceneCopy.height,
                GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(false);

        Vec3 cam = event.getCamera().getPosition();
        Matrix4f invProj = new Matrix4f(event.getProjectionMatrix()).invert();
        Matrix4f invView = new Matrix4f(event.getModelViewMatrix()).invert();

        shader.setSampler("SceneColor", sceneCopy.getColorTextureId());
        shader.setSampler("SceneDepth", sceneCopy.getDepthTextureId());
        shader.setSampler("LightGrid", gridTexture);
        set(shader.getUniform("InvProjMat"), invProj);
        set(shader.getUniform("InvModelViewMat"), invView);
        Uniform camUniform = shader.getUniform("CamInGrid");
        if (camUniform != null) camUniform.set((float) (cam.x - gridOx), (float) (cam.y - gridOy), (float) (cam.z - gridOz));
        Uniform strength = shader.getUniform("Strength");
        if (strength != null) strength.set(LightingSettings.strength());
        Uniform range = shader.getUniform("EncodeRange");
        if (range != null) range.set(LightGridBuilder.ENCODE_RANGE);

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(() -> shader);

        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.addVertex(-1f, -1f, 0f);
        b.addVertex(1f, -1f, 0f);
        b.addVertex(1f, 1f, 0f);
        b.addVertex(-1f, 1f, 0f);
        BufferUploader.drawWithShader(b.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void set(Uniform u, Matrix4f m) {
        if (u != null) u.set(m);
    }

    private static void upload(Result r) {
        gridEmpty = r.empty();
        gridOx = r.ox();
        gridOy = r.oy();
        gridOz = r.oz();
        if (r.empty()) return;

        if (gridTexture == -1) {
            gridTexture = TextureUtil.generateTextureId();
            GlStateManager._bindTexture(gridTexture);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, ATLAS, ATLAS, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
        }
        if (uploadBuffer == null) uploadBuffer = MemoryUtil.memAlloc(ATLAS * ATLAS * 4);
        uploadBuffer.clear();
        uploadBuffer.put(r.atlas()).flip();
        GlStateManager._bindTexture(gridTexture);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, ATLAS, ATLAS, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, uploadBuffer);
    }

    // ---- Iris: shader packs do their own lighting, so we stay out of the way ----
    private static Boolean irisPresent;
    private static Method irisInUse;
    private static Object irisApi;

    private static boolean shaderPackActive() {
        if (irisPresent == null) {
            irisPresent = ModList.get().isLoaded("iris");
            if (irisPresent) {
                try {
                    Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    irisApi = api.getMethod("getInstance").invoke(null);
                    irisInUse = api.getMethod("isShaderPackInUse");
                } catch (Throwable t) {
                    irisPresent = false;
                }
            }
        }
        if (!irisPresent) return false;
        try {
            return (boolean) irisInUse.invoke(irisApi);
        } catch (Throwable t) {
            return false;
        }
    }
}