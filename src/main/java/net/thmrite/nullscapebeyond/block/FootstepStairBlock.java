package net.thmrite.nullscapebeyond.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FootstepStairBlock extends StairBlock {
    private final FootstepSounds sounds;

    public FootstepStairBlock(BlockState baseState, Properties properties, FootstepSounds sounds) {
        super(baseState, properties.sound(sounds.walk()));
        this.sounds = sounds;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return sounds.forEntity(entity);
    }
}