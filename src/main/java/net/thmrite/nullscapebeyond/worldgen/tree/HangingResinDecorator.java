package net.thmrite.nullscapebeyond.worldgen.tree;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.thmrite.nullscapebeyond.block.ViridianResinHangingBlock;
import net.thmrite.nullscapebeyond.registry.ModTreeDecorators;

public class HangingResinDecorator extends TreeDecorator {
    public static final MapCodec<HangingResinDecorator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability),
            Codec.intRange(1, 32).fieldOf("min_length").forGetter(d -> d.minLength),
            Codec.intRange(1, 32).fieldOf("max_length").forGetter(d -> d.maxLength),
            BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(d -> d.rootProvider),
            BlockStateProvider.CODEC.fieldOf("resin_provider").forGetter(d -> d.resinProvider)
    ).apply(i, HangingResinDecorator::new));

    private final float probability;
    private final int minLength;
    private final int maxLength;
    private final BlockStateProvider rootProvider;
    private final BlockStateProvider resinProvider;

    public HangingResinDecorator(float probability, int minLength, int maxLength, BlockStateProvider rootProvider, BlockStateProvider resinProvider) {
        this.probability = probability;
        this.minLength = Math.min(minLength, maxLength);
        this.maxLength = Math.max(minLength, maxLength);
        this.rootProvider = rootProvider;
        this.resinProvider = resinProvider;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.HANGING_RESIN.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        for (BlockPos leaf : context.leaves()) {
            if (random.nextFloat() >= probability) continue;
            int length = minLength + random.nextInt(maxLength - minLength + 1);
            List<BlockPos> strand = new ArrayList<>();
            BlockPos pos = leaf.below();
            while (strand.size() < length && context.isAir(pos)) {
                strand.add(pos);
                pos = pos.below();
            }
            if (strand.isEmpty()) continue;
            context.setBlock(leaf, rootProvider.getState(random, leaf));
            for (int i = 0; i < strand.size(); i++) {
                BlockPos p = strand.get(i);
                BlockState state = resinProvider.getState(random, p);
                if (state.hasProperty(ViridianResinHangingBlock.PART)) {
                    ViridianResinHangingBlock.Part part = strand.size() == 1 || i == strand.size() - 1
                            ? ViridianResinHangingBlock.Part.BOTTOM
                            : (i == 0 ? ViridianResinHangingBlock.Part.TOP : ViridianResinHangingBlock.Part.MIDDLE);
                    state = state.setValue(ViridianResinHangingBlock.PART, part);
                }
                context.setBlock(p, state);
            }
        }
    }
}
