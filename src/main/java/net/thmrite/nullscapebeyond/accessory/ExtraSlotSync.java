package net.thmrite.nullscapebeyond.accessory;

import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;

/**
 * Keeps the size of the "extra" slot type equal to the max_extra_slots attribute.
 * The "extra" slot has base amount 0 in its datapack file; this adds the attribute value on top.
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class ExtraSlotSync {
    private static final String SLOT = "extra";
    private static final ResourceLocation MOD_ID =
            ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "max_extra_slots");
    // Keyed by entity instance: a respawned player is a new key, so slots get re-applied.
    private static final Map<ServerPlayer, Integer> APPLIED = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Integer have = APPLIED.get(player);
        if (have != null && player.tickCount % 10 != 0) return;

        int want = Mth.clamp((int) Math.floor(ClassAttributes.MAX_EXTRA_SLOTS.of(player)), 0, 16);
        if (have != null && have == want) return;

        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return;

        if (have != null) cap.removeSlotModifiers(modifier(have));
        cap.addTransientSlotModifiers(modifier(want));
        APPLIED.put(player, want);
    }

    public static Multimap<String, AttributeModifier> modifier(int amount) {
        Multimap<String, AttributeModifier> map = HashMultimap.create();
        map.put(SLOT, new AttributeModifier(MOD_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        return map;
    }

    private ExtraSlotSync() {}
}
