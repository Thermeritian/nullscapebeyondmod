package net.thmrite.nullscapebeyond.registry;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.fluid.WaterFluid;
import net.thmrite.nullscapebeyond.fluid.WaterFluidType;
import net.thmrite.nullscapebeyond.item.WaterBottleItem;


public final class ModFluidsWater {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NullscapeBeyond.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NullscapeBeyond.MODID);

    public static final List<Entry> ALL = new ArrayList<>();

    private ModFluidsWater() {
    }

    public record Entry(String name,
                        DeferredHolder<FluidType, WaterFluidType> type,
                        DeferredHolder<Fluid, WaterFluid.Source> source,
                        DeferredHolder<Fluid, WaterFluid.Flowing> flowing,
                        DeferredBlock<LiquidBlock> block,
                        DeferredItem<Item> bucket,
                        @Nullable DeferredItem<Item> bottle,
                        Textures textures) {
    }

    public static final class Textures {
        private final ResourceLocation still;
        private final ResourceLocation flowing;
        private final ResourceLocation overlay;
        private final int tint;
        private final ResourceLocation[][] tiles;

        Textures(String name, int areaTiles, int tint) {
            this.tint = tint;
            if (areaTiles > 0) {
                this.tiles = new ResourceLocation[areaTiles][areaTiles];
                for (int x = 0; x < areaTiles; x++) {
                    for (int z = 0; z < areaTiles; z++) {
                        tiles[x][z] = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "fluid/" + name + "/" + x + "_" + z);
                    }
                }
                this.still = tiles[0][0];
                this.flowing = tiles[0][0];
                this.overlay = tiles[0][0];
            } else {
                this.tiles = null;
                this.still = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "block/" + name + "_still");
                this.flowing = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "block/" + name + "_flow");
                this.overlay = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "block/" + name + "_overlay");
            }
        }

        public ResourceLocation still(BlockPos pos) {
            return tiles == null ? still : tile(pos);
        }

        public ResourceLocation flowing(BlockPos pos) {
            return tiles == null ? flowing : tile(pos);
        }

        public ResourceLocation overlay(BlockPos pos) {
            return tiles == null ? overlay : tile(pos);
        }

        public ResourceLocation still() {
            return still;
        }

        public ResourceLocation flowing() {
            return flowing;
        }

        public ResourceLocation overlay() {
            return overlay;
        }

        public int tint() {
            return tint;
        }

        private ResourceLocation tile(BlockPos pos) {
            return tiles[Math.floorMod(pos.getX(), tiles.length)][Math.floorMod(pos.getZ(), tiles.length)];
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private int areaTiles = 0;
        private boolean bottle = false;
        private int tint = 0xFFFFFFFF;
        private ParticleOptions bubble = ParticleTypes.BUBBLE;
        private ParticleOptions splash = ParticleTypes.SPLASH;

        private Builder(String name) {
            this.name = name;
        }

        public Builder areaTexture(int tiles) {
            this.areaTiles = tiles;
            return this;
        }

        public Builder withBottle() {
            this.bottle = true;
            return this;
        }

        public Builder tint(int argb) {
            this.tint = argb;
            return this;
        }

        public Builder particles(ParticleOptions bubble, ParticleOptions splash) {
            this.bubble = bubble;
            this.splash = splash;
            return this;
        }

        public Entry register() {
            final String id = name;
            final Holders h = new Holders();
            h.type = FLUID_TYPES.register(id, () -> new WaterFluidType(
                    WaterFluidType.waterProperties("block." + NullscapeBeyond.MODID + "." + id),
                    bottle ? () -> h.bottle.get() : null, this.bubble, this.splash));
            h.source = FLUIDS.register(id, () -> new WaterFluid.Source(properties(h)));
            h.flowing = FLUIDS.register(id + "_flowing", () -> new WaterFluid.Flowing(properties(h)));
            h.block = ModBlocks.BLOCKS.register(id, () -> new LiquidBlock(h.source.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));
            h.bucket = ModItems.ITEMS.register(id + "_bucket",
                    () -> new BucketItem(h.source.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
            if (bottle) {
                h.bottle = ModItems.ITEMS.register(id + "_bottle", () -> new WaterBottleItem(new Item.Properties().stacksTo(1)));
            }
            Entry entry = new Entry(id, h.type, h.source, h.flowing, h.block, h.bucket, h.bottle, new Textures(id, areaTiles, tint));
            ALL.add(entry);
            return entry;
        }
    }

    private static final class Holders {
        DeferredHolder<FluidType, WaterFluidType> type;
        DeferredHolder<Fluid, WaterFluid.Source> source;
        DeferredHolder<Fluid, WaterFluid.Flowing> flowing;
        DeferredBlock<LiquidBlock> block;
        DeferredItem<Item> bucket;
        DeferredItem<Item> bottle;
    }

    private static BaseFlowingFluid.Properties properties(Holders h) {
        return new BaseFlowingFluid.Properties(h.type, h.source, h.flowing)
                .bucket(h.bucket)
                .block(h.block)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .explosionResistance(100.0F);
    }
}
