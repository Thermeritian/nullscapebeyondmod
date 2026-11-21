package net.thmrite.nullscapebeyond.event.fluids;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModFluids;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class NulquaSounds {
    private static final Set<Entity> INSIDE = Collections.newSetFromMap(new WeakHashMap<>()); // server thread only

    private NulquaSounds() {}

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity instanceof LivingEntity)) return;

        boolean inside = entity.isInFluidType(ModFluids.NULQUA_TYPE.get());
        boolean was = INSIDE.contains(entity);

        if (inside && !was) {
            INSIDE.add(entity);
            Vec3 v = entity.getDeltaMovement();
            float volume = Math.min(1.0F, (float) Math.sqrt(v.x * v.x * 0.2 + v.y * v.y + v.z * v.z * 0.2) * 0.5F + 0.2F);
            boolean player = entity instanceof Player;
            var sound = volume < 0.5F || !player ? (player ? SoundEvents.PLAYER_SPLASH : SoundEvents.GENERIC_SPLASH) : SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(),
                    volume, 1.0F + (entity.level().random.nextFloat() - entity.level().random.nextFloat()) * 0.4F);
        } else if (!inside && was) {
            INSIDE.remove(entity);
        } else if (inside && entity.tickCount % 10 == 0 && entity.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    entity instanceof Player ? SoundEvents.PLAYER_SWIM : SoundEvents.GENERIC_SWIM, entity.getSoundSource(), 0.4F, 1.0F);
        }
    }
}