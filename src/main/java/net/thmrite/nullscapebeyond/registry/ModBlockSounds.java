package net.thmrite.nullscapebeyond.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.FootstepSounds;

public final class ModBlockSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, NullscapeBeyond.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TILE_WALK = register("block.tile.walk");
    public static final DeferredHolder<SoundEvent, SoundEvent> TILE_RUN = register("block.tile.run");
    public static final DeferredHolder<SoundEvent, SoundEvent> NULLSTONE_WALK = register("block.nullstone.walk");
    public static final DeferredHolder<SoundEvent, SoundEvent> NULLSTONE_RUN = register("block.nullstone.run");

    public static final FootstepSounds TILE = create(TILE_WALK, TILE_RUN);
    public static final FootstepSounds NULLSTONE = create(NULLSTONE_WALK, NULLSTONE_RUN);

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, name)));
    }

    private static FootstepSounds create(Supplier<SoundEvent> walk, Supplier<SoundEvent> run) {
        return new FootstepSounds(stoneLike(walk), stoneLike(run));
    }

    // Custom step sound; break/place/hit/fall stay vanilla stone.
    private static SoundType stoneLike(Supplier<SoundEvent> step) {
        return new DeferredSoundType(1.0F, 1.0F,
                () -> SoundEvents.STONE_BREAK, step, () -> SoundEvents.STONE_PLACE,
                () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_FALL);
    }

    private ModBlockSounds() {}
}