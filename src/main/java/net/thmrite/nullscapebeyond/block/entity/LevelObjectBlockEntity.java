package net.thmrite.nullscapebeyond.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock;
import net.thmrite.nullscapebeyond.registry.ModBlockEntities;

public class LevelObjectBlockEntity extends BlockEntity {
    public enum Phase { IDLE, ARMED, EXPLODING }

    private Phase phase = Phase.IDLE;
    private long phaseStart;

    // Client-only idle animation state (never saved or synced).
    public boolean animInit;
    public int animFrame;
    public float nextFrameSwitch;

    public LevelObjectBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVEL_OBJECT.get(), pos, state);
    }

    public Phase getPhase() { return phase; }
    public long getPhaseStart() { return phaseStart; }

    public void setPhase(Phase phase, long gameTime) {
        this.phase = phase;
        this.phaseStart = gameTime;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void reset() {
        setPhase(Phase.IDLE, 0L);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("Phase", (byte) phase.ordinal());
        tag.putLong("PhaseStart", phaseStart);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        phase = Phase.values()[Mth.clamp(tag.getByte("Phase"), 0, Phase.values().length - 1)];
        phaseStart = tag.getLong("PhaseStart");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}