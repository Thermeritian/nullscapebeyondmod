package net.thmrite.nullscapebeyond.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.menu.FlashWarningScreen;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public class ClientEvents {

    //Warning
    private static boolean warningShown = false;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!warningShown && ClientConfig.SHOW_FLASH_WARNING.get() && event.getNewScreen() instanceof TitleScreen) {
            warningShown = true;
            event.setNewScreen(new FlashWarningScreen(event.getNewScreen()));
        }
    }


    //Splash Text
    private static final ResourceLocation SPLASHES =
            ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "texts/splashes.txt");
    private static final RandomSource RANDOM = RandomSource.create();
    @SubscribeEvent
    public static void onTitleInit(ScreenEvent.Init.Pre event) {
        // Only set it if nothing is set yet, so window resizes keep the same splash
        if (event.getScreen() instanceof TitleScreen title && title.splash == null) {
            List<String> splashes = loadSplashes();
            if (!splashes.isEmpty()) {
                title.splash = new SplashRenderer(splashes.get(RANDOM.nextInt(splashes.size())));
            }
        }
    }
    private static List<String> loadSplashes() {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(SPLASHES);
        if (resource.isEmpty()) {
            NullscapeBeyond.LOGGER.warn("Missing splash file: {}", SPLASHES);
            return List.of();
        }
        try (BufferedReader reader = resource.get().openAsReader()) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            NullscapeBeyond.LOGGER.error("Failed to read splash file", e);
            return List.of();
        }
    }
}