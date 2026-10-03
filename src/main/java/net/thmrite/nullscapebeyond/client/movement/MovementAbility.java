package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;

/**
 * One movement move (double jump, dash, wall slide...). Abilities hold no data of their own:
 * numbers come from attributes, runtime data lives in {@link MovementState}.
 * An ability simply does nothing when its attributes are zero, so accessories only
 * need to grant attribute modifiers.
 */
public interface MovementAbility {
    void tick(LocalPlayer player, MovementState state);
}