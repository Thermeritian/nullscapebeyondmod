package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.*;
import net.minecraft.world.level.block.CarpetBlock;

import net.minecraft.world.food.FoodProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NullscapeBeyond.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(NullscapeBeyond.MODID);

    ////////////HELPERS

    public static <T extends Block> DeferredBlock<T> reg(String name, Supplier<T> block, Function<T, Item> item,
                                                         List<Supplier<? extends ItemLike>> tab) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        if (item != null) {
            DeferredItem<Item> blockItem = ModItems.ITEMS.register(name, () -> item.apply(registered.get()));
            if (tab != null) tab.add(blockItem);
        }
        return registered;
    }
    public static <T extends Block> DeferredBlock<T> regNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    //Stone Derivatives Helper
    public record StoneVariants(DeferredBlock<StairBlock> stairs, DeferredBlock<SlabBlock> slab, DeferredBlock<WallBlock> wall) {}
    private static StoneVariants variants(String name, Supplier<? extends Block> base) {
        DeferredBlock<StairBlock> stairs = reg(name + "_stairs",
                () -> new StairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(base.get())), TERRAIN_ITEMS);
        DeferredBlock<SlabBlock> slab = reg(name + "_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(base.get())), TERRAIN_ITEMS);
        DeferredBlock<WallBlock> wall = reg(name + "_wall",
                () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(base.get()).forceSolidOn()), TERRAIN_ITEMS);
        return new StoneVariants(stairs, slab, wall);
    }

    //Terrain Blocks Helper
    public static final List<Supplier<? extends ItemLike>> TERRAIN_ITEMS = new ArrayList<>();
    public static <T extends Block> DeferredBlock<T> reg(String name, Supplier<T> block, List<Supplier<? extends ItemLike>> tab) {
        return reg(name, block, b -> new BlockItem(b, new Item.Properties()), tab);
    }

    //Flora Helper
    public static final List<Supplier<? extends ItemLike>> FLORA_ITEMS = new ArrayList<>();
    private static BlockBehaviour.Properties plant(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak()
                .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    //Flower Pot Helper
    private static DeferredBlock<FlowerPotBlock> potted(String plantName, Supplier<? extends Block> plant) {
        return regNoItem("potted_" + plantName,
                () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, plant,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_FERN)));
    }
    public static void registerPottedPlants() {
        FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
        pot.addPlant(IVORY_FIDDLEHEAD.getId(), POTTED_IVORY_FIDDLEHEAD);
        pot.addPlant(AZURE_CERULIANITES.getId(), POTTED_AZURE_CERULIANITES);
        pot.addPlant(COBALT_MUSHROOM.getId(), POTTED_COBALT_MUSHROOM);
        pot.addPlant(AZURE.sapling.getId(), POTTED_AZURE_SAPLING);
        pot.addPlant(VIRIDIAN.sapling.getId(), POTTED_VIRIDIAN_SAPLING);
    }

    /////////BLOCKS

    ////WOODSETS
    public static final ModWoodSet AZURE = ModWoodSet.create("azure",
            MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_GRAY, ModWoodTypes.AZURE, ModTreeGrowers.AZURE);
    public static final ModWoodSet VIRIDIAN = ModWoodSet.create("viridian",
            MapColor.COLOR_LIGHT_GREEN, MapColor.COLOR_GRAY, ModWoodTypes.VIRIDIAN, ModTreeGrowers.VIRIDIAN);


    ////TERRAIN
    public static final DeferredBlock<Block> NULLSOIL = reg("nullsoil",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).mapColor(MapColor.TERRACOTTA_BROWN)), TERRAIN_ITEMS);

    //GRASS BLOCKS
    public static final DeferredBlock<NullGrassBlock> AZURE_GRASS_BLOCK = reg("azure_grass_block",
            () -> new NullGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.COLOR_LIGHT_BLUE)), TERRAIN_ITEMS);
    public static final DeferredBlock<NullGrassBlock> VIRIDIAN_GRASS_BLOCK = reg("viridian_grass_block",
            () -> new NullGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN)), TERRAIN_ITEMS);
    public static final DeferredBlock<NullGrassBlock> SCARLET_GRASS_BLOCK = reg("scarlet_grass_block",
            () -> new NullGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.COLOR_RED)), TERRAIN_ITEMS);

    //NULLSLATE
    public static final DeferredBlock<Block> NULLSLATE = reg("nullslate",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    public static final DeferredBlock<Block> NULLSLATE_COBBLED = reg("nullslate_cobbled",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    //NULLMARBLE
    public static final DeferredBlock<Block> NULLMARBLE = reg("nullmarble",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.QUARTZ)), TERRAIN_ITEMS);
    //PALESAND
    public static final DeferredBlock<ColoredFallingBlock> PALESAND = reg("palesand",
            () -> new ColoredFallingBlock(new ColorRGBA(0xE9E2CF), BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.QUARTZ)), TERRAIN_ITEMS);
    public static final DeferredBlock<Block> PALESANDSTONE = reg("palesandstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE).mapColor(MapColor.QUARTZ)), TERRAIN_ITEMS);

    //BLIGHT SHALE
    public static final DeferredBlock<ColoredFallingBlock> BLIGHTED_SHALE = reg("blighted_shale",
            () -> new ColoredFallingBlock(new ColorRGBA(0x6B5F67), BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE)), TERRAIN_ITEMS);
    public static final DeferredBlock<Block> BLIGHTED_SHALE_BRICKS = reg("blighted_shale_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).mapColor(MapColor.COLOR_PURPLE)), TERRAIN_ITEMS);
    public static final StoneVariants BLIGHTED_SHALE_BRICKS_VARIANTS = variants("blighted_shale_bricks", BLIGHTED_SHALE_BRICKS);
    public static final DeferredBlock<SuspiciousBlightedShaleBlock> BLIGHTED_SHALE_SUSPICIOUS = reg("blighted_shale_suspicious",
            () -> new SuspiciousBlightedShaleBlock(BLIGHTED_SHALE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.SUSPICIOUS_GRAVEL),
                    SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED), TERRAIN_ITEMS);

    //STILLCLAY
    public static final DeferredBlock<Block> STILLCLAY = reg("stillclay",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY)), TERRAIN_ITEMS);

    ////NATURAL BLOCKS (TERRAIN)
    public static final DeferredBlock<HangingLeavesBlock> AZURE_LEAVES_HANGING = regNoItem("azure_leaves_hanging",
            () -> new HangingLeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollission()
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredItem<Item> AZURE_LEAVES_HANGING_ITEM = ModItems.ITEMS.register("azure_leaves_hanging",
            () -> new BlockItem(AZURE_LEAVES_HANGING.get(), new Item.Properties()));
    public static final DeferredBlock<MushroomBlock> COBALT_MUSHROOM = reg("cobalt_mushroom",
            () -> new MushroomBlock(ModFeatures.COBALT_MUSHROOM_HUGE,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM).mapColor(MapColor.COLOR_BLUE)), FLORA_ITEMS);
    public static final DeferredBlock<Block> COBALT_MUSHROOM_BLOCK = reg("cobalt_mushroom_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM_BLOCK).mapColor(MapColor.COLOR_BLUE)), FLORA_ITEMS);
    public static final DeferredBlock<RotatedPillarBlock> COBALT_MUSHROOM_STEM = reg("cobalt_mushroom_stem",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM).mapColor(MapColor.COLOR_LIGHT_BLUE)), FLORA_ITEMS);
    public static final DeferredBlock<HangingRootsBlock> COBALT_MUSHROOM_HANGING_ROOTS = reg("cobalt_mushroom_hanging_roots",
            () -> new HangingRootsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HANGING_ROOTS).mapColor(MapColor.COLOR_LIGHT_BLUE)), FLORA_ITEMS);
    public static final DeferredBlock<NullPlantBlock> AZURE_GRASS = reg("azure_grass",
            () -> new NullPlantBlock(plant(MapColor.COLOR_LIGHT_BLUE).replaceable()), FLORA_ITEMS);
    public static final DeferredBlock<NullPlantBlock> AZURE_CERULIANITES = reg("azure_cerulianites",
            () -> new NullPlantBlock(plant(MapColor.COLOR_CYAN)), FLORA_ITEMS);
    public static final DeferredBlock<NullPlantBlock> IVORY_FIDDLEHEAD = reg("ivory_fiddlehead",
            () -> new NullPlantBlock(plant(MapColor.SNOW)), FLORA_ITEMS);
    public static final DeferredBlock<PinkPetalsBlock> CELESTINE_FLOWERS = reg("celestine_flowers",
            () -> new PinkPetalsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_PETALS).mapColor(MapColor.COLOR_PURPLE)), FLORA_ITEMS);

    ////NATURAL BLOCKS (SEA)
    public static final DeferredBlock<RiftweedBlock> RIFTWEED = reg("riftweed",
            () -> new RiftweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.KELP)),
            b -> new ItemNameBlockItem(b, new Item.Properties()), FLORA_ITEMS);
    public static final DeferredBlock<RiftweedPlantBlock> RIFTWEED_PLANT = regNoItem("riftweed_plant",
            () -> new RiftweedPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.KELP_PLANT)));
    public static final DeferredBlock<NilreedBlock> NILREED = reg("nilreed",
            () -> new NilreedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS)), FLORA_ITEMS);
    public static final DeferredBlock<TallNilreedBlock> TALL_NILREED = regNoItem("tall_nilreed",
            () -> new TallNilreedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_SEAGRASS)));
    public static final DeferredBlock<AlbinoleafBlock> ALBINOLEAF = reg("albinoleaf",
            () -> new AlbinoleafBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD)),
            b -> new PlaceOnWaterBlockItem(b, new Item.Properties()), FLORA_ITEMS);
    public static final DeferredBlock<DeepOnionBlock> DEEPONION = reg("deeponion",
            () -> new DeepOnionBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().randomTicks()
                    .instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)),
            b -> new ItemNameBlockItem(b, new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())),
            FLORA_ITEMS);


    public static final DeferredBlock<ViridianLeavesResinBlock> VIRIDIAN_LEAVES_RESIN = reg("viridian_leaves_resin",
            () -> new ViridianLeavesResinBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)), FLORA_ITEMS);
    public static final DeferredBlock<ViridianResinHangingBlock> VIRIDIAN_RESIN_HANGING = reg("viridian_resin_hanging",
            () -> new ViridianResinHangingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission()
                    .strength(0.3F).sound(SoundType.HONEY_BLOCK).noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY)), FLORA_ITEMS);
    public static final DeferredBlock<CarpetBlock> VIRIDIAN_RESIN_DROPLETS = reg("viridian_resin_droplets",
            () -> new CarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noCollission().instabreak()
                    .sound(SoundType.HONEY_BLOCK).noOcclusion().pushReaction(PushReaction.DESTROY)), FLORA_ITEMS);

    ///CROPS
        public static final DeferredBlock<WildRossaBlock> WILD_ROSSA = reg("wild_rossa",
                () -> new WildRossaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission()
                        .randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)), FLORA_ITEMS
        );

    //FLAMES
    public static final DeferredBlock<NullflameBlock> NULLFLAME = regNoItem("nullflame",
            () -> new NullflameBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_FIRE).lightLevel(s -> 12)));
    public static final DeferredBlock<TorchBlock> NULLFLAME_TORCH = regNoItem("nullflame_torch",
            () -> new TorchBlock(ParticleTypes.SOUL_FIRE_FLAME, BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_TORCH).lightLevel(s -> 12)));   // placeholder flame particle
    public static final DeferredBlock<WallTorchBlock> NULLFLAME_WALL_TORCH = regNoItem("nullflame_wall_torch",
            () -> new WallTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_WALL_TORCH).lightLevel(s -> 12)
                    .dropsLike(NULLFLAME_TORCH.get())));
    public static final DeferredItem<Item> NULLFLAME_TORCH_ITEM = ModItems.ITEMS.register("nullflame_torch",
            () -> new StandingAndWallBlockItem(NULLFLAME_TORCH.get(), NULLFLAME_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredBlock<LanternBlock> NULLFLAME_LANTERN = reg("nullflame_lantern",
            () -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN).lightLevel(s -> 12)), TERRAIN_ITEMS);
    public static final DeferredBlock<CampfireBlock> NULLFLAME_CAMPFIRE = reg("nullflame_campfire",
            () -> new CampfireBlock(false, 2, BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_CAMPFIRE)
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 12 : 0)), TERRAIN_ITEMS);


    ////BUILDING BLOKCS
        ///STONESETS
            //NULLSLATE
    public static final DeferredBlock<Block> NULLSLATE_BRICKS = reg("nullslate_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    public static final DeferredBlock<Block> NULLSLATE_BRICKS_CRACKED = reg("nullslate_bricks_cracked",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    public static final DeferredBlock<Block> NULLSLATE_SMALL_BRICKS = reg("nullslate_small_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    public static final DeferredBlock<Block> NULLSLATE_POLISHED = reg("nullslate_polished",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    public static final DeferredBlock<RotatedPillarBlock> NULLSLATE_PILLAR = reg("nullslate_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_GRAY)), TERRAIN_ITEMS);
    public static final StoneVariants NULLSLATE_VARIANTS = variants("nullslate", NULLSLATE);
    public static final StoneVariants NULLSLATE_BRICKS_VARIANTS = variants("nullslate_bricks", NULLSLATE_BRICKS);
    public static final StoneVariants NULLSLATE_SMALL_BRICKS_VARIANTS = variants("nullslate_small_bricks", NULLSLATE_SMALL_BRICKS);
    public static final StoneVariants NULLSLATE_COBBLED_VARIANTS = variants("nullslate_cobbled", NULLSLATE_COBBLED);
        //NULLMARBLE
    public static final DeferredBlock<Block> NULLMARBLE_BRICKS = reg("nullmarble_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.QUARTZ)), TERRAIN_ITEMS);
    public static final StoneVariants NULLMARBLE_VARIANTS = variants("nullmarble", NULLMARBLE);
    public static final StoneVariants NULLMARBLE_BRICKS_VARIANTS = variants("nullmarble_bricks", NULLMARBLE_BRICKS);

    ///POTTED FLORA
    public static final DeferredBlock<FlowerPotBlock> POTTED_IVORY_FIDDLEHEAD = potted("ivory_fiddlehead", IVORY_FIDDLEHEAD);
    public static final DeferredBlock<FlowerPotBlock> POTTED_AZURE_CERULIANITES = potted("azure_cerulianites", AZURE_CERULIANITES);
    public static final DeferredBlock<FlowerPotBlock> POTTED_COBALT_MUSHROOM = potted("cobalt_mushroom", COBALT_MUSHROOM);
    public static final DeferredBlock<FlowerPotBlock> POTTED_AZURE_SAPLING = potted("azure_sapling", AZURE.sapling);
    public static final DeferredBlock<FlowerPotBlock> POTTED_VIRIDIAN_SAPLING = potted("viridian_sapling", VIRIDIAN.sapling);

    ////LEVEL BLOCKS
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

    public static final DeferredBlock<LevelObjectBlock> LEVEL_OBJET = BLOCKS.registerBlock("level_objet",
            LevelObjectBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3600000.0F)
                    .noCollission()
                    .noLootTable()
                    .lightLevel(LevelObjectBlock::lightFor));



    public static final DeferredItem<BlockItem> TILE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TILE);
    public static final DeferredItem<BlockItem> TILE_SLAB_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TILE_SLAB);
    public static final DeferredItem<BlockItem> TILE_STAIRS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TILE_STAIRS);
    public static final DeferredItem<BlockItem> NULLSTONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(NULLSTONE);
    public static final DeferredItem<BlockItem> NULLSTONE_SLAB_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(NULLSTONE_SLAB);
    public static final DeferredItem<BlockItem> NULLSTONE_STAIRS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(NULLSTONE_STAIRS);
    public static final DeferredItem<BlockItem> SPAWN_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(SPAWN);
    public static final DeferredItem<BlockItem> LEVEL_OBJET_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(LEVEL_OBJET);


    private ModBlocks() {}
}