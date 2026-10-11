package net.thmrite.nullscapebeyond.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.*;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NullscapeBeyond.MODID);
    // Separate from ModItems so this file is self-contained; merge into ModItems if you prefer.
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(NullscapeBeyond.MODID);

    public static final DeferredBlock<FootstepBlock> TILE = BLOCKS.registerBlock("tile",
            props -> new FootstepBlock(props, ModBlockSounds.TILE),
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.5F, 6.0F));
    public static final DeferredBlock<FootstepSlabBlock> TILE_SLAB = BLOCKS.registerBlock("tile_slab",
            props -> new FootstepSlabBlock(props, ModBlockSounds.TILE),
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.5F, 6.0F));
    public static final DeferredBlock<FootstepStairBlock> TILE_STAIRS = BLOCKS.registerBlock("tile_stairs",
            props -> new FootstepStairBlock(TILE.get().defaultBlockState(), props, ModBlockSounds.TILE),
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.5F, 6.0F));

    public static final DeferredBlock<FootstepBlock> NULLSTONE = BLOCKS.registerBlock("nullstone",
            props -> new FootstepBlock(props, ModBlockSounds.NULLSTONE),
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F));

    public static final DeferredBlock<FootstepSlabBlock> NULLSTONE_SLAB = BLOCKS.registerBlock("nullstone_slab",
            props -> new FootstepSlabBlock(props, ModBlockSounds.NULLSTONE),
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F));

    public static final DeferredBlock<FootstepStairBlock> NULLSTONE_STAIRS = BLOCKS.registerBlock("nullstone_stairs",
            props -> new FootstepStairBlock(NULLSTONE.get().defaultBlockState(), props, ModBlockSounds.NULLSTONE),
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F));

    public static final DeferredBlock<SpawnBlock> SPAWN = BLOCKS.registerBlock("spawn",
            props -> new SpawnBlock(props, ModBlockSounds.NULLSTONE),
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F));

    public static final DeferredItem<BlockItem> TILE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TILE);
    public static final DeferredItem<BlockItem> TILE_SLAB_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TILE_SLAB);
    public static final DeferredItem<BlockItem> TILE_STAIRS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TILE_STAIRS);
    public static final DeferredItem<BlockItem> NULLSTONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(NULLSTONE);
    public static final DeferredItem<BlockItem> NULLSTONE_SLAB_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(NULLSTONE_SLAB);
    public static final DeferredItem<BlockItem> NULLSTONE_STAIRS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(NULLSTONE_STAIRS);
    public static final DeferredItem<BlockItem> SPAWN_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(SPAWN);

    private ModBlocks() {}
}