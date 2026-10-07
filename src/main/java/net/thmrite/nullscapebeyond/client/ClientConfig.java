package net.thmrite.nullscapebeyond.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PHOTOSENSITIVE_MODE;
    public static final ModConfigSpec.BooleanValue SHOW_FLASH_WARNING;
    public static final ModConfigSpec.DoubleValue SOUNDTRACK_VOLUME;
    public static final ModConfigSpec.BooleanValue MENU_SOUNDS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("accessibility");
        PHOTOSENSITIVE_MODE = BUILDER
                .comment("Reduces or removes flashing and rapid visual effects")
                .define("photosensitiveMode", false);
        SHOW_FLASH_WARNING = BUILDER
                .comment("Show the flashing-lights warning when the game starts")
                .define("showFlashWarning", true);
        BUILDER.pop();

        BUILDER.push("sound");
        SOUNDTRACK_VOLUME = BUILDER
                .comment("Volume of the mod's soundtrack (independent from Minecraft's Music slider)")
                .defineInRange("soundtrackVolume", 1.0, 0.0, 1.0);
        MENU_SOUNDS = BUILDER
                .comment("Use the mod's custom hover/click/return sounds in the main menu")
                .define("menuSounds", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}