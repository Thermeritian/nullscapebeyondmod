package net.thmrite.nullscapebeyond.skybox;

import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/**
 * Registered skyboxes. Each one is a single equirectangular (2:1) PNG at
 * assets/nscapebeyond/textures/skybox/<id>.png
 * To add a new skybox, add a constant here and drop the PNG in that folder.
 */
public enum SkyboxType {
    LEVEL("level"),
    CELESTIAL("celestial");

    private final String id;
    private final ResourceLocation texture;

    SkyboxType(String id) {
        this.id = id;
        this.texture = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "textures/skybox/" + id + ".png");
    }

    public String id() {
        return id;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public static SkyboxType byId(String id) {
        for (SkyboxType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}