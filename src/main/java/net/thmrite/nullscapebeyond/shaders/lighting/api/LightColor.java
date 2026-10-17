package net.thmrite.nullscapebeyond.shaders.lighting.api;

import net.minecraft.util.Mth;

/** Linear RGB light colour, each channel 0..1. */
public record LightColor(float r, float g, float b) {

    public static final LightColor WHITE = new LightColor(1f, 1f, 1f);
    public static final LightColor TORCH = of(255, 160, 70);
    public static final LightColor SOUL = of(70, 190, 255);

    public LightColor {
        r = Mth.clamp(r, 0f, 1f);
        g = Mth.clamp(g, 0f, 1f);
        b = Mth.clamp(b, 0f, 1f);
    }

    public static LightColor of(int r, int g, int b) {
        return new LightColor(r / 255f, g / 255f, b / 255f);
    }

    public static LightColor fromRgb(int rgb) {
        return of((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public LightColor lerp(LightColor other, float t) {
        return new LightColor(Mth.lerp(t, r, other.r), Mth.lerp(t, g, other.g), Mth.lerp(t, b, other.b));
    }

    public LightColor scale(float factor) {
        return new LightColor(r * factor, g * factor, b * factor);
    }
}