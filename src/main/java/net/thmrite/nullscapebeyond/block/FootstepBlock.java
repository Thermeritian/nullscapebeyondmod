package net.thmrite.nullscapebeyond.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class FootstepBlock extends Block {
    private final FootstepSounds sounds;

    public FootstepBlock(Properties properties, FootstepSounds sounds) {
        super(properties.sound(sounds.walk()));
        this.sounds = sounds;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return sounds.forEntity(entity);
    }
}