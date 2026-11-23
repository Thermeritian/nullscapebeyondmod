package net.thmrite.nullscapebeyond.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/**
 * The fluid the sea flora lives in. Vanilla kelp / seagrass / lily pads hard-code Fluids.WATER; every place that did is
 * replaced by this. It looks the fluid up by id, so it does not depend on how your fluid classes are named.
 * To make the flora live in another water-like fluid, change FLUID_ID.
 */
public final class SeaFloraFluid {
    public static final ResourceLocation FLUID_ID = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "nulqua");
    private static FlowingFluid cached;

    private SeaFloraFluid() {
    }

    public static FlowingFluid source() {
        if (cached == null) {
            Fluid fluid = BuiltInRegistries.FLUID.get(FLUID_ID);
            if (!(fluid instanceof FlowingFluid flowing) || fluid == Fluids.EMPTY) {
                throw new IllegalStateException("Sea flora fluid not registered: " + FLUID_ID);
            }
            cached = flowing;
        }
        return cached;
    }

    /** The fluid state the plants report (like kelp reporting water). */
    public static FluidState sourceState() {
        return source().getSource(false);
    }

    /** A full source block of the fluid (what vanilla checks with getAmount() == 8 on a water tag state). */
    public static boolean isSource(FluidState state) {
        return state.getType() == source() && state.getAmount() == 8;
    }

    /** The liquid block of the fluid, for "can grow into" checks that vanilla does with Blocks.WATER. */
    public static Block liquidBlock() {
        return source().defaultFluidState().createLegacyBlock().getBlock();
    }

    public static boolean isSourceBlock(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(liquidBlock()) && level.getFluidState(pos).isSource();
    }

    /** Vanilla plants schedule a WATER tick after a shape update so the fluid keeps flowing; this is the same for our fluid. */
    public static void scheduleTick(LevelAccessor level, BlockPos pos) {
        level.scheduleTick(pos, source(), source().getTickDelay(level));
    }
}
