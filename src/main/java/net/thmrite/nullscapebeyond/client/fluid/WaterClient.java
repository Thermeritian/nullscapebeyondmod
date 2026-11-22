package net.thmrite.nullscapebeyond.client.fluid;

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
import net.thmrite.nullscapebeyond.registry.ModFluidsWater;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class WaterClient {
    private static final ResourceLocation UNDERWATER = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

    private WaterClient() {
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        for (ModFluidsWater.Entry entry : ModFluidsWater.ALL) {
            ModFluidsWater.Textures t = entry.textures();
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return t.still();
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return t.flowing();
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return t.overlay();
                }

                @Override
                public ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                    return t.still(pos);
                }

                @Override
                public ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                    return t.flowing(pos);
                }

                @Override
                public ResourceLocation getOverlayTexture(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                    return t.overlay(pos);
                }

                @Override
                public ResourceLocation getRenderOverlayTexture(Minecraft minecraft) {
                    return UNDERWATER;
                }

                @Override
                public int getTintColor() {
                    return t.tint();
                }
            }, entry.type().get());
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ModFluidsWater.Entry entry : ModFluidsWater.ALL) {
                ItemBlockRenderTypes.setRenderLayer(entry.source().get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(entry.flowing().get(), RenderType.translucent());
            }
        });
    }
}
