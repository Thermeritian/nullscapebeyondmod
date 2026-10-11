package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

import net.thmrite.nullscapebeyond.entity.ChargerPlatformEntity;
import net.thmrite.nullscapebeyond.entity.MartEntity;
import net.thmrite.nullscapebeyond.entity.WaterJoltEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NullscapeBeyond.MODID);

    // Hitbox size only matters for tracking/culling; the platform has no collision. Match it to your model.
    public static final DeferredHolder<EntityType<?>, EntityType<ChargerPlatformEntity>> CHARGER_PLATFORM =
            ENTITIES.register("charger_platform", () -> EntityType.Builder
                    .<ChargerPlatformEntity>of(ChargerPlatformEntity::new, MobCategory.MISC)
                    .sized(2.0f, 0.2f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .noSave()
                    .fireImmune()
                    .build("charger_platform"));

    // Base size is for scale 1.0 (empty, unmerged). Water level and merging scale it via the SCALE attribute.
    // Adjust to match mart.geo.json.
    public static final DeferredHolder<EntityType<?>, EntityType<MartEntity>> MART =
            ENTITIES.register("mart", () -> EntityType.Builder
                    .<MartEntity>of(MartEntity::new, MobCategory.CREATURE)
                    .sized(0.8f, 0.8f)
                    .eyeHeight(0.5f)
                    .clientTrackingRange(10)
                    .build("mart"));

    // Tiny hitbox, 1-tick updates so the client follows the bolt smoothly.
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