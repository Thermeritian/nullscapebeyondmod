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

    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_HOVER = register("menu.hover");
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_CLICK = register("menu.click");
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_RETURN = register("menu.return");

    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_JUMP = register("generic_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_AIRJUMP  = register("generic_extrajump");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERIC_LAND   = register("generic_land");

    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGER_CHARGE = register("charger_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGER_BRAKE  = register("charger_brake");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGER_BONK   = register("charger_bonk");


    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, name)));
    }
}