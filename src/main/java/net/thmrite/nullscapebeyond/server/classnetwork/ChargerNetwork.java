package net.thmrite.nullscapebeyond.server.classnetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;
import net.thmrite.nullscapebeyond.entity.ChargerPlatformEntity;
import net.thmrite.nullscapebeyond.registry.ModEntities;
import net.thmrite.nullscapebeyond.registry.ModSounds;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class ChargerNetwork {
    private static final Map<Player, Map<Integer, Long>> LAST_KNOCK = new WeakHashMap<>();
    private static final Map<UUID, ChargerPlatformEntity> PLATFORMS = new HashMap<>();

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ChargerEventPayload.TYPE, ChargerEventPayload.CODEC, ChargerNetwork::handle);
    }

    private static void handle(ChargerEventPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp) || !ChargerBoots.isEquipped(sp)) return;
            switch (msg.kind()) {
                case START -> sound(sp, ModSounds.CHARGER_CHARGE.get());
                case BRAKE -> {
                    sound(sp, ModSounds.CHARGER_BRAKE.get());
                    endPlatform(sp);
                }
                case PLATFORM_START -> startPlatform(sp, msg.value());
                case PLATFORM_END -> endPlatform(sp);
                case BONK -> {
                    endPlatform(sp);
                    sound(sp, ModSounds.CHARGER_BONK.get());
                    double res = ClassAttributes.BONK_RESISTANCE.of(sp);
                    double dmg = ClassAttributes.BONK_DAMAGE.of(sp) * (1.0 - res);
                    if (dmg > 0) sp.hurt(sp.damageSources().flyIntoWall(), (float) dmg);
                }
                case KNOCK -> hit(sp, msg, false);
                case SHIELD_BASH -> {
                    if (sp.isBlocking()) hit(sp, msg, true);
                }
            }
        });
    }

    private static void startPlatform(ServerPlayer sp, float y) {
        endPlatform(sp);
        ChargerPlatformEntity platform = new ChargerPlatformEntity(ModEntities.CHARGER_PLATFORM.get(), sp.level());
        platform.setOwner(sp);
        // Do not trust the client with an arbitrary height
        platform.setPos(sp.getX(), Mth.clamp(y, sp.getY() - 3.0, sp.getY() + 1.0), sp.getZ());
        sp.serverLevel().addFreshEntity(platform);
        PLATFORMS.put(sp.getUUID(), platform);
    }

    private static void endPlatform(ServerPlayer sp) {
        ChargerPlatformEntity old = PLATFORMS.remove(sp.getUUID());
        if (old != null && !old.isRemoved()) old.discard();
    }

    /** Plays to everyone except the sender (the sender already played it locally). */
    private static void sound(ServerPlayer sp, SoundEvent event) {
        sp.level().playSound(sp, sp.getX(), sp.getY(), sp.getZ(), event, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * Charge impact on an entity. bash = the charger is blocking with a shield (any shield that declares
     * NeoForge's SHIELD_BLOCK ability, since that is what isBlocking() checks): big damage, big knockback
     * (held back during a short freeze), and heavy durability damage to the shield.
     */
    private static void hit(ServerPlayer sp, ChargerEventPayload msg, boolean bash) {
        Entity ent = sp.level().getEntity(msg.targetId());
        if (!(ent instanceof LivingEntity target) || !target.isAlive() || sp.distanceToSqr(target) > 25) return;

        double chargeSpeed = ClassAttributes.CHARGE_SPEED.of(sp);
        double speed = Math.min(msg.value(), chargeSpeed * 1.5); // clamp what the client claims
        if (speed < ClassAttributes.KNOCK_MIN_SPEED.of(sp) * 0.9) return;

        long now = sp.level().getGameTime();
        Map<Integer, Long> map = LAST_KNOCK.computeIfAbsent(sp, k -> new HashMap<>());
        Long last = map.get(target.getId());
        if (last != null && now - last < ClassAttributes.KNOCK_HIT_COOLDOWN.of(sp)) return;
        map.put(target.getId(), now);

        double dmg = bash ? ClassAttributes.SHIELD_BASH_DAMAGE.of(sp) : ClassAttributes.KNOCK_DAMAGE.of(sp);
        double base = bash ? ClassAttributes.SHIELD_BASH_KNOCKBACK.of(sp) : ClassAttributes.KNOCK_STRENGTH.of(sp);
        double up = bash ? ClassAttributes.SHIELD_BASH_UPWARD.of(sp) : ClassAttributes.KNOCK_UPWARD.of(sp);
        double strength = base * (speed / chargeSpeed);
        double dx = sp.getX() - target.getX();
        double dz = sp.getZ() - target.getZ();

        if (dmg > 0) target.hurt(sp.damageSources().playerAttack(sp), (float) dmg);

        if (!bash) {
            BashFreeze.launch(target, strength, dx, dz, up);
            return;
        }

        // Shield bash extras
        InteractionHand hand = sp.getUsedItemHand();
        ItemStack shield = sp.getItemInHand(hand);
        int wear = (int) ClassAttributes.SHIELD_BASH_DURABILITY.of(sp);
        if (wear > 0 && shield.isDamageableItem()) {
            shield.hurtAndBreak(wear, sp, LivingEntity.getSlotForHand(hand));
        }
        sp.level().playSound(sp, target.getX(), target.getY(), target.getZ(),
                ModSounds.SHIELD_BASH.get(), SoundSource.PLAYERS, 1.2f, 1.0f);

        int freeze = (int) ClassAttributes.SHIELD_BASH_FREEZE.of(sp);
        if (freeze > 0 && target.isAlive()) {
            BashFreeze.start(target, freeze, strength, dx, dz, up);
        } else {
            BashFreeze.launch(target, strength, dx, dz, up);
        }
    }

    private ChargerNetwork() {}
}
