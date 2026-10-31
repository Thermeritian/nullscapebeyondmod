package net.thmrite.nullscapebeyond.client.renderer.effect;

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
import net.thmrite.nullscapebeyond.debug.VoidboundDebug;

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
    /** At most this share of a texture's pixels (the brightest ones) become glowing highlights. */
    private static final float MAX_GLOW_FRACTION = 0.10F;

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
            VoidboundDebug.log("VoidTextures: built {} ({}x{}) -> {} / {}", source, src.getWidth(), src.getHeight(), baseId, glowId);
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
        int opaque = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = src.getPixelRGBA(x, y);
                if ((px >>> 24) == 0) continue;
                float l = luminance(px);
                min = Math.min(min, l);
                max = Math.max(max, l);
                opaque++;
            }
        }
        float range = Math.max(0.001F, max - min);

        // pass 2: histogram of the stretched brightness -> find the cut that keeps at most MAX_GLOW_FRACTION
        // of the pixels as highlights. A mostly-white mob (chicken) therefore never turns into one big glow.
        int[] hist = new int[256];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = src.getPixelRGBA(x, y);
                if ((px >>> 24) == 0) continue;
                hist[bin((luminance(px) - min) / range)]++;
            }
        }
        int cut = 256; // 256 = nothing glows
        int cumulative = 0;
        for (int b = 255; b >= 0; b--) {
            if (cumulative + hist[b] > MAX_GLOW_FRACTION * opaque) break;
            cumulative += hist[b];
            cut = b;
        }

        // pass 3: write the textures
        int glowPixels = 0;
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
                boolean highlight = bin(l) >= cut;

                int[] c = VoidboundDebug.FORCE_MAGENTA ? new int[]{255, 0, 255} : ramp(l);
                if (highlight) {
                    // highlight pixels go dark in the base: the glow layer fully defines their colour
                    c = new int[]{0, 0, 0};
                    glow.setPixelRGBA(x, y, (a << 24) | 0x00FFFFFF);
                    glowPixels++;
                } else {
                    glow.setPixelRGBA(x, y, 0);
                }
                base.setPixelRGBA(x, y, (a << 24) | (c[2] << 16) | (c[1] << 8) | c[0]);
            }
        }
        VoidboundDebug.log("VoidTextures: luminance min={} max={}, glow pixels={}/{} (cut bin {})",
                min, max, glowPixels, opaque, cut);
    }

    private static int bin(float stretched) {
        return Mth.clamp((int) (stretched * 255.0F + 0.5F), 0, 255);
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