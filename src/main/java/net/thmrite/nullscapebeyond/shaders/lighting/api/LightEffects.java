package net.thmrite.nullscapebeyond.shaders.lighting.api;

import net.minecraft.util.Mth;

/** Small helpers to animate {@link LightEmission#intensity()}. Return multipliers in 0..1. */
public final class LightEffects {
    private LightEffects() {}

    /** Organic candle-like flicker. {@code amount} 0 = steady, 1 = deep flicker. */
    public static float flicker(float seed, long gameTime, float amount) {
        float t = gameTime * 0.35f + seed * 13.37f;
        float n = 0.5f + 0.25f * Mth.sin(t) + 0.15f * Mth.sin(t * 2.7f + 1.3f) + 0.10f * Mth.sin(t * 6.1f + 4.1f);
        return 1f - amount * (1f - Mth.clamp(n, 0f, 1f));
    }

    /** Smooth sine pulse between {@code min} and {@code max}. */
    public static float pulse(long gameTime, float periodTicks, float min, float max) {
        float s = 0.5f + 0.5f * Mth.sin((float) (gameTime * (Math.PI * 2.0) / periodTicks));
        return Mth.lerp(s, min, max);
    }
}