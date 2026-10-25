package net.thmrite.nullscapebeyond.client.events;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.renderer.InfestorRenderer;
import net.thmrite.nullscapebeyond.client.renderer.VoidTextures;
import net.thmrite.nullscapebeyond.client.renderer.VoidboundLayer;
import net.thmrite.nullscapebeyond.registry.ModEntities;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class VoidboundClientEvents {

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INFESTOR.get(), InfestorRenderer::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new VoidboundLayer(living));
            }
        }
    }

    @SubscribeEvent
    static void onReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) rm -> VoidTextures.clear());
    }
}