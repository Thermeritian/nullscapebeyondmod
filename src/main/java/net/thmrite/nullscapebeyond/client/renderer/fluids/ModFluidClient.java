package net.thmrite.nullscapebeyond.client.renderer.fluids;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModFluids;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ModFluidClient {
    public static final int TILES = 20;
    private static final ResourceLocation[][] TILE = new ResourceLocation[TILES][TILES];
    private static final ResourceLocation UNDERWATER = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

    static {
        for (int x = 0; x < TILES; x++) {
            for (int z = 0; z < TILES; z++) {
                TILE[x][z] = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "fluid/nulqua/" + x + "_" + z);
            }
        }
    }

    private ModFluidClient() {
    }

    private static ResourceLocation tile(BlockPos pos) {
        return TILE[Math.floorMod(pos.getX(), TILES)][Math.floorMod(pos.getZ(), TILES)];
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return TILE[0][0];
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return TILE[0][0];
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return TILE[0][0];
            }

            @Override
            public ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                return tile(pos);
            }

            @Override
            public ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                return tile(pos);
            }

            @Override
            public ResourceLocation getOverlayTexture(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                return tile(pos);
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft minecraft) {
                return UNDERWATER;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;   // no biome water tint: the texture carries all the colour
            }
        }, ModFluids.NULQUA_TYPE.get());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.NULQUA.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.NULQUA_FLOWING.get(), RenderType.translucent());
        });
    }
}
