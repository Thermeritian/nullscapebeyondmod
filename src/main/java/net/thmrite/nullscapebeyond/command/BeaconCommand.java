package net.thmrite.nullscapebeyond.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.BeaconEntity;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class BeaconCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        // /nsb beacon open all | open <targets>
        // /nsb beacon delete all | delete <targets>
        LiteralArgumentBuilder<CommandSourceStack> open = Commands.literal("open")
                .then(Commands.literal("all").executes(ctx -> open(ctx, allBeacons(ctx.getSource().getServer()))))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> open(ctx, fromSelection(EntityArgument.getEntities(ctx, "targets")))));

        LiteralArgumentBuilder<CommandSourceStack> delete = Commands.literal("delete")
                .then(Commands.literal("all").executes(ctx -> delete(ctx, allBeacons(ctx.getSource().getServer()))))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> delete(ctx, fromSelection(EntityArgument.getEntities(ctx, "targets")))));

        event.getDispatcher().register(Commands.literal("nsb")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("beacon").then(open).then(delete)));
    }

    private static int open(CommandContext<CommandSourceStack> ctx, List<BeaconEntity> beacons) {
        int count = 0;
        for (BeaconEntity beacon : beacons) {
            if (beacon.open()) count++;
        }
        int total = count;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Opened " + total + " beacon(s) (" + beacons.size() + " matched)"), true);
        return total;
    }

    private static int delete(CommandContext<CommandSourceStack> ctx, List<BeaconEntity> beacons) {
        for (BeaconEntity beacon : beacons) {
            beacon.discard();
        }
        int total = beacons.size();
        ctx.getSource().sendSuccess(() -> Component.literal("Deleted " + total + " beacon(s)"), true);
        return total;
    }

    private static List<BeaconEntity> allBeacons(MinecraftServer server) {
        List<BeaconEntity> list = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof BeaconEntity beacon) list.add(beacon);
            }
        }
        return list;
    }

    private static List<BeaconEntity> fromSelection(Collection<? extends Entity> entities) {
        List<BeaconEntity> list = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof BeaconEntity beacon) list.add(beacon);
        }
        return list;
    }
}