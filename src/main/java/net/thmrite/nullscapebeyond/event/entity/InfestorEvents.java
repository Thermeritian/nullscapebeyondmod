package net.thmrite.nullscapebeyond.event.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.InfestorEntity;
import net.thmrite.nullscapebeyond.registry.ModEntities;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class InfestorEvents {

    @SubscribeEvent
    static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.INFESTOR.get(), InfestorEntity.createAttributes().build());
    }

    /**
     * Cows, chickens etc. have no attack attributes, and MeleeAttackGoal crashes without them.
     * Give every living entity type a 0-valued attack damage / knockback so voidbound animals can bite.
     */
    @SubscribeEvent
    static void onAttributeModify(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (!event.has(type, Attributes.ATTACK_DAMAGE)) event.add(type, Attributes.ATTACK_DAMAGE, 0.0D);
            if (!event.has(type, Attributes.ATTACK_KNOCKBACK)) event.add(type, Attributes.ATTACK_KNOCKBACK, 0.0D);
        }
    }

    @SubscribeEvent
    static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.INFESTOR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }
}