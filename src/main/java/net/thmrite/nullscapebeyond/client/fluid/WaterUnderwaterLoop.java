package net.thmrite.nullscapebeyond.client.fluid;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.thmrite.nullscapebeyond.fluid.WaterFluidType;

public class WaterUnderwaterLoop extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private int fade;

    public WaterUnderwaterLoop(LocalPlayer player) {
        super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || this.fade < 0) {
            this.stop();
            return;
        }
        if (this.player.getEyeInFluidType() instanceof WaterFluidType) {
            this.fade++;
        } else {
            this.fade -= 2;
        }
        this.fade = Math.min(this.fade, 40);
        this.volume = Math.max(0.0F, Math.min(this.fade / 40.0F, 1.0F));
    }
}
