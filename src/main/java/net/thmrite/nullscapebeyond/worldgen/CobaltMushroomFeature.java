package net.thmrite.nullscapebeyond.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

/**
 * Tree-like cobalt mushroom: a 1-wide stem (4-6 blocks), a 5x5 cap (corners cut) with a 3x3 layer on top,
 * and small hanging roots under the cap. Used when a cobalt_mushroom grows (bone meal / random growth).
 */
public class CobaltMushroomFeature extends Feature<NoneFeatureConfiguration> {
    public CobaltMushroomFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int height = 4 + random.nextInt(3);
        int capY = origin.getY() + height;
        if (capY + 2 >= level.getMaxBuildHeight()) return false;
        BlockPos ground = origin.below();
        if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) return false;

        for (int y = 0; y < height; y++) {
            if (!canReplace(level, origin.above(y))) return false;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                if (!canReplace(level, new BlockPos(origin.getX() + dx, capY, origin.getZ() + dz))) return false;
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && !canReplace(level, new BlockPos(origin.getX() + dx, capY + 1, origin.getZ() + dz))) return false;
            }
        }

        BlockState stem = ModBlocks.COBALT_MUSHROOM_STEM.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState cap = ModBlocks.COBALT_MUSHROOM_BLOCK.get().defaultBlockState();
        BlockState roots = ModBlocks.COBALT_MUSHROOM_HANGING_ROOTS.get().defaultBlockState();

        for (int y = 0; y < height; y++) {
            setBlock(level, origin.above(y), stem);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                BlockPos capPos = new BlockPos(origin.getX() + dx, capY, origin.getZ() + dz);
                setBlock(level, capPos, cap);
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setBlock(level, capPos.above(), cap);
                }
                // small roots hanging under the cap (not where the stem is)
                if ((dx != 0 || dz != 0) && random.nextFloat() < 0.3F) {
                    BlockPos below = capPos.below();
                    if (level.getBlockState(below).isAir()) {
                        setBlock(level, below, roots);
                    }
                }
            }
        }
        return true;
    }
}
