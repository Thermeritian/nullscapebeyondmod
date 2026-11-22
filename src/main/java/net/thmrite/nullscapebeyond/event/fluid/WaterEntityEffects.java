package net.thmrite.nullscapebeyond.event.fluid;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.fluid.WaterFluidType;
import net.thmrite.nullscapebeyond.registry.ModFluidsWater;


@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class WaterEntityEffects {
    private static final Map<Entity, WaterFluidType> CURRENT = Collections.synchronizedMap(new WeakHashMap<>());

    private WaterEntityEffects() {
    }

    @Nullable
    private static WaterFluidType currentType(Entity entity) {
        for (ModFluidsWater.Entry entry : ModFluidsWater.ALL) {
            WaterFluidType type = entry.type().get();
            if (entity.isInFluidType(type)) return type;
        }
        return null;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || ModFluidsWater.ALL.isEmpty()) return;
        Level level = entity.level();

        WaterFluidType now = currentType(entity);
        WaterFluidType before = CURRENT.get(entity);

        if (now != null && now != before) {            // entered (or moved from one water-like fluid into another)
            CURRENT.put(entity, now);
            if (level.isClientSide) {
                splashParticles(entity, level, now);
            } else {
                splashSound(entity, level);
            }
        } else if (now == null && before != null) {    // left
            CURRENT.remove(entity);
        } else if (now != null) {                      // still inside
            if (level.isClientSide) {
                swimBubbles(entity, level, now);
            } else if (entity.tickCount % 10 == 0 && entity.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        entity instanceof Player ? SoundEvents.PLAYER_SWIM : SoundEvents.GENERIC_SWIM, entity.getSoundSource(), 0.4F, 1.0F);
            }
        }
    }

    private static void splashSound(Entity entity, Level level) {
        Vec3 v = entity.getDeltaMovement();
        float volume = Math.min(1.0F, (float) Math.sqrt(v.x * v.x * 0.2 + v.y * v.y + v.z * v.z * 0.2) * 0.5F + 0.2F);
        boolean player = entity instanceof Player;
        SoundEvent sound = !player ? SoundEvents.GENERIC_SPLASH
                : (volume < 0.5F ? SoundEvents.PLAYER_SPLASH : SoundEvents.PLAYER_SPLASH_HIGH_SPEED);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(),
                volume, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
    }

    /** Same shape as vanilla's water splash: a burst of bubbles and droplets at the surface. */
    private static void splashParticles(Entity entity, Level level, WaterFluidType type) {
        RandomSource random = level.random;
        Vec3 v = entity.getDeltaMovement();
        float width = entity.getBbWidth();
        double surface = Mth.floor(entity.getY()) + 1.0;
        for (int i = 0; i < 1.0F + width * 20.0F; i++) {
            double dx = (random.nextDouble() * 2.0 - 1.0) * width;
            double dz = (random.nextDouble() * 2.0 - 1.0) * width;
            level.addParticle(type.getBubbleParticle(), entity.getX() + dx, surface, entity.getZ() + dz, v.x, v.y - random.nextDouble() * 0.2, v.z);
        }
        for (int j = 0; j < 1.0F + width * 20.0F; j++) {
            double dx = (random.nextDouble() * 2.0 - 1.0) * width;
            double dz = (random.nextDouble() * 2.0 - 1.0) * width;
            level.addParticle(type.getSplashParticle(), entity.getX() + dx, surface, entity.getZ() + dz, v.x, v.y, v.z);
        }
    }

    private static void swimBubbles(Entity entity, Level level, WaterFluidType type) {
        if (!(entity.getEyeInFluidType() instanceof WaterFluidType)) return;
        if (entity.getDeltaMovement().lengthSqr() < 0.002 || level.random.nextInt(2) != 0) return;
        RandomSource random = level.random;
        double x = entity.getX() + (random.nextDouble() - 0.5) * entity.getBbWidth();
        double y = entity.getEyeY() - 0.2 + (random.nextDouble() - 0.5) * 0.3;
        double z = entity.getZ() + (random.nextDouble() - 0.5) * entity.getBbWidth();
        level.addParticle(type.getBubbleParticle(), x, y, z, 0.0, 0.0, 0.0);
    }
}
