package net.thmrite.nullscapebeyond.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.entity.LevelObjectBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NullscapeBeyond.MODID);

    public static final Supplier<BlockEntityType<LevelObjectBlockEntity>> LEVEL_OBJECT =
            BLOCK_ENTITIES.register("level_object", () -> BlockEntityType.Builder
                    .of(LevelObjectBlockEntity::new, ModBlocks.LEVEL_OBJET.get())
                    .build(null));

    private ModBlockEntities() {}
}