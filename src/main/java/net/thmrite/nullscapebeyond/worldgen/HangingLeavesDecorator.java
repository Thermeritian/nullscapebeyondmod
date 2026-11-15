package net.thmrite.nullscapebeyond.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.thmrite.nullscapebeyond.registry.ModTreeDecorators;

/**
 * Hangs strands (e.g. azure_leaves_hanging) below the tree's leaves.
 * JSON: {"type":"nscapebeyond:hanging_leaves","probability":0.2,"max_length":3,"provider":{...}}
 */
public class HangingLeavesDecorator extends TreeDecorator {
    public static final MapCodec<HangingLeavesDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability),
            Codec.intRange(1, 8).fieldOf("max_length").forGetter(d -> d.maxLength),
            BlockStateProvider.CODEC.fieldOf("provider").forGetter(d -> d.provider)
    ).apply(instance, HangingLeavesDecorator::new));

    private final float probability;
    private final int maxLength;
    private final BlockStateProvider provider;

    public HangingLeavesDecorator(float probability, int maxLength, BlockStateProvider provider) {
        this.probability = probability;
        this.maxLength = maxLength;
        this.provider = provider;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.HANGING_LEAVES.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        for (BlockPos leaf : context.leaves()) {
            if (random.nextFloat() >= probability) continue;
            int length = 1 + random.nextInt(maxLength);
            BlockPos pos = leaf.below();
            for (int i = 0; i < length && context.isAir(pos); i++) {
                context.setBlock(pos, provider.getState(random, pos));
                pos = pos.below();
            }
        }
    }
}
