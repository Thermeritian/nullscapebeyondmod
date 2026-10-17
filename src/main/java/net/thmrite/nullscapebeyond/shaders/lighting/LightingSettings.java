package net.thmrite.nullscapebeyond.shaders.lighting;

import net.thmrite.nullscapebeyond.client.ClientConfig;

/** Runtime switches for the lighting library. enabled/strength come from ClientConfig (Visual tab). */
public final class LightingSettings {
    private LightingSettings() {}

    /** Hard cap on simultaneous lights (nearest to the camera win). */
    public static volatile int maxSources = 384;
    /** Entities holding registered items (torch, lantern...) emit light. */
    public static volatile boolean heldItems = true;

    public static boolean enabled() {
        return ClientConfig.COLORED_LIGHTING.get();
    }

    public static float strength() {
        return ClientConfig.LIGHT_STRENGTH.get().floatValue();
    }
}