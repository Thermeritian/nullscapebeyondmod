package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.worldgen.tree.HangingLeavesDecorator;
import net.thmrite.nullscapebeyond.worldgen.tree.HangingResinDecorator;

public final class ModTreeDecorators {
    public static final DeferredRegister<TreeDecoratorType<?>> DECORATORS = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, NullscapeBeyond.MODID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<HangingLeavesDecorator>> HANGING_LEAVES =
            DECORATORS.register("hanging_leaves", () -> new TreeDecoratorType<>(HangingLeavesDecorator.CODEC));
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<HangingResinDecorator>> HANGING_RESIN =
            DECORATORS.register("hanging_resin", () -> new TreeDecoratorType<>(HangingResinDecorator.CODEC));
}
