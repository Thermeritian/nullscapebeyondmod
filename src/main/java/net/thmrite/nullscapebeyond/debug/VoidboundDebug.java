package net.thmrite.nullscapebeyond.debug;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.thmrite.nullscapebeyond.NullscapeBeyond;

/** Debug helper for the voidbound visuals. Set ENABLED to false (or delete the calls) when you are done. */
public final class VoidboundDebug {
    public static final boolean ENABLED = true;
    /** Paints the recoloured base texture flat magenta (keeps the model's shading). If you still see the original texture with this on, the layer is not drawing. */
    public static final boolean FORCE_MAGENTA = false;

    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

    private VoidboundDebug() {}

    public static void log(String message, Object... args) {
        if (ENABLED) NullscapeBeyond.LOGGER.info("[VoidboundDebug] " + message, args);
    }

    /** Logs only the first time this key is seen, so per-frame code does not spam the console. */
    public static void once(String key, String message, Object... args) {
        if (ENABLED && SEEN.add(key)) NullscapeBeyond.LOGGER.info("[VoidboundDebug] " + message, args);
    }
}