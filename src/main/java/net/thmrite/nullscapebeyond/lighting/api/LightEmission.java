package net.thmrite.nullscapebeyond.lighting.api;

import java.util.Objects;

import net.minecraft.util.Mth;

/**
 * Describes one light: colour, reach in blocks and strength.
 * Intensity 1.0 is "normal"; values up to {@link #MAX_INTENSITY} give an over-bright light.
 */
public record LightEmission(LightColor color, float radius, float intensity) {

    public static final float MAX_RADIUS = 24f;
    public static final float MAX_INTENSITY = 2f;

    public LightEmission {
        Objects.requireNonNull(color, "color");
        radius = Mth.clamp(radius, 0f, MAX_RADIUS);
        intensity = Mth.clamp(intensity, 0f, MAX_INTENSITY);
    }

    public static LightEmission of(LightColor color, float radius) {
        return new LightEmission(color, radius, 1f);
    }

    public static LightEmission of(LightColor color, float radius, float intensity) {
        return new LightEmission(color, radius, intensity);
    }

    public LightEmission withIntensity(float newIntensity) {
        return new LightEmission(color, radius, newIntensity);
    }

    public LightEmission withRadius(float newRadius) {
        return new LightEmission(color, newRadius, intensity);
    }

    public boolean isActive() {
        return radius > 0.5f && intensity > 0.01f;
    }
}