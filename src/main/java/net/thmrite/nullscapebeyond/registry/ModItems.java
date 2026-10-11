package net.thmrite.nullscapebeyond.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import net.thmrite.nullscapebeyond.item.MartBucketItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NullscapeBeyond.MODID);

    public static final DeferredItem<ChargerBoots> CHARGER_BOOTS =
            ITEMS.register("charger_boots", () -> new ChargerBoots(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<DeferredSpawnEggItem> MART_SPAWN_EGG =
            ITEMS.register("mart_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.MART, 0x3A7BD5, 0x9BE3FF, new Item.Properties()));

    public static final DeferredItem<MartBucketItem> MART_BUCKET =
            ITEMS.register("mart_bucket", () -> new MartBucketItem(new Item.Properties().stacksTo(1)));



    private ModItems() {}
}
