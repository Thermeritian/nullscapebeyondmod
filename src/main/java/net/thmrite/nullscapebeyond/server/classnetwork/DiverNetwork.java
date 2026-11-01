package net.thmrite.nullscapebeyond.server.classnetwork;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.DiverHelmet;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;
import net.thmrite.nullscapebeyond.registry.ModSounds;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class DiverNetwork {
    private static final Set<UUID> POSING = new HashSet<>();

    public static void handle(DiverEventPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp) || !DiverHelmet.isEquipped(sp)) return;
            switch (msg.kind()) {
                case DIVE -> sound(sp, ModSounds.DIVER_DIVE.get());
                case JUMP_OUT -> sound(sp, ModSounds.DIVER_JUMP.get());
                case BONK -> {
                    sound(sp, ModSounds.CHARGER_BONK.get()); // shared bonk sound
                    double dmg = ClassAttributes.BONK_DAMAGE.of(sp) * (1.0 - ClassAttributes.BONK_RESISTANCE.of(sp));
                    if (dmg > 0) sp.hurt(sp.damageSources().flyIntoWall(), (float) dmg);
                }
                case POSE_ON -> POSING.add(sp.getUUID());
                case POSE_OFF -> POSING.remove(sp.getUUID());
            }
        });
    }

    private static void sound(ServerPlayer sp, SoundEvent event) {
        sp.level().playSound(sp, sp.getX(), sp.getY(), sp.getZ(), event, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || POSING.isEmpty() || !POSING.contains(sp.getUUID())) return;
        if (!DiverHelmet.isEquipped(sp)) {
            POSING.remove(sp.getUUID());
            return;
        }
        sp.setPose(Pose.SWIMMING);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        POSING.remove(event.getEntity().getUUID());
    }

    private DiverNetwork() {}
}
