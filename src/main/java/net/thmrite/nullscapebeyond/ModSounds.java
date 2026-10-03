package net.thmrite.nullscapebeyond;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, NullscapeBeyond.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_HOVER = register("menu.hover");
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_CLICK = register("menu.click");
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_RETURN = register("menu.return");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, name)));
    }
}