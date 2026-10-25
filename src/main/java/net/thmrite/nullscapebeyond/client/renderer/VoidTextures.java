package net.thmrite.nullscapebeyond.client.renderer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.thmrite.nullscapebeyond.NullscapeBeyond;


public final class VoidTextures {
    public record Entry(ResourceLocation base, ResourceLocation glow) {}

    private static final Map<ResourceLocation, Entry> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> FAILED = new HashSet<>();

    // luminance stop, r, g, b
    private static final float[][] RAMP = {
            {0.00F, 6, 4, 20},
            {0.35F, 22, 20, 92},
            {0.70F, 104, 36, 168},
            {1.00F, 190, 150, 255},
    };
    private static final float GLOW_START = 0.80F;
    private static final float GLOW_END = 0.95F;

    private VoidTextures() {}

    public static Entry get(ResourceLocation source) {
        Entry cached = CACHE.get(source);
        if (cached != null) return cached;
        if (FAILED.contains(source)) return null;

        Minecraft mc = Minecraft.getInstance();
        try (InputStream in = mc.getResourceManager().open(source); NativeImage src = NativeImage.read(in)) {
            NativeImage base = new NativeImage(src.getWidth(), src.getHeight(), false);
            NativeImage glow = new NativeImage(src.getWidth(), src.getHeight(), false);
            recolor(src, base, glow);

            String path = "voidbound/" + source.getNamespace() + "/" + source.getPath().replace(".png", "");
            ResourceLocation baseId = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, path + "/base");
            ResourceLocation glowId = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, path + "/glow");
            TextureManager tm = mc.getTextureManager();
            tm.register(baseId, new DynamicTexture(base));
            tm.register(glowId, new DynamicTexture(glow));

            Entry entry = new Entry(baseId, glowId);
            CACHE.put(source, entry);
            return entry;
        } catch (Exception e) {
            NullscapeBeyond.LOGGER.warn("Could not build voidbound texture for {}", source, e);
            FAILED.add(source);
            return null;
        }
    }

    public static void clear() {
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        for (Entry e : CACHE.values()) {
            tm.release(e.base());
            tm.release(e.glow());
        }
        CACHE.clear();
        FAILED.clear();
    }

    private static void recolor(NativeImage src, NativeImage base, NativeImage glow) {
        int w = src.getWidth(), h = src.getHeight();
        // pass 1: luminance range, so dark textures (zombie, spider...) still get the full ramp
        float min = 1.0F, max = 0.0F;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = src.getPixelRGBA(x, y);
                if ((px >>> 24) == 0) continue;
                float l = luminance(px);
                min = Math.min(min, l);
                max = Math.max(max, l);
            }
        }
        float range = Math.max(0.001F, max - min);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = src.getPixelRGBA(x, y); // ABGR
                int a = px >>> 24;
                if (a == 0) {
                    base.setPixelRGBA(x, y, 0);
                    glow.setPixelRGBA(x, y, 0);
                    continue;
                }
                float l = Mth.clamp((luminance(px) - min) / range, 0.0F, 1.0F);
                int[] c = ramp(l);
                base.setPixelRGBA(x, y, (a << 24) | (c[2] << 16) | (c[1] << 8) | c[0]);

                float hi = smoothstep(GLOW_START, GLOW_END, l);
                int ga = Math.round(hi * a);
                glow.setPixelRGBA(x, y, (ga << 24) | 0x00FFFFFF);
            }
        }
    }

    private static float luminance(int abgr) {
        float r = (abgr & 255) / 255.0F, g = ((abgr >> 8) & 255) / 255.0F, b = ((abgr >> 16) & 255) / 255.0F;
        return 0.299F * r + 0.587F * g + 0.114F * b;
    }

    private static int[] ramp(float l) {
        for (int i = 1; i < RAMP.length; i++) {
            if (l <= RAMP[i][0]) {
                float t = (l - RAMP[i - 1][0]) / (RAMP[i][0] - RAMP[i - 1][0]);
                return new int[]{
                        Math.round(Mth.lerp(t, RAMP[i - 1][1], RAMP[i][1])),
                        Math.round(Mth.lerp(t, RAMP[i - 1][2], RAMP[i][2])),
                        Math.round(Mth.lerp(t, RAMP[i - 1][3], RAMP[i][3]))};
            }
        }
        float[] last = RAMP[RAMP.length - 1];
        return new int[]{(int) last[1], (int) last[2], (int) last[3]};
    }

    private static float smoothstep(float e0, float e1, float x) {
        float t = Mth.clamp((x - e0) / (e1 - e0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}