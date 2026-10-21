package net.thmrite.nullscapebeyond.skybox;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.skybox.SkyboxManager;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class SkyboxNetwork {
    private SkyboxNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                SkyboxPayload.TYPE,
                SkyboxPayload.CODEC,
                (payload, ctx) -> SkyboxManager.apply(payload));
    }
}