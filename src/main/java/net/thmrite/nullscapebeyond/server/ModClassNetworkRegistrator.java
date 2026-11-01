package net.thmrite.nullscapebeyond.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.server.classnetwork.ChargerEventPayload;
import net.thmrite.nullscapebeyond.server.classnetwork.DiverEventPayload;
import net.thmrite.nullscapebeyond.server.classnetwork.DiverNetwork;
import net.thmrite.nullscapebeyond.server.classnetwork.ChargerNetwork;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public abstract class ModClassNetworkRegistrator {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(ChargerEventPayload.TYPE, ChargerEventPayload.CODEC, ChargerNetwork::handle);
        registrar.playToServer(DiverEventPayload.TYPE, DiverEventPayload.CODEC, DiverNetwork::handle);
    }
}