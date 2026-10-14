package net.thmrite.nullscapebeyond.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModAttachments;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class BeaconTeleportManager {
    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    public static boolean isPending(UUID id) {
        return PENDING.containsKey(id);
    }

    public static void begin(ServerPlayer player, int delayTicks) {
        PENDING.put(player.getUUID(), Math.max(1, delayTicks));
        player.setData(ModAttachments.BEACON_ENTERING, true);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) return;
        Iterator<Map.Entry<UUID, Integer>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) { it.remove(); continue; }      // logged out (attachment isn't saved)
            int left = entry.getValue() - 1;
            if (left > 0) { entry.setValue(left); continue; }
            it.remove();
            finish(player);
        }
    }

    private static void finish(ServerPlayer player) {
        player.setData(ModAttachments.BEACON_ENTERING, false);
        if (!player.isAlive()) return;

        DimensionTransition target = player.findRespawnPositionAndUseSpawnBlock(true, entity -> {});
        Vec3 pos = target.pos();
        player.fallDistance = 0.0F;
        player.teleportTo(target.newLevel(), pos.x, pos.y, pos.z, target.yRot(), target.xRot());
    }

    private BeaconTeleportManager() {}
}