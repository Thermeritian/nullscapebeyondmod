package net.thmrite.nullscapebeyond.server.classnetwork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;


@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class BashFreeze {
    private record Freeze(long end, double strength, double dx, double dz, double up, boolean wasNoAi) {}

    private static final Map<LivingEntity, Freeze> FROZEN = new HashMap<>();

    public static void start(LivingEntity target, int ticks, double strength, double dx, double dz, double up) {
        boolean wasNoAi = target instanceof Mob m && m.isNoAi();
        if (target instanceof Mob m) m.setNoAi(true);
        FROZEN.put(target, new Freeze(target.level().getGameTime() + ticks, strength, dx, dz, up, wasNoAi));
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
    }

    public static void launch(LivingEntity target, double strength, double dx, double dz, double up) {
        // LivingEntity.knockback pushes AWAY from the (x, z) vector, so (dx, dz) points towards the charger.
        target.knockback(strength, dx, dz);
        target.setDeltaMovement(target.getDeltaMovement().add(0, up, 0));
        target.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (FROZEN.isEmpty()) return;
        Iterator<Map.Entry<LivingEntity, Freeze>> it = FROZEN.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            LivingEntity e = entry.getKey();
            Freeze f = entry.getValue();
            if (!e.isAlive()) {
                restore(e, f);
                it.remove();
            } else if (e.level().getGameTime() >= f.end()) {
                restore(e, f);
                launch(e, f.strength(), f.dx(), f.dz(), f.up());
                it.remove();
            } else {
                e.setDeltaMovement(Vec3.ZERO);
                e.hurtMarked = true;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FROZEN.clear();
    }

    private static void restore(LivingEntity e, Freeze f) {
        if (e instanceof Mob m && !f.wasNoAi()) m.setNoAi(false);
    }

    private BashFreeze() {}
}
