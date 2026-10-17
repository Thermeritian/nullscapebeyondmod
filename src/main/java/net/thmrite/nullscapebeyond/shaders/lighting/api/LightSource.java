package net.thmrite.nullscapebeyond.shaders.lighting.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;

/**
 * Implement on a {@code BlockEntity} or an {@code Entity} to make it emit coloured light.
 * Polled on the client every tick, so it may change over time (flicker, pulse, charge-up...).
 * Light is positioned at the centre of the block (block entities) or of the bounding box (entities),
 * plus {@link #getLightOffset()}.
 */
public interface LightSource {

    /** Current emission, or {@code null} / an inactive emission when switched off. */
    @Nullable
    LightEmission getLightEmission();

    default Vec3 getLightOffset() {
        return Vec3.ZERO;
    }
}