package net.thmrite.nullscapebeyond.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.ClassAccessory;
import net.thmrite.nullscapebeyond.accessory.ClassSensitiveAccessory;
import net.thmrite.nullscapebeyond.accessory.GeoAccessoryItem;
import net.thmrite.nullscapebeyond.accessory.NormalAccessory;
import net.thmrite.nullscapebeyond.accessory.classaccessory.*;
import net.thmrite.nullscapebeyond.item.*;

import java.util.List;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NullscapeBeyond.MODID);

    //accessories




    //class accessories
    public static final DeferredItem<ChargerBoots> CHARGER_BOOTS =
            ITEMS.register("charger_boots", () -> new ChargerBoots(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<DiverHelmet> DIVER_HELMET =
            ITEMS.register("diver_helmet", () -> new DiverHelmet(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<WantedHat> WANTED_HAT =
            ITEMS.register("wanted_hat", () -> new WantedHat(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<GliderKit> GLIDER_KIT =
            ITEMS.register("glider_kit", () -> new GliderKit(new Item.Properties().stacksTo(1)));

    public static List<ClassAccessory> classAccessories() {
        return List.of(CHARGER_BOOTS.get(), DIVER_HELMET.get(), WANTED_HAT.get(), GLIDER_KIT.get());
    }


    //normal accessories
    public static final DeferredItem<NormalAccessory> ORB =
            ITEMS.register("orb", () -> new NormalAccessory(new Item.Properties().stacksTo(1), "extra", "orb"));
    public static final DeferredItem<ClassSensitiveAccessory> SHARK_TAIL =
            ITEMS.register("shark_tail", () -> new ClassSensitiveAccessory(new Item.Properties().stacksTo(1), "extra", "shark_tail"));
    public static final DeferredItem<ClassSensitiveAccessory> NINJA_BELT =
            ITEMS.register("ninja_belt", () -> new ClassSensitiveAccessory(new Item.Properties().stacksTo(1), "extra", "ninja_belt"));
    public static final DeferredItem<NormalAccessory> PROPELLER_HAT =
            ITEMS.register("propeller_hat", () -> new NormalAccessory(new Item.Properties().stacksTo(1), "head", "propeller_hat"));



    public static List<GeoAccessoryItem> geoAccessories() {
        return List.of(CHARGER_BOOTS.get(), DIVER_HELMET.get(), WANTED_HAT.get(), GLIDER_KIT.get(),
                ORB.get(), SHARK_TAIL.get(), NINJA_BELT.get(), PROPELLER_HAT.get());
    }







    //items
    public static final DeferredItem<MartBucketItem> MART_BUCKET =
            ITEMS.register("mart_bucket", () -> new MartBucketItem(new Item.Properties().stacksTo(1)));

    //spawn eggs
    public static final DeferredItem<DeferredSpawnEggItem> MART_SPAWN_EGG =
            ITEMS.register("mart_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.MART, 0x3A7BD5, 0x9BE3FF, new Item.Properties()));
    public static final DeferredItem<Item> INFESTOR_SPAWN_EGG = ITEMS.register("infestor_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.INFESTOR, 0x1B1033, 0xD9C2FF, new Item.Properties()));




    private ModItems() {}
}
