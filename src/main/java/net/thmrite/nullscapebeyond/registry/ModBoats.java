package net.thmrite.nullscapebeyond.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.ModBoat;
import net.thmrite.nullscapebeyond.entity.ModBoatItem;
import net.thmrite.nullscapebeyond.entity.ModChestBoat;

public final class ModBoats {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NullscapeBeyond.MODID);
    public static final List<BoatSet> ALL = new ArrayList<>();

    public record BoatSet(String name,
                          DeferredHolder<EntityType<?>, EntityType<ModBoat>> boat,
                          DeferredHolder<EntityType<?>, EntityType<ModChestBoat>> chestBoat,
                          DeferredItem<Item> boatItem,
                          DeferredItem<Item> chestBoatItem) {
    }

    public static final BoatSet AZURE = register("azure");
    public static final BoatSet VIRIDIAN = register("viridian");


    private static BoatSet register(String name) {
        DeferredHolder<EntityType<?>, EntityType<ModBoat>> boat = ENTITY_TYPES.register(name + "_boat",
                () -> EntityType.Builder.<ModBoat>of(ModBoat::new, MobCategory.MISC)
                        .sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(name + "_boat"));
        DeferredHolder<EntityType<?>, EntityType<ModChestBoat>> chest = ENTITY_TYPES.register(name + "_chest_boat",
                () -> EntityType.Builder.<ModChestBoat>of(ModChestBoat::new, MobCategory.MISC)
                        .sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(name + "_chest_boat"));
        DeferredItem<Item> boatItem = ModItems.ITEMS.register(name + "_boat",
                () -> new ModBoatItem(boat, new Item.Properties().stacksTo(1)));
        DeferredItem<Item> chestItem = ModItems.ITEMS.register(name + "_chest_boat",
                () -> new ModBoatItem(chest, new Item.Properties().stacksTo(1)));
        BoatSet set = new BoatSet(name, boat, chest, boatItem, chestItem);
        ALL.add(set);
        return set;
    }

    public static Item dropItemFor(EntityType<?> type) {
        for (BoatSet set : ALL) {
            if (type == set.boat().get()) return set.boatItem().get();
            if (type == set.chestBoat().get()) return set.chestBoatItem().get();
        }
        return Items.OAK_BOAT;
    }
}
