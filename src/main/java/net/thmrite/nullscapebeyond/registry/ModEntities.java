package net.thmrite.nullscapebeyond.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

import net.thmrite.nullscapebeyond.entity.ChargerPlatformEntity;

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

    private ModEntities() {}
}
