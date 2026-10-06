package net.thmrite.nullscapebeyond.client;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.renderer.ChargerBootsRenderer;
import net.thmrite.nullscapebeyond.registry.ModItems;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                AccessoriesRendererRegistry.registerRenderer(ModItems.CHARGER_BOOTS.get(), ChargerBootsRenderer::new));
    }

    private ClientSetup() {}
}
