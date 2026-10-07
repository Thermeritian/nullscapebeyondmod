package net.thmrite.nullscapebeyond.client.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.ClientConfig;
import net.thmrite.nullscapebeyond.registry.ModSounds;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public class MenuMusic {
    private static final ResourceLocation VANILLA_MENU_MUSIC =
            ResourceLocation.withDefaultNamespace("music.menu");
    private static final int GAP_TICKS = 100; // pause between tracks (5 s)

    private static SoundInstance current;
    private static int cooldown = 0;

    // Silence vanilla's menu music; ours replaces it
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound != null && sound.getLocation().equals(VANILLA_MENU_MUSIC)) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        SoundManager sounds = mc.getSoundManager();

        if (mc.level != null) {                        // in a world: stop menu music
            if (current != null) {
                sounds.stop(current);
                current = null;
            }
            return;
        }
        if (current != null && sounds.isActive(current)) {
            return;                                    // still playing
        }
        if (current != null) {                         // track just ended
            current = null;
            cooldown = GAP_TICKS;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        current = new MenuMusicInstance();
        sounds.play(current);
    }

    private static class MenuMusicInstance extends AbstractTickableSoundInstance {
        MenuMusicInstance() {
            super(ModSounds.SOUNDTRACK_MENU.get(), SoundSource.MASTER, RandomSource.create());
            this.looping = false;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = ClientConfig.SOUNDTRACK_VOLUME.get().floatValue();
        }

        @Override
        public void tick() { // lets the slider change the volume live
            this.volume = ClientConfig.SOUNDTRACK_VOLUME.get().floatValue();
        }
    }
}