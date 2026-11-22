package net.thmrite.nullscapebeyond;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;
import net.thmrite.nullscapebeyond.attribute.ModAttributes;
import net.thmrite.nullscapebeyond.registry.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NullscapeBeyond.MODID)
public class NullscapeBeyond {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "nscapebeyond";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public NullscapeBeyond(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        ModWoodTypes.init();
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addSignBlocks);

        ModSounds.SOUNDS.register(modEventBus);
        ModBlockSounds.SOUND_EVENTS.register(modEventBus);
        ModTreeDecorators.DECORATORS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBoats.ENTITY_TYPES.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModFluidsWater.FLUID_TYPES.register(modEventBus);
        ModFluidsWater.FLUIDS.register(modEventBus);
        ModFluids.init();

        ClassAttributes.ATTRIBUTES.register(modEventBus);
        ModAttributes.register(modEventBus);

    }

    private void addSignBlocks(BlockEntityTypeAddBlocksEvent event) {          // net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent
        ModWoodSet.ALL.forEach(set -> set.addSignBlocks(event));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //event.enqueueWork(ColoredLights::registerVanillaDefaults);
        event.enqueueWork(() -> ModWoodSet.ALL.forEach(ModWoodSet::registerFlammables));
        event.enqueueWork(ModBlocks::registerPottedPlants);

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ModWoodTypes.ALL.forEach(net.minecraft.client.renderer.Sheets::addWoodType));
        }
    }
}
