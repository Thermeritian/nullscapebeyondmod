package net.thmrite.nullscapebeyond.client;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.renderer.ChargerBootsRenderer;
import net.thmrite.nullscapebeyond.client.renderer.ChargerPlatformRenderer;
import net.thmrite.nullscapebeyond.client.renderer.blockentity.LevelObjectRenderer;
import net.thmrite.nullscapebeyond.registry.ModBlockEntities;
import net.thmrite.nullscapebeyond.registry.ModBlocks;
import net.thmrite.nullscapebeyond.registry.ModEntities;
import net.thmrite.nullscapebeyond.registry.ModItems;


@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                AccessoriesRendererRegistry.registerRenderer(ModItems.CHARGER_BOOTS.get(), ChargerBootsRenderer::new));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LEVEL_OBJECT.get(), LevelObjectRenderer::new);
        event.registerEntityRenderer(ModEntities.CHARGER_PLATFORM.get(), ChargerPlatformRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                return true;
            }
        }, ModBlocks.LEVEL_OBJET.get());
    }

    private ClientSetup() {}
}
