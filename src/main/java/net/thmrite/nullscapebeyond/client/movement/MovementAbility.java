package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;

public interface MovementAbility {
    void tick(LocalPlayer player, MovementState state);
}