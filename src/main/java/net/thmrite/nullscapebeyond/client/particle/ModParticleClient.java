package net.thmrite.nullscapebeyond.client.particle;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModParticles;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ModParticleClient {
    private ModParticleClient() {
    }

    @SubscribeEvent
    static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticles.VIRIDIAN_RESIN_DRIP.get(), new ResinDripParticle.Provider());
    }
}
