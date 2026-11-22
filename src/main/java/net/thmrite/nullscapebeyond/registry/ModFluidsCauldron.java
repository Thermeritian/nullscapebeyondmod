package net.thmrite.nullscapebeyond.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ModFluidsCauldron {
    public static final List<Cauldron> ALL = new ArrayList<>();

    private ModFluidsCauldron() {
    }

    public record Cauldron(String name, CauldronInteraction.InteractionMap interactions, DeferredBlock<LayeredCauldronBlock> block,
                           Supplier<? extends Item> bucket, @Nullable Supplier<? extends Item> bottle, int tint) {
    }

    public static Cauldron register(String name, Supplier<? extends Item> bucket, @Nullable Supplier<? extends Item> bottle) {
        return register(name, bucket, bottle, 0xFFFFFFFF);
    }

    public static Cauldron register(String name, Supplier<? extends Item> bucket, @Nullable Supplier<? extends Item> bottle, int tint) {
        CauldronInteraction.InteractionMap interactions = CauldronInteraction.newInteractionMap(name);
        DeferredBlock<LayeredCauldronBlock> block = ModBlocks.BLOCKS.register(name + "_cauldron",
                () -> new LayeredCauldronBlock(Biome.Precipitation.NONE, interactions, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER_CAULDRON)));
        Cauldron cauldron = new Cauldron(name, interactions, block, bucket, bottle, tint);
        ALL.add(cauldron);
        return cauldron;
    }

    public static void registerInteractions() {
        for (Cauldron c : ALL) {
            registerFor(c);
        }
    }

    private static void registerFor(Cauldron c) {
        Item bucket = c.bucket().get();
        Item bottle = c.bottle() == null ? null : c.bottle().get();
        BlockState empty1 = c.block().get().defaultBlockState();                                   // level 1
        BlockState full = empty1.setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL);

        CauldronInteraction pourBucket = (state, level, pos, player, hand, stack) ->
                CauldronInteraction.emptyBucket(level, pos, player, hand, stack, full, SoundEvents.BUCKET_EMPTY);

        // --- inside the fluid's own cauldron
        Map<Item, CauldronInteraction> own = c.interactions().map();
        own.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> CauldronInteraction.fillBucket(state, level, pos, player, hand,
                stack, new ItemStack(bucket), s -> s.getValue(LayeredCauldronBlock.LEVEL) == LayeredCauldronBlock.MAX_FILL_LEVEL, SoundEvents.BUCKET_FILL));
        own.put(bucket, pourBucket);
        own.put(Items.LAVA_BUCKET, CauldronInteraction.FILL_LAVA);
        own.put(Items.WATER_BUCKET, CauldronInteraction.FILL_WATER);
        own.put(Items.POWDER_SNOW_BUCKET, CauldronInteraction.FILL_POWDER_SNOW);
        if (bottle != null) {
            own.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
                if (!level.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(bottle)));
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            });
            own.put(bottle, (state, level, pos, player, hand, stack) -> {
                if (state.getValue(LayeredCauldronBlock.LEVEL) == LayeredCauldronBlock.MAX_FILL_LEVEL) {
                    return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
                }
                if (!level.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL));
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            });
        }
        // washing, same list as the water cauldron
        for (Item item : List.of(Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET,
                Items.LEATHER_HORSE_ARMOR, Items.WOLF_ARMOR)) {
            own.put(item, CauldronInteraction.DYED_ITEM);
        }
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof BannerItem) own.put(item, CauldronInteraction.BANNER);
            if (ShulkerBoxBlock.getColorFromItem(item) != null) own.put(item, CauldronInteraction.SHULKER_BOX);
        });

        // --- filling it from the other cauldrons (empty, water, lava, powder snow)
        for (Map<Item, CauldronInteraction> other : List.of(CauldronInteraction.EMPTY.map(), CauldronInteraction.WATER.map(),
                CauldronInteraction.LAVA.map(), CauldronInteraction.POWDER_SNOW.map())) {
            other.put(bucket, pourBucket);
        }
        if (bottle != null) {
            CauldronInteraction.EMPTY.map().put(bottle, (state, level, pos, player, hand, stack) -> {
                if (!level.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.setBlockAndUpdate(pos, empty1);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            });
        }
    }
}
