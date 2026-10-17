package net.thmrite.nullscapebeyond.shaders.emissive;

import net.thmrite.nullscapebeyond.client.ClientConfig;

/** Single switch for the whole emissive system (Visual settings). */
public final class EmissiveSettings {
    private EmissiveSettings() {}

    public static boolean enabled() {
        return ClientConfig.EMISSIVE_TEXTURES.get();
    }
}