package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

import net.thmrite.nullscapebeyond.entity.BeaconEntity;
import net.thmrite.nullscapebeyond.entity.ChargerPlatformEntity;
import net.thmrite.nullscapebeyond.entity.MartEntity;
import net.thmrite.nullscapebeyond.entity.WaterJoltEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NullscapeBeyond.MODID);

    //Beacon
    public static final DeferredHolder<EntityType<?>, EntityType<BeaconEntity>> BEACON =
            ENTITIES.register("beacon", () -> EntityType.Builder
                    .<BeaconEntity>of(BeaconEntity::new, MobCategory.MISC)
                    .sized(2.0f, 0.2f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .noSave()
                    .fireImmune()
                    .build("beacon"));

    // Charger Platform
    public static final DeferredHolder<EntityType<?>, EntityType<ChargerPlatformEntity>> CHARGER_PLATFORM =
            ENTITIES.register("charger_platform", () -> EntityType.Builder
                    .<ChargerPlatformEntity>of(ChargerPlatformEntity::new, MobCategory.MISC)
                    .sized(2.0f, 0.2f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .noSave()
                    .fireImmune()
                    .build("charger_platform"));

    // Mart
    public static final DeferredHolder<EntityType<?>, EntityType<MartEntity>> MART =
            ENTITIES.register("mart", () -> EntityType.Builder
                    .<MartEntity>of(MartEntity::new, MobCategory.CREATURE)
                    .sized(0.8f, 0.8f)
                    .eyeHeight(0.5f)
                    .clientTrackingRange(10)
                    .build("mart"));

    // Water Jolt
    public static final DeferredHolder<EntityType<?>, EntityType<WaterJoltEntity>> WATER_JOLT =
            ENTITIES.register("water_jolt", () -> EntityType.Builder
                    .<WaterJoltEntity>of(WaterJoltEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .noSave()
                    .fireImmune()
                    .build("water_jolt"));

    private ModEntities() {}
}