package net.thmrite.nullscapebeyond.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thmrite.nullscapebeyond.registry.ModParticles;

public class ViridianLeavesResinBlock extends LeavesBlock {
    public ViridianLeavesResinBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(12) != 0) return;
        BlockPos below = pos.below();
        if (level.getBlockState(below).canOcclude()) return;   // nothing to fall through
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        double y = pos.getY() - 0.05;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        level.addParticle(ModParticles.VIRIDIAN_RESIN_DRIP.get(), x, y, z, 0.0, 0.0, 0.0);
    }
}
