package net.thmrite.nullscapebeyond.movement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/**
 * Every tunable movement number lives here as an Attribute, so accessories can grant
 * them through attribute modifiers and /attribute can tweak them live while testing.
 *
 * Vanilla 1.21.1 already provides GRAVITY, JUMP_STRENGTH, MOVEMENT_SPEED, STEP_HEIGHT
 * and SAFE_FALL_DISTANCE (see net.minecraft.world.entity.ai.attributes.Attributes).
 */
public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, NullscapeBeyond.MODID);

    /** How many extra jumps the player can do while airborne. */
    public static final DeferredHolder<Attribute, Attribute> MAX_AIR_JUMPS =
            register("max_air_jumps", 0.0, 0.0, 16.0);

    /** Upward velocity (blocks/tick) set by an air jump. Vanilla jump is ~0.42. */
    public static final DeferredHolder<Attribute, Attribute> AIR_JUMP_STRENGTH =
            register("air_jump_strength", 0.42, 0.0, 4.0);

    /** Dash impulse (blocks/tick). 0 = dash unavailable. */
    public static final DeferredHolder<Attribute, Attribute> DASH_STRENGTH =
            register("dash_strength", 0.0, 0.0, 16.0);

    /** Dash cooldown in ticks (20 ticks = 1 second). */
    public static final DeferredHolder<Attribute, Attribute> DASH_COOLDOWN =
            register("dash_cooldown", 20.0, 0.0, 1200.0);

    /**
     * Multiplier on vanilla's airborne horizontal acceleration (1.0 = vanilla, 0 = no steering).
     * Applied passively by AirControlController, not by an ability.
     */
    public static final DeferredHolder<Attribute, Attribute> AIR_CONTROL =
            register("air_control", 1.0, 0.0, 8.0);

    /**
     * Top speed reachable by running alone, in blocks/second. Default 4.317 = vanilla walking.
     * Vanilla modifiers (sprint, speed/slowness effects) still scale it. It does NOT limit speed
     * that comes from other sources (dash, knockback, sprint-jump momentum).
     */
    public static final DeferredHolder<Attribute, Attribute> GROUND_MAX_SPEED =
            register("ground_max_speed", 4.317, 0.0, 200.0);

    /**
     * How fast running ramps up to full speed, as a multiplier on vanilla's ramp rate
     * (1.0 = vanilla, 2.0 = twice as fast, 0.5 = sluggish, very high = instant).
     */
    public static final DeferredHolder<Attribute, Attribute> GROUND_ACCELERATION =
            register("ground_acceleration", 1.0, 0.0, 100.0);

    /**
     * How fast momentum that doesn't match the input direction is cancelled/redirected on the
     * ground, as a multiplier on vanilla (1.0 = vanilla). Higher = sharper turns and 180s.
     */
    public static final DeferredHolder<Attribute, Attribute> GROUND_TURN_SPEED =
            register("ground_turn_speed", 1.0, 0.0, 100.0);

    private ModAttributes() {}

    private static DeferredHolder<Attribute, Attribute> register(String name, double def, double min, double max) {
        return ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + NullscapeBeyond.MODID + "." + name, def, min, max)
                        .setSyncable(true));
    }

    /** Call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(ModAttributes::onAttributeModify);
    }

    /** Attaches every custom attribute to players. */
    private static void onAttributeModify(EntityAttributeModificationEvent event) {
        for (var holder : ATTRIBUTES.getEntries()) {
            event.add(EntityType.PLAYER, holder);
        }
    }
}