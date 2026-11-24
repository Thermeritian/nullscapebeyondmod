package net.thmrite.nullscapebeyond.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface NullflameCombustion {
    boolean nscapebeyond$isNullflame();

    /** Server tick, before vanilla runs. */
    void nscapebeyond$before();

    /** Server tick, after vanilla ran. */
    void nscapebeyond$after(Level level, BlockPos pos);
}
