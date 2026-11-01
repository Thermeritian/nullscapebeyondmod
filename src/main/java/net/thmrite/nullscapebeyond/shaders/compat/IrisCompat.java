package net.thmrite.nullscapebeyond.shaders.compat;

import java.lang.reflect.Method;

import net.neoforged.fml.ModList;


public final class IrisCompat {
    private IrisCompat() {}

    private static boolean resolved;
    private static Object api;
    private static Method inUse;
    private static Method shadowPass;
    private static long nextCheck;
    private static boolean cached;

    /** True while an Iris shader pack is actually rendering the world. */
    public static boolean isShaderPackActive() {
        long now = System.nanoTime();
        if (now < nextCheck) return cached;
        nextCheck = now + 250_000_000L; // re-check 4x per second
        cached = query();
        return cached;
    }

    /** True while Iris renders the shadow map (we must not draw our own geometry into it). */
    public static boolean isRenderingShadowPass() {
        if (!isShaderPackActive() || shadowPass == null) return false;
        try {
            return (boolean) shadowPass.invoke(api);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean query() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded("iris")) {
                try {
                    Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    api = apiClass.getMethod("getInstance").invoke(null);
                    inUse = apiClass.getMethod("isShaderPackInUse");
                    shadowPass = apiClass.getMethod("isRenderingShadowPass");
                } catch (Throwable t) {
                    api = null;
                    inUse = null;
                }
            }
        }
        if (inUse == null) return false;
        try {
            return (boolean) inUse.invoke(api);
        } catch (Throwable t) {
            return false;
        }
    }
}