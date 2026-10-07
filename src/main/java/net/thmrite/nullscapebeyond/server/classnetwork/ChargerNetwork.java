package net.thmrite.nullscapebeyond.server.classnetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;
import net.thmrite.nullscapebeyond.registry.ModSounds;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class ChargerNetwork {
    private static final Map<Player, Map<Integer, Long>> LAST_KNOCK = new WeakHashMap<>();

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ChargerEventPayload.TYPE, ChargerEventPayload.CODEC, ChargerNetwork::handle);
    }

    private static void handle(ChargerEventPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp) || !ChargerBoots.isEquipped(sp)) return;
            switch (msg.kind()) {
                case START -> sound(sp, ModSounds.CHARGER_CHARGE.get());
                case BRAKE -> sound(sp, ModSounds.CHARGER_BRAKE.get());
                case BONK -> {
                    sound(sp, ModSounds.CHARGER_BONK.get());
                    double res = ClassAttributes.BONK_RESISTANCE.of(sp);
                    double dmg = ClassAttributes.BONK_DAMAGE.of(sp) * (1.0 - res);
                    if (dmg > 0) sp.hurt(sp.damageSources().flyIntoWall(), (float) dmg);
                }
                case KNOCK -> knock(sp, msg);
            }
        });
    }

    /** Plays to everyone except the sender (the sender already played it locally). */
    private static void sound(ServerPlayer sp, SoundEvent event) {
        sp.level().playSound(sp, sp.getX(), sp.getY(), sp.getZ(), event, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void knock(ServerPlayer sp, ChargerEventPayload msg) {
        Entity ent = sp.level().getEntity(msg.targetId());
        if (!(ent instanceof LivingEntity target) || !target.isAlive() || sp.distanceToSqr(target) > 25) return;

        double chargeSpeed = ClassAttributes.CHARGE_SPEED.of(sp);
        double speed = Math.min(msg.speed(), chargeSpeed * 1.5); // clamp what the client claims
        if (speed < ClassAttributes.KNOCK_MIN_SPEED.of(sp) * 0.9) return;

        long now = sp.level().getGameTime();
        Map<Integer, Long> map = LAST_KNOCK.computeIfAbsent(sp, k -> new HashMap<>());
        Long last = map.get(target.getId());
        if (last != null && now - last < ClassAttributes.KNOCK_HIT_COOLDOWN.of(sp)) return;
        map.put(target.getId(), now);

        double dmg = ClassAttributes.KNOCK_DAMAGE.of(sp);
        if (dmg > 0) target.hurt(sp.damageSources().playerAttack(sp), (float) dmg);

        double strength = ClassAttributes.KNOCK_STRENGTH.of(sp) * (speed / chargeSpeed);
        // LivingEntity.knockback pushes AWAY from the (x, z) vector, so pass the vector towards the charger.
        target.knockback(strength, sp.getX() - target.getX(), sp.getZ() - target.getZ());
        target.setDeltaMovement(target.getDeltaMovement().add(0, ClassAttributes.KNOCK_UPWARD.of(sp), 0));
        target.hurtMarked = true;
    }

    private ChargerNetwork() {}
}
