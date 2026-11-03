package net.thmrite.nullscapebeyond.client.classcontrol;

public final class ClassStamina {
    public static boolean isActive() {
        return ChargerController.hasClass ? ChargerController.isActive() : DiverController.isStaminaActive();
    }

    public static float value() {
        return ChargerController.hasClass ? ChargerController.stamina : DiverController.stamina;
    }

    public static float max() {
        return ChargerController.hasClass ? ChargerController.maxStamina : DiverController.maxStamina;
    }

    /** True while the class is spending stamina (the bar is white), false while it recovers (yellow). */
    public static boolean using() {
        return ChargerController.hasClass ? ChargerController.charging : DiverController.diving || DiverController.sliding;
    }

    private ClassStamina() {}
}
