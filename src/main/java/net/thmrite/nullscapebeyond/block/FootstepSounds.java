package net.thmrite.nullscapebeyond.block;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;

public record FootstepSounds(SoundType walk, SoundType run) {
    public SoundType forEntity(@Nullable Entity entity) {
        return entity != null && entity.isSprinting() ? run : walk;
    }
}