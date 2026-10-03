package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;


public class MovementState {
    public boolean grounded;
    public int ticksSinceGrounded;

    public int airJumpsUsed;
    public int dashCooldownTicks;
    public boolean wasJumpHeld;

    public void update(LocalPlayer player) {
        grounded = player.onGround() || player.isInWater() || player.onClimbable();

        if (grounded) {
            airJumpsUsed = 0;
            ticksSinceGrounded = 0;
        } else {
            ticksSinceGrounded++;
        }

        if (dashCooldownTicks > 0) {
            dashCooldownTicks--;
        }
    }

    public void endTick(LocalPlayer player) {
        wasJumpHeld = player.input.jumping;
    }

    public void reset() {
        grounded = false;
        ticksSinceGrounded = 0;
        airJumpsUsed = 0;
        dashCooldownTicks = 0;
        wasJumpHeld = false;
    }
}