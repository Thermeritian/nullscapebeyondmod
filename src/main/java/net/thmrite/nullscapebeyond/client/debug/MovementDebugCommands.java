package net.thmrite.nullscapebeyond.client.debug;

import com.mojang.brigadier.Command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.movement.MovementHandler;


@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class MovementDebugCommands {
    private MovementDebugCommands() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("nsb")
                        .then(Commands.literal("movement")
                                .executes(ctx -> {
                                    boolean on = MovementDebugHud.toggle();
                                    message(Component.literal("Movement HUD: " + (on ? "ON" : "OFF")));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.literal("dump").executes(ctx -> {
                                    LocalPlayer player = Minecraft.getInstance().player;
                                    if (player != null) {
                                        for (String line : MovementDebugHud.buildLines(player)) {
                                            message(Component.literal(line));
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(Commands.literal("reset").executes(ctx -> {
                                    MovementHandler.STATE.reset();
                                    message(Component.literal("Movement state reset"));
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static void message(Component component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(component, false);
        }
    }
}