package net.thmrite.nullscapebeyond.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PHOTOSENSITIVE_MODE;
    public static final ModConfigSpec.BooleanValue SHOW_FLASH_WARNING;
    public static final ModConfigSpec.DoubleValue SOUNDTRACK_VOLUME;
    public static final ModConfigSpec.BooleanValue MENU_SOUNDS;
    public static final ModConfigSpec.BooleanValue CUSTOM_ACCESSORY_TOOLTIP;
    public static final ModConfigSpec.BooleanValue COLORED_LIGHTING;
    public static final ModConfigSpec.DoubleValue LIGHT_STRENGTH;
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

        BUILDER.push("visual");
        CUSTOM_ACCESSORY_TOOLTIP = BUILDER
                .comment("Use the mod's custom accessory tooltip instead of the vanilla one")
                .define("customAccessoryTooltip", true);
        COLORED_LIGHTING = BUILDER
                .comment("Enable the mod's dynamic coloured lighting")
                .define("coloredLighting", true);
        LIGHT_STRENGTH = BUILDER
                .comment("How strongly coloured light is applied (1.0 = default)")
                .defineInRange("lightStrength", 1.0, 0.0, 2.0);
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