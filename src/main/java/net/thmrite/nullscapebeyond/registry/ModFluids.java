package net.thmrite.nullscapebeyond.registry;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NullscapeBeyond.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NullscapeBeyond.MODID);

    public static final DeferredHolder<FluidType, FluidType> NULQUA_TYPE = FLUID_TYPES.register("nulqua",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block." + NullscapeBeyond.MODID + ".nulqua")
                    .fallDistanceModifier(0.0F)
                    .canExtinguish(true)
                    .canConvertToSource(true)
                    .supportsBoating(true)
                    .canHydrate(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            ));


    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> NULQUA =
            FLUIDS.register("nulqua", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> NULQUA_FLOWING =
            FLUIDS.register("nulqua_flowing", () -> new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredBlock<LiquidBlock> NULQUA_BLOCK = ModBlocks.regNoItem("nulqua", () -> new LiquidBlock(NULQUA.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(NULQUA_TYPE, NULQUA, NULQUA_FLOWING)
                .bucket(ModItems.NULQUA_BUCKET)
                .block(NULQUA_BLOCK)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .explosionResistance(100.0F);
    }

    private ModFluids() {
    }
}
