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

    /** Multiplier for horizontal steering while airborne. */
    public static final DeferredHolder<Attribute, Attribute> AIR_CONTROL =
            register("air_control", 1.0, 0.0, 8.0);

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