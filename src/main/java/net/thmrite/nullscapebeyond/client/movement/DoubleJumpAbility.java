package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.movement.ModAttributes;

/** Example ability: extra jumps in mid-air, driven entirely by attributes. */
public final class DoubleJumpAbility implements MovementAbility {
    @Override
    public void tick(LocalPlayer player, MovementState state) {
        // Only on a fresh key press while airborne.
        if (state.grounded || state.wasJumpHeld || !player.input.jumping) return;

        int max = (int) player.getAttributeValue(ModAttributes.MAX_AIR_JUMPS);
        if (state.airJumpsUsed >= max) return;

        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x, player.getAttributeValue(ModAttributes.AIR_JUMP_STRENGTH), v.z);
        player.fallDistance = 0;
        state.airJumpsUsed++;
    }
}