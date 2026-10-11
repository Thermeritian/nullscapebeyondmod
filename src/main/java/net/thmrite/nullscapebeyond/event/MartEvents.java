package net.thmrite.nullscapebeyond.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.renderer.MartRenderer;
import net.thmrite.nullscapebeyond.client.renderer.WaterJoltRenderer;
import net.thmrite.nullscapebeyond.entity.MartEntity;
import net.thmrite.nullscapebeyond.registry.ModEntities;

/** Mart-related event hooks. If you already have a client/mod event class, move the methods there. */
public final class MartEvents {
    private MartEvents() {}

    @EventBusSubscriber(modid = NullscapeBeyond.MODID)
    public static class ModBus {
        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.MART.get(), MartEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            event.register(ModEntities.MART.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MartEntity::checkMartSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.OR);
        }
    }

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static class ClientBus {
        @SubscribeEvent
        public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.MART.get(), MartRenderer::new);
            event.registerEntityRenderer(ModEntities.WATER_JOLT.get(), WaterJoltRenderer::new);
        }
    }

    /** Fire-type mobs (tag nscapebeyond:fire_creatures) killed by a Mart or its jolts drop more. */
    @EventBusSubscriber(modid = NullscapeBeyond.MODID)
    public static class GameBus {
        @SubscribeEvent
        public static void onDrops(LivingDropsEvent event) {
            if (!(event.getSource().getEntity() instanceof MartEntity)) return;
            if (!event.getEntity().getType().is(MartEntity.FIRE_CREATURES)) return;
            RandomSource random = event.getEntity().getRandom();
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (stack.getMaxStackSize() > 1) {
                    stack.grow(1 + random.nextInt(2));
                    drop.setItem(stack);
                }
            }
        }
    }
}