package net.thmrite.nullscapebeyond.client.classcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.thmrite.nullscapebeyond.registry.ModSounds;

/** The looping slide sound: follows the player and fades out as the slide loses speed; stops when the slide ends. */
public class DiverSlideSound extends AbstractTickableSoundInstance {
    public DiverSlideSound() {
        super(ModSounds.DIVER_SLIDE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
    }

    @Override
    public void tick() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null || !p.isAlive() || !DiverController.sliding) {
            stop();
            return;
        }
        this.x = p.getX();
        this.y = p.getY();
        this.z = p.getZ();
        float t = Mth.clamp(DiverController.speed, 0.25f, 1.0f); // quieter and lower as it slows down
        this.volume = t;
        this.pitch = 0.7f + 0.3f * t;
    }
}
