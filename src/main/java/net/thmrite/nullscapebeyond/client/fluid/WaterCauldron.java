package net.thmrite.nullscapebeyond.client.fluid;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModFluidsCauldron;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class WaterCauldron {
    private WaterCauldron() {
    }

    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (ModFluidsCauldron.Cauldron cauldron : ModFluidsCauldron.ALL) {
            int tint = cauldron.tint();
            event.register((state, level, pos, index) -> tint, cauldron.block().get());
        }
    }
}
