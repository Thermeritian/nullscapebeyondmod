package net.thmrite.nullscapebeyond.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        event.getGenerator().addProvider(event.includeClient(),
                new ModBlockStateProvider(output, event.getExistingFileHelper()));
    }
}