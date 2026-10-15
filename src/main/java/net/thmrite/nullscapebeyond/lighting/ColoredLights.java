package net.thmrite.nullscapebeyond.lighting;

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
import net.thmrite.nullscapebeyond.lighting.api.LightColor;
import net.thmrite.nullscapebeyond.lighting.api.LightEmission;

/**
 * Public facade / registry of the coloured lighting library. Safe to call from common code
 * (it only stores data; all rendering lives in the client package).
 *
 * Anything you own can instead implement {@link net.thmrite.nullscapebeyond.lighting.api.LightSource}
 * on its BlockEntity / Entity class.
 */
public final class ColoredLights {
    private ColoredLights() {}

    private static final Map<Block, Function<BlockState, LightEmission>> BLOCKS = new ConcurrentHashMap<>();
    private static final Map<BlockEntityType<?>, Function<BlockEntity, LightEmission>> BLOCK_ENTITIES = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, Function<Entity, LightEmission>> ENTITIES = new ConcurrentHashMap<>();
    private static final Map<Item, LightEmission> HELD_ITEMS = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------- registration

    /** Every state of {@code block} emits {@code emission}. */
    public static void block(Block block, LightEmission emission) {
        BLOCKS.put(block, state -> emission);
    }

    /** State-dependent emission; return {@code null} for "off" (e.g. unlit). */
    public static void block(Block block, Function<BlockState, LightEmission> provider) {
        BLOCKS.put(block, provider);
    }

    /** For block entities you don't own. For your own, implement LightSource instead. */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> void blockEntity(BlockEntityType<T> type, Function<? super T, LightEmission> provider) {
        BLOCK_ENTITIES.put(type, be -> provider.apply((T) be));
    }

    /** For entities you don't own. For your own, implement LightSource instead. */
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

    // ---------------------------------------------------------------- vanilla tints

    private static Function<BlockState, LightEmission> whenLit(LightEmission e) {
        return s -> s.hasProperty(BlockStateProperties.LIT) && !s.getValue(BlockStateProperties.LIT) ? null : e;
    }

    /** Gives vanilla light sources a colour. Call once from common setup (enqueueWork). */
    public static void registerVanillaDefaults() {
        LightEmission torch = LightEmission.of(LightColor.TORCH, 10f, 0.8f);
        LightEmission soul = LightEmission.of(LightColor.SOUL, 10f, 0.8f);
        LightEmission redstone = LightEmission.of(LightColor.of(255, 40, 20), 6f, 0.7f);
        LightEmission lava = LightEmission.of(LightColor.of(255, 100, 25), 12f, 1.0f);
        LightEmission glow = LightEmission.of(LightColor.of(255, 215, 120), 14f, 0.9f);

        block(Blocks.TORCH, torch);
        block(Blocks.WALL_TORCH, torch);
        block(Blocks.LANTERN, LightEmission.of(LightColor.of(255, 175, 90), 12f, 0.85f));
        block(Blocks.FIRE, LightEmission.of(LightColor.of(255, 140, 50), 10f, 0.9f));
        block(Blocks.CAMPFIRE, whenLit(LightEmission.of(LightColor.of(255, 140, 60), 12f, 0.9f)));
        block(Blocks.SOUL_TORCH, soul);
        block(Blocks.SOUL_WALL_TORCH, soul);
        block(Blocks.SOUL_LANTERN, LightEmission.of(LightColor.SOUL, 12f, 0.85f));
        block(Blocks.SOUL_FIRE, LightEmission.of(LightColor.SOUL, 10f, 0.9f));
        block(Blocks.SOUL_CAMPFIRE, whenLit(LightEmission.of(LightColor.SOUL, 12f, 0.9f)));
        block(Blocks.REDSTONE_TORCH, whenLit(redstone));
        block(Blocks.REDSTONE_WALL_TORCH, whenLit(redstone));
        block(Blocks.LAVA, lava);
        block(Blocks.GLOWSTONE, glow);
        block(Blocks.SHROOMLIGHT, LightEmission.of(LightColor.of(255, 160, 80), 14f, 0.9f));
        block(Blocks.JACK_O_LANTERN, LightEmission.of(LightColor.of(255, 150, 40), 14f, 0.9f));
        block(Blocks.SEA_LANTERN, LightEmission.of(LightColor.of(150, 235, 255), 14f, 0.9f));
        block(Blocks.END_ROD, LightEmission.of(LightColor.of(235, 235, 255), 14f, 0.8f));
        block(Blocks.OCHRE_FROGLIGHT, LightEmission.of(LightColor.of(255, 215, 110), 14f, 0.9f));
        block(Blocks.VERDANT_FROGLIGHT, LightEmission.of(LightColor.of(130, 255, 140), 14f, 0.9f));
        block(Blocks.PEARLESCENT_FROGLIGHT, LightEmission.of(LightColor.of(235, 150, 255), 14f, 0.9f));

        heldItem(Items.TORCH, torch.withRadius(8f));
        heldItem(Items.SOUL_TORCH, soul.withRadius(8f));
        heldItem(Items.LANTERN, LightEmission.of(LightColor.of(255, 175, 90), 9f, 0.85f));
        heldItem(Items.SOUL_LANTERN, LightEmission.of(LightColor.SOUL, 9f, 0.85f));
        heldItem(Items.GLOWSTONE, glow.withRadius(9f));
        heldItem(Items.LAVA_BUCKET, lava.withRadius(8f));

        entity(EntityType.BLAZE, e -> LightEmission.of(LightColor.of(255, 150, 40), 9f, 0.9f));
        entity(EntityType.MAGMA_CUBE, e -> LightEmission.of(LightColor.of(255, 90, 20), 7f, 0.8f));
        entity(EntityType.GLOW_SQUID, e -> LightEmission.of(LightColor.of(60, 220, 200), 8f, 0.8f));
    }
}