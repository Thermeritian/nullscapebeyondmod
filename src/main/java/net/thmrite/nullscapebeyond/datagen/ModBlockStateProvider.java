package net.thmrite.nullscapebeyond.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.SpawnBlock;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, NullscapeBeyond.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        // ---- Tile ----
        ModelFile tile = models().cubeBottomTop("tile",
                modLoc("block/tile_side"), modLoc("block/tile_bottom"), modLoc("block/tile_top"));
        simpleBlock(ModBlocks.TILE.get(), tile);
        simpleBlockItem(ModBlocks.TILE.get(), tile);
        ResourceLocation tileSide = modLoc("block/tile_side");
        ResourceLocation tileBottom = modLoc("block/tile_bottom");
        ResourceLocation tileTop = modLoc("block/tile_top");

        slabBlock(ModBlocks.TILE_SLAB.get(), modLoc("block/tile"), tileSide, tileBottom, tileTop);
        simpleBlockItem(ModBlocks.TILE_SLAB.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/tile_slab")));

        stairsBlock(ModBlocks.TILE_STAIRS.get(), tileSide, tileBottom, tileTop);
        simpleBlockItem(ModBlocks.TILE_STAIRS.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/tile_stairs")));

        // ---- Nullstone (top texture shared with bottom) ----
        ResourceLocation nsSide = modLoc("block/nullstone_side");
        ResourceLocation nsTop = modLoc("block/nullstone_top");

        ModelFile nullstone = models().cubeColumn("nullstone", nsSide, nsTop);
        simpleBlock(ModBlocks.NULLSTONE.get(), nullstone);
        simpleBlockItem(ModBlocks.NULLSTONE.get(), nullstone);

        slabBlock(ModBlocks.NULLSTONE_SLAB.get(), modLoc("block/nullstone"), nsSide, nsTop, nsTop);
        simpleBlockItem(ModBlocks.NULLSTONE_SLAB.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/nullstone_slab")));

        stairsBlock(ModBlocks.NULLSTONE_STAIRS.get(), nsSide, nsTop, nsTop);
        simpleBlockItem(ModBlocks.NULLSTONE_STAIRS.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/nullstone_stairs")));

        // ---- Spawn ----
        ResourceLocation spawnTex = modLoc("block/spawn/spawn");
        ModelFile spawnDefault = models().cubeAll("spawn", spawnTex);
        simpleBlockItem(ModBlocks.SPAWN.get(), spawnDefault);

        var builder = getVariantBuilder(ModBlocks.SPAWN.get());
        builder.partialState().with(SpawnBlock.PATTERN, SpawnBlock.Pattern.DEFAULT)
                .modelForState().modelFile(spawnDefault).addModel();

        for (SpawnBlock.Pattern pattern : SpawnBlock.Pattern.values()) {
            if (pattern == SpawnBlock.Pattern.DEFAULT) continue;
            String cell = pattern.getSerializedName();
            ResourceLocation cellTex = modLoc("block/spawn/spawn_" + cell);
            // Cell texture on top only; every other face uses the default spawn texture.
            ModelFile model = models().cube("spawn_" + cell,
                            spawnTex, cellTex, spawnTex, spawnTex, spawnTex, spawnTex)
                    .texture("particle", spawnTex);
            builder.partialState().with(SpawnBlock.PATTERN, pattern)
                    .modelForState().modelFile(model).addModel();
        }

        //Level Block
        // ---- Level object: invisible block, barrier icon in inventory ----
        ModelFile levelObjectModel = models().getBuilder("level_objet")
                .texture("particle", mcLoc("item/barrier"));
        simpleBlock(ModBlocks.LEVEL_OBJET.get(), levelObjectModel);
        itemModels().withExistingParent("level_objet", "minecraft:item/generated")
                .texture("layer0", mcLoc("item/barrier"));
    }
}