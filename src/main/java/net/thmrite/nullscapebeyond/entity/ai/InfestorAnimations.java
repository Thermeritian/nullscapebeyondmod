package net.thmrite.nullscapebeyond.entity.ai;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class InfestorAnimations {

    public static final String IDLE = "idle";
    public static final String WALK = "walk";
    public static final String LEAP = "leap";

    public static final int TRANSITION_TICKS = 3;

    private static final int LEAP_FALLBACK_TICKS = 14;
    private static int leapTicks = -1;

    private InfestorAnimations() {}

    public static int leapTicks() {
        if (leapTicks < 0) leapTicks = readLengthTicks(LEAP, LEAP_FALLBACK_TICKS);
        return leapTicks;
    }

    private static int readLengthTicks(String animation, int fallback) {
        String path = "/assets/" + NullscapeBeyond.MODID + "/animations/entity/infestor.animation.json";
        try (InputStream in = InfestorAnimations.class.getResourceAsStream(path)) {
            if (in == null) {
                NullscapeBeyond.LOGGER.warn("Could not find {} to read the leap length, using {} ticks", path, fallback);
                return fallback;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject anim = root.getAsJsonObject("animations").getAsJsonObject(animation);
            double seconds = anim.get("animation_length").getAsDouble();
            int ticks = Math.max(1, (int) Math.ceil(seconds * 20.0D));
            NullscapeBeyond.LOGGER.info("Infestor leap animation length: {}s = {} ticks", seconds, ticks);
            return ticks;
        } catch (Exception e) {
            NullscapeBeyond.LOGGER.warn("Could not read '{}' length from {} (missing animation_length?), using {} ticks",
                    animation, path, fallback, e);
            return fallback;
        }
    }
}