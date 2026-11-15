package net.thmrite.nullscapebeyond.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class ModWoodTypes {
    public static final List<WoodType> ALL = new ArrayList<>();

    public static final WoodType AZURE = register("azure");

    private static WoodType register(String name) {
        String id = NullscapeBeyond.MODID + ":" + name;
        BlockSetType setType = BlockSetType.register(new BlockSetType(id));
        WoodType type = WoodType.register(new WoodType(id, setType));
        ALL.add(type);
        return type;
    }

    public static void init() {
    }
}
