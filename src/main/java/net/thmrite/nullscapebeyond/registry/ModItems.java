package net.thmrite.nullscapebeyond.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NullscapeBeyond.MODID);

    public static final DeferredItem<ChargerBoots> CHARGER_BOOTS =
            ITEMS.register("charger_boots", () -> new ChargerBoots(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
