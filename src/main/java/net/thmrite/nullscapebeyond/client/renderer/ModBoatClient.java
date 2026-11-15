package net.thmrite.nullscapebeyond.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModBoats;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ModBoatClient {
    private ModBoatClient() {
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (ModBoats.BoatSet set : ModBoats.ALL) {
            event.registerLayerDefinition(ModBoatRenderer.layer(set.name(), false), BoatModel::createBodyModel);
            event.registerLayerDefinition(ModBoatRenderer.layer(set.name(), true), ChestBoatModel::createBodyModel);
        }
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (ModBoats.BoatSet set : ModBoats.ALL) {
            event.registerEntityRenderer(set.boat().get(), ctx -> new ModBoatRenderer(ctx, set.name(), false));
            event.registerEntityRenderer(set.chestBoat().get(), ctx -> new ModBoatRenderer(ctx, set.name(), true));
        }
    }
}
