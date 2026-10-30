package net.thmrite.nullscapebeyond.debug;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class VoidboundDebug {
    public static final boolean ENABLED = false;
    public static final boolean FORCE_MAGENTA = false;

    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

    private VoidboundDebug() {}

    public static void log(String message, Object... args) {
        if (ENABLED) NullscapeBeyond.LOGGER.info("[VoidboundDebug] " + message, args);
    }

    public static void once(String key, String message, Object... args) {
        if (ENABLED && SEEN.add(key)) NullscapeBeyond.LOGGER.info("[VoidboundDebug] " + message, args);
    }
}