package net.thmrite.nullscapebeyond.event;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock;

public class LevelObjectCollectedEvent extends Event {
    private final ServerLevel level;
    private final BlockPos pos;
    private final LevelObjectBlock.Mode mode;
    @Nullable private final ServerPlayer collector;

    public LevelObjectCollectedEvent(ServerLevel level, BlockPos pos, LevelObjectBlock.Mode mode,
                                     @Nullable ServerPlayer collector) {
        this.level = level;
        this.pos = pos;
        this.mode = mode;
        this.collector = collector;
    }

    public ServerLevel getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public LevelObjectBlock.Mode getMode() { return mode; }
    @Nullable public ServerPlayer getCollector() { return collector; }
}