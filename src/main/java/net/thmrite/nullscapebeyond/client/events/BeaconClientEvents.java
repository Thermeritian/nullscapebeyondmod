package net.thmrite.nullscapebeyond.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModAttachments;

/** Fully hides players (body, armor, held items, first-person hand) while a beacon is swallowing them. */
@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class BeaconClientEvents {
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getData(ModAttachments.BEACON_ENTERING)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getData(ModAttachments.BEACON_ENTERING)) {
            event.setCanceled(true);
        }
    }
}