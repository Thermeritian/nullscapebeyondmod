package net.thmrite.nullscapebeyond.shaders.lighting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.thmrite.nullscapebeyond.shaders.lighting.api.LightColor;
import net.thmrite.nullscapebeyond.shaders.lighting.api.LightEmission;

public final class ColoredLights {
    private ColoredLights() {}

    private static final Map<Block, Function<BlockState, LightEmission>> BLOCKS = new ConcurrentHashMap<>();
    private static final Map<BlockEntityType<?>, Function<BlockEntity, LightEmission>> BLOCK_ENTITIES = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, Function<Entity, LightEmission>> ENTITIES = new ConcurrentHashMap<>();
    private static final Map<Item, LightEmission> HELD_ITEMS = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------- registration

    public static void block(Block block, LightEmission emission) {
        BLOCKS.put(block, state -> emission);
    }

    public static void block(Block block, Function<BlockState, LightEmission> provider) {
        BLOCKS.put(block, provider);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> void blockEntity(BlockEntityType<T> type, Function<? super T, LightEmission> provider) {
        BLOCK_ENTITIES.put(type, be -> provider.apply((T) be));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void entity(EntityType<T> type, Function<? super T, LightEmission> provider) {
        ENTITIES.put(type, e -> provider.apply((T) e));
    }

    /** Living entities holding this item (either hand) emit light. */
    public static void heldItem(Item item, LightEmission emission) {
        HELD_ITEMS.put(item, emission);
    }

    // ---------------------------------------------------------------- lookups (used by the client engine)

    public static boolean hasBlock(BlockState state) {
        return BLOCKS.containsKey(state.getBlock());
    }

    @Nullable
    public static LightEmission blockEmission(BlockState state) {
        Function<BlockState, LightEmission> f = BLOCKS.get(state.getBlock());
        return f == null ? null : f.apply(state);
    }

    @Nullable
    public static LightEmission blockEntityEmission(BlockEntity be) {
        Function<BlockEntity, LightEmission> f = BLOCK_ENTITIES.get(be.getType());
        return f == null ? null : f.apply(be);
    }

    @Nullable
    public static LightEmission entityEmission(Entity entity) {
        Function<Entity, LightEmission> f = ENTITIES.get(entity.getType());
        return f == null ? null : f.apply(entity);
    }

    @Nullable
    public static LightEmission heldEmission(LivingEntity entity) {
        if (HELD_ITEMS.isEmpty()) return null;
        LightEmission a = HELD_ITEMS.get(entity.getMainHandItem().getItem());
        LightEmission b = HELD_ITEMS.get(entity.getOffhandItem().getItem());
        if (a == null) return b;
        if (b == null) return a;
        return a.radius() * a.intensity() >= b.radius() * b.intensity() ? a : b;
    }

    private static Function<BlockState, LightEmission> whenLit(LightEmission e) {
        return s -> s.hasProperty(BlockStateProperties.LIT) && !s.getValue(BlockStateProperties.LIT) ? null : e;
    }
}