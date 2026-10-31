package net.thmrite.nullscapebeyond.effect.visual;

import net.thmrite.nullscapebeyond.shaders.lighting.api.LightColor;
import net.thmrite.nullscapebeyond.shaders.lighting.api.LightEffects;
import net.thmrite.nullscapebeyond.shaders.lighting.api.LightEmission;

public final class VoidLighting {
    public static final LightColor DARK_BLUE = LightColor.of(24, 44, 200);

    private static final float INFESTOR_RADIUS = 7.0F;
    private static final float VOIDBOUND_RADIUS = 6.0F;
    private static final float PULSE_PERIOD_TICKS = 60.0F;

    private VoidLighting() {}

    public static LightEmission infestor(long time) {
        return LightEmission.of(DARK_BLUE, INFESTOR_RADIUS, LightEffects.pulse(time, PULSE_PERIOD_TICKS, 0.75F, 1.0F));
    }

    public static LightEmission voidbound(long time) {
        return LightEmission.of(DARK_BLUE, VOIDBOUND_RADIUS, LightEffects.pulse(time, PULSE_PERIOD_TICKS, 0.65F, 0.9F));
    }
}