package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NullscapeBeyond.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ACCESSORIES =
            TABS.register("accessories", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nscapebeyond.accessories"))
                    .icon(() -> new ItemStack(ModItems.ORB.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.ORB.get());
                        output.accept(ModItems.PROPELLER_HAT.get());
                        output.accept(ModItems.SHARK_TAIL.get());
                        output.accept(ModItems.NINJA_BELT.get());

                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CLASS_ACCESSORIES =
            TABS.register("class_accessories", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nscapebeyond.class_accessories"))
                    .icon(() -> new ItemStack(ModItems.DIVER_HELMET.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DIVER_HELMET.get());
                        output.accept(ModItems.CHARGER_BOOTS.get());
                        output.accept(ModItems.GLIDER_KIT.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPAWN_EGGS =
            TABS.register("spawn_eggs", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nscapebeyond.spawn_eggs"))
                    .icon(() -> new ItemStack(ModItems.MART_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.MART_SPAWN_EGG.get());
                        output.accept(ModItems.INFESTOR_SPAWN_EGG.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS =
            TABS.register("blocks", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nscapebeyond.blocks"))
                    .icon(() -> new ItemStack(ModBlocks.TILE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.NULLSTONE.get());
                        output.accept(ModBlocks.NULLSTONE_SLAB.get());
                        output.accept(ModBlocks.NULLSTONE_STAIRS.get());
                        output.accept(ModBlocks.TILE.get());
                        output.accept(ModBlocks.TILE_SLAB.get());
                        output.accept(ModBlocks.TILE_STAIRS.get());
                        output.accept(ModBlocks.SPAWN.get());
                    })
                    .build());
}
