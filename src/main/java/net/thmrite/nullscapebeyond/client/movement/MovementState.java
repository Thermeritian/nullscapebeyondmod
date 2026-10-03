package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;

/**
 * Per-tick runtime data for the local player. Client-side only for now.
 * Add new fields here as abilities need them (wall contact, dash direction, ...).
 */
public class MovementState {
    /** On ground, in water or on a climbable: counts as "touching something". */
    public boolean grounded;
    public int ticksSinceGrounded;

    public int airJumpsUsed;
    public int dashCooldownTicks;

    /** Jump key state from the previous tick, for edge detection (press vs. hold). */
    public boolean wasJumpHeld;

    /** Called at the start of every player tick, before abilities run. */
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

    /** Called at the end of every player tick, after abilities ran. */
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