package net.thmrite.nullscapebeyond.client.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.renderer.InfestorRenderer;
import net.thmrite.nullscapebeyond.client.renderer.effect.VoidTextures;
import net.thmrite.nullscapebeyond.client.renderer.effect.VoidboundLayer;
import net.thmrite.nullscapebeyond.debug.VoidboundDebug;
import net.thmrite.nullscapebeyond.registry.ModEntities;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public class VoidboundClientEvents {

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INFESTOR.get(), InfestorRenderer::new);
        VoidboundDebug.log("RegisterRenderers fired, infestor renderer registered");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        int total = 0, added = 0;
        List<String> notLiving = new ArrayList<>();
        List<String> nullRenderer = new ArrayList<>();
        for (EntityType<?> type : event.getEntityTypes()) {
            total++;
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer == null) {
                nullRenderer.add(name);
            } else if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new VoidboundLayer(living));
                added++;
            } else {
                notLiving.add(name + " (" + renderer.getClass().getSimpleName() + ")");
            }
        }
        VoidboundDebug.log("AddLayers fired: {} entity types, VoidboundLayer added to {} renderers", total, added);
        VoidboundDebug.log("AddLayers: renderer is NOT a LivingEntityRenderer (no layer possible): {}", notLiving);
        VoidboundDebug.log("AddLayers: renderer was null: {}", nullRenderer);
    }

    @SubscribeEvent
    static void onReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) rm -> {
            VoidboundDebug.log("Resource reload: clearing voidbound texture cache");
            VoidTextures.clear();
        });
    }
}