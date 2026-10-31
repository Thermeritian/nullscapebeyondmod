package net.thmrite.nullscapebeyond.event.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thmrite.nullscapebeyond.server.effects.VoidboundSyncPayload;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.debug.VoidboundDebug;
import net.thmrite.nullscapebeyond.effect.VoidboundEffect;
import net.thmrite.nullscapebeyond.effect.VoidboundHelper;
import net.thmrite.nullscapebeyond.entity.InfestorEntity;
import net.thmrite.nullscapebeyond.registry.ModEntities;
import net.thmrite.nullscapebeyond.registry.ModSounds;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class VoidboundEvents {

    /** Effect just applied -> turn the mob hostile. */
    @SubscribeEvent
    static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEffectInstance().getEffect().value() instanceof VoidboundEffect
                && event.getEntity() instanceof Mob mob) {
            VoidboundDebug.log("SERVER: voidbound added to {} (id {})", mob.getType().toShortString(), mob.getId());
            VoidboundHelper.applyAggression(mob);
            // vanilla does not tell clients about effects added to mobs, so do it ourselves
            PacketDistributor.sendToPlayersTrackingEntity(mob, new VoidboundSyncPayload(mob.getId(), true));
        }
    }

    /** A player starts seeing an already-voidbound mob (walked into range, joined, chunk loaded). */
    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity target && VoidboundHelper.hasVoidbound(target)
                && event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new VoidboundSyncPayload(target.getId(), true));
        }
    }

    /** Goals are not saved, so re-install them when a voidbound mob is loaded back into the world. */
    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Mob mob && VoidboundHelper.hasVoidbound(mob)) {
            VoidboundHelper.applyAggression(mob);
        }
    }

    /** Host dies -> the infestor leaves its body and has to wait 20 s before infesting again. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDeath(LivingDeathEvent event) {
        LivingEntity host = event.getEntity();
        if (!(host.level() instanceof ServerLevel level)) return;
        var data = host.getPersistentData();
        if (!data.contains(VoidboundHelper.KEY_INFESTOR_HEALTH)) return;

        float health = data.getFloat(VoidboundHelper.KEY_INFESTOR_HEALTH);
        data.remove(VoidboundHelper.KEY_INFESTOR_HEALTH);

        InfestorEntity infestor = ModEntities.INFESTOR.get().create(level);
        if (infestor == null) return;
        infestor.moveTo(host.getX(), host.getY(), host.getZ(), host.getYRot(), 0.0F);
        infestor.setHealth(Math.max(1.0F, Math.min(health, infestor.getMaxHealth())));
        infestor.setInfestCooldown(InfestorEntity.EXIT_COOLDOWN_TICKS);
        infestor.setPersistenceRequired();
        level.addFreshEntity(infestor);
        level.playSound(null, host.blockPosition(), ModSounds.INFESTOR_EXITS.get(), SoundSource.HOSTILE, 1.2F, 1.0F);
    }

    /** Void-aligned things never target each other. */
    @SubscribeEvent
    static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget != null && VoidboundHelper.isVoidAligned(event.getEntity())
                && VoidboundHelper.isVoidAligned(newTarget)) {
            event.setCanceled(true);
        }
    }

    /** ...and never hurt each other either. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var attacker = event.getSource().getEntity();
        if (attacker != null && VoidboundHelper.isVoidAligned(attacker)
                && VoidboundHelper.isVoidAligned(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}