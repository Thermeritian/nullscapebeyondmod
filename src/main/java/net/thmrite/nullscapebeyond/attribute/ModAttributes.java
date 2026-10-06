package net.thmrite.nullscapebeyond.attribute;

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

    public static final DeferredHolder<Attribute, Attribute> MAX_AIR_JUMPS =
            register("max_air_jumps", 0.0, 0.0, 16.0);

    public static final DeferredHolder<Attribute, Attribute> AIR_JUMP_STRENGTH =
            register("air_jump_strength", 0.42, 0.0, 4.0);

    public static final DeferredHolder<Attribute, Attribute> DASH_STRENGTH =
            register("dash_strength", 0.0, 0.0, 16.0);

    public static final DeferredHolder<Attribute, Attribute> DASH_COOLDOWN =
            register("dash_cooldown", 20.0, 0.0, 1200.0);
    public static final DeferredHolder<Attribute, Attribute> AIR_CONTROL =
            register("air_control", 1.0, 0.0, 8.0);
    public static final DeferredHolder<Attribute, Attribute> GROUND_MAX_SPEED =
            register("ground_max_speed", 4.317, 0.0, 200.0);

    public static final DeferredHolder<Attribute, Attribute> GROUND_ACCELERATION =
            register("ground_acceleration", 1.0, 0.0, 100.0);

    public static final DeferredHolder<Attribute, Attribute> GROUND_TURN_SPEED =
            register("ground_turn_speed", 1.0, 0.0, 100.0);

    private ModAttributes() {}

    private static DeferredHolder<Attribute, Attribute> register(String name, double def, double min, double max) {
        return ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + NullscapeBeyond.MODID + "." + name, def, min, max)
                        .setSyncable(true));
    }
    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(ModAttributes::onAttributeModify);
    }
    private static void onAttributeModify(EntityAttributeModificationEvent event) {
        for (var holder : ATTRIBUTES.getEntries()) {
            event.add(EntityType.PLAYER, holder);
        }
    }
}