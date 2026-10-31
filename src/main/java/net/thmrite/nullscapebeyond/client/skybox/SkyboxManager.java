package net.thmrite.nullscapebeyond.client.skybox;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.skybox.SkyboxPayload;
import net.thmrite.nullscapebeyond.skybox.SkyboxType;

public final class SkyboxManager {
    private static SkyboxType current;

    // index 0 = yaw (left/right), 1 = pitch (up/down), 2 = roll (clockwise/counterclockwise)
    private static final float[] speeds = new float[3]; // degrees per second
    private static final float[] angles = new float[3]; // degrees

    // index 0 = R, 1 = G, 2 = B; period in seconds, 0 = not pulsing
    private static final float[] pulsePeriod = new float[3];
    private static final long[] pulseStartMs = new long[3];

    private static long lastNanos;

    private SkyboxManager() {}

    public static void apply(SkyboxPayload p) {
        switch (p.op()) {
            case SET -> {
                SkyboxType t = SkyboxType.byId(p.skybox());
                if (t != null) current = t;
            }
            case CLEAR -> {
                current = null;
                resetEffects();
            }
            case MOTION -> {
                float v = p.value();
                switch (SkyboxPayload.Direction.values()[p.axis()]) {
                    case LEFT -> speeds[0] = v;
                    case RIGHT -> speeds[0] = -v;
                    case UP -> speeds[1] = v;
                    case DOWN -> speeds[1] = -v;
                    case COUNTERCLOCKWISE -> speeds[2] = v;
                    case CLOCKWISE -> speeds[2] = -v;
                }
            }
            case PULSE -> {
                int ch = p.axis();
                if (ch >= 0 && ch < 3) {
                    pulsePeriod[ch] = Math.max(p.value(), 0.1f);
                    pulseStartMs[ch] = Util.getMillis();
                }
            }
            case EFFECT_CLEAR -> resetEffects();
        }
    }

    public static void reset() {
        current = null;
        resetEffects();
    }

    private static void resetEffects() {
        for (int i = 0; i < 3; i++) {
            speeds[i] = 0f;
            angles[i] = 0f;
            pulsePeriod[i] = 0f;
        }
    }

    public static ResourceLocation texture() {
        return current == null ? null : current.texture();
    }

    public static void advance() {
        long now = Util.getNanos();
        float dt = lastNanos == 0 ? 0f : Math.min((now - lastNanos) / 1.0e9f, 0.1f);
        lastNanos = now;
        for (int i = 0; i < 3; i++) {
            angles[i] = (angles[i] + speeds[i] * dt) % 360f;
        }
    }

    public static float yaw() { return angles[0]; }
    public static float pitch() { return angles[1]; }
    public static float roll() { return angles[2]; }

    public static float[] tint() {
        float[] rgb = {1f, 1f, 1f};
        boolean safe = photosensitiveMode();
        for (int i = 0; i < 3; i++) {
            float period = pulsePeriod[i];
            if (period <= 0f) continue;
            if (safe) period = Math.max(period, 4f); // keep well under 3 flashes per second
            double t = (Util.getMillis() - pulseStartMs[i]) / 1000.0;
            float wave = 0.5f + 0.5f * (float) Math.cos(2.0 * Math.PI * t / period); // 1 -> 0 -> 1
            float low = safe ? 0.75f : 0f; // softer swing in photosensitive mode
            rgb[i] = low + (1f - low) * wave;
        }
        return rgb;
    }

    private static boolean photosensitiveMode() {
        return false;
    }
}