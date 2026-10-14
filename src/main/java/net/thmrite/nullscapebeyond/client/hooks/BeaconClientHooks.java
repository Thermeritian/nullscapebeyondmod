package net.thmrite.nullscapebeyond.client.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.thmrite.nullscapebeyond.entity.BeaconEntity;
import net.thmrite.nullscapebeyond.registry.ModSounds;

public final class BeaconClientHooks {
    public static void startIdleSound(BeaconEntity beacon) {
        Minecraft.getInstance().getSoundManager().play(new IdleSound(beacon));
    }

    /** Looping ambience that lives as long as the beacon is open. */
    private static class IdleSound extends AbstractTickableSoundInstance {
        private final BeaconEntity beacon;

        IdleSound(BeaconEntity beacon) {
            super(ModSounds.BEACON_IDLE.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.beacon = beacon;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.x = beacon.getX();
            this.y = beacon.getY();
            this.z = beacon.getZ();
        }

        @Override
        public void tick() {
            if (beacon.isRemoved() || !beacon.isOpen()) {
                stop();
            }
        }
    }

    private BeaconClientHooks() {}
}