package net.thmrite.nullscapebeyond.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.renderer.effect.ClientVoidboundTracker;
import net.thmrite.nullscapebeyond.debug.VoidboundDebug;
import net.thmrite.nullscapebeyond.server.effects.VoidboundSyncPayload;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class ModNetwork {
    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(VoidboundSyncPayload.TYPE, VoidboundSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ClientVoidboundTracker.set(payload.entityId(), payload.voidbound());
                    VoidboundDebug.log("CLIENT: received voidbound sync for entity id {} -> {}", payload.entityId(), payload.voidbound());
                }));
    }
}