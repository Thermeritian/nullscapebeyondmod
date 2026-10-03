package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

public interface MovementController {
    void tick(LocalPlayer player, MovementState state);
    default void onInput(LocalPlayer player, MovementState state, Input input) {}
}