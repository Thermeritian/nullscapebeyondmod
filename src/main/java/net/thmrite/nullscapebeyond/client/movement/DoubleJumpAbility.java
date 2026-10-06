package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.attribute.ModAttributes;
import net.thmrite.nullscapebeyond.registry.ModSounds;

public final class DoubleJumpAbility implements MovementAbility {
    private static void playLocal(LocalPlayer p, SoundEvent sound) {
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f, false);
    }

    @Override
    public void tick(LocalPlayer player, MovementState state) {
        if (state.grounded || state.wasJumpHeld || !player.input.jumping) return;

        int max = (int) player.getAttributeValue(ModAttributes.MAX_AIR_JUMPS);
        if (state.airJumpsUsed >= max) return;

        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x, player.getAttributeValue(ModAttributes.AIR_JUMP_STRENGTH), v.z);
        player.fallDistance = 0;

        //playLocal(player, ModSounds.GENERIC_AIRJUMP.get());
        state.airJumpsUsed++;


    }
}