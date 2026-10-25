package net.thmrite.nullscapebeyond.client.hooks;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/** Client-side list of entity ids that are voidbound (filled by VoidboundSyncPayload). */
@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ClientVoidboundTracker {
    private static final IntSet IDS = IntSets.synchronize(new IntOpenHashSet());

    private ClientVoidboundTracker() {}

    public static void set(int entityId, boolean voidbound) {
        if (voidbound) IDS.add(entityId); else IDS.remove(entityId);
    }

    public static boolean contains(int entityId) {
        return IDS.contains(entityId);
    }

    @SubscribeEvent
    static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) IDS.remove(event.getEntity().getId());
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        IDS.clear();
    }
}