package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, NullscapeBeyond.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SOUNDTRACK_MENU = register("soundtrack.menu");


    //MENU
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_HOVER = register("menu.hover");
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_CLICK = register("menu.click");
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_RETURN = register("menu.return");

    //GENERIC MOVEMENT
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_JUMP = register("generic_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_AIRJUMP  = register("generic_extrajump");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_LAND   = register("generic_land");

    //CHARGER
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGER_CHARGE = register("charger_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGER_BRAKE  = register("charger_brake");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGER_BONK   = register("charger_bonk");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BASH    = register("shield_bash");

    //ENTITIES
    //MART
    public static final DeferredHolder<SoundEvent, SoundEvent> MART_MERGE = register("entity.mart.merge");
    public static final DeferredHolder<SoundEvent, SoundEvent> MART_DEATH = register("entity.mart.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MART_SHOOT = register("entity.mart.shoot");   // 3 variants in sounds.json

    //PROJECTILES
    // WATER JOLT
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_JOLT_HIT = register("entity.water_jolt.hit");

    //BEACON
    public static final DeferredHolder<SoundEvent, SoundEvent> BEACON_ENTER = register("entity.beacon.enter");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEACON_IDLE = register("entity.beacon.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEACON_SPAWN = register("entity.beacon.spawn");


    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, name)));
    }
}