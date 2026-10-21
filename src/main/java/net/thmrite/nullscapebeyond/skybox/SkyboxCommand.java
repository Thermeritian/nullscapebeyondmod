package net.thmrite.nullscapebeyond.skybox;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/**
 * /nsb skybox set <skybox> [targets]
 * /nsb skybox clear [targets]
 * /nsb skybox effect motion <direction> [speed] [targets]
 * /nsb skybox effect pulsing <r|g|b> <time> [targets]
 * /nsb skybox effect clear [targets]
 *
 * Registering a second "nsb" literal is fine: Brigadier merges it into your existing /nsb root.
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class SkyboxCommand {
    private static final float DEFAULT_SPEED = 5f; // degrees per second
    private static final String[] CHANNELS = {"r", "g", "b"};

    private static final DynamicCommandExceptionType UNKNOWN =
            new DynamicCommandExceptionType(o -> Component.literal("Unknown option: " + o));

    private static final SuggestionProvider<CommandSourceStack> SKYBOXES = (c, b) ->
            SharedSuggestionProvider.suggest(Arrays.stream(SkyboxType.values()).map(SkyboxType::id), b);
    private static final SuggestionProvider<CommandSourceStack> DIRECTIONS = (c, b) ->
            SharedSuggestionProvider.suggest(Arrays.stream(SkyboxPayload.Direction.values()).map(d -> d.name().toLowerCase()), b);
    private static final SuggestionProvider<CommandSourceStack> CHANNEL_SUGGESTIONS = (c, b) ->
            SharedSuggestionProvider.suggest(CHANNELS, b);

    private SkyboxCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nsb").then(skybox()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> skybox() {
        return Commands.literal("skybox")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("skybox", StringArgumentType.word()).suggests(SKYBOXES)
                                .executes(c -> set(c, false))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(c -> set(c, true)))))
                .then(Commands.literal("clear")
                        .executes(c -> send(c, false, SkyboxPayload.clear(), "Skybox cleared"))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(c -> send(c, true, SkyboxPayload.clear(), "Skybox cleared"))))
                .then(Commands.literal("effect")
                        .then(Commands.literal("motion")
                                .then(Commands.argument("direction", StringArgumentType.word()).suggests(DIRECTIONS)
                                        .executes(c -> motion(c, DEFAULT_SPEED, false))
                                        .then(Commands.argument("speed", FloatArgumentType.floatArg(0.01f, 360f))
                                                .executes(c -> motion(c, FloatArgumentType.getFloat(c, "speed"), false))
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(c -> motion(c, FloatArgumentType.getFloat(c, "speed"), true))))))
                        .then(Commands.literal("pulsing")
                                .then(Commands.argument("channel", StringArgumentType.word()).suggests(CHANNEL_SUGGESTIONS)
                                        .then(Commands.argument("time", TimeArgument.time(2))
                                                .executes(c -> pulse(c, false))
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(c -> pulse(c, true))))))
                        .then(Commands.literal("clear")
                                .executes(c -> send(c, false, SkyboxPayload.effectClear(), "Skybox effects cleared"))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(c -> send(c, true, SkyboxPayload.effectClear(), "Skybox effects cleared")))));
    }

    private static int set(CommandContext<CommandSourceStack> c, boolean targets) throws CommandSyntaxException {
        String name = StringArgumentType.getString(c, "skybox");
        SkyboxType type = SkyboxType.byId(name);
        if (type == null) throw UNKNOWN.create(name);
        return send(c, targets, SkyboxPayload.set(type), "Skybox set to " + type.id());
    }

    private static int motion(CommandContext<CommandSourceStack> c, float speed, boolean targets) throws CommandSyntaxException {
        String name = StringArgumentType.getString(c, "direction");
        SkyboxPayload.Direction dir = null;
        for (SkyboxPayload.Direction d : SkyboxPayload.Direction.values()) {
            if (d.name().equalsIgnoreCase(name)) dir = d;
        }
        if (dir == null) throw UNKNOWN.create(name);
        return send(c, targets, SkyboxPayload.motion(dir, speed),
                "Skybox motion: " + dir.name().toLowerCase() + " at " + speed + " deg/s");
    }

    private static int pulse(CommandContext<CommandSourceStack> c, boolean targets) throws CommandSyntaxException {
        String name = StringArgumentType.getString(c, "channel");
        int channel = Arrays.asList(CHANNELS).indexOf(name.toLowerCase());
        if (channel < 0) throw UNKNOWN.create(name);
        float seconds = IntegerArgumentType.getInteger(c, "time") / 20f; // TimeArgument yields ticks
        return send(c, targets, SkyboxPayload.pulse(channel, seconds),
                "Skybox pulsing " + name.toUpperCase() + " every " + seconds + "s");
    }

    private static int send(CommandContext<CommandSourceStack> c, boolean hasTargets, SkyboxPayload payload, String feedback)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = hasTargets
                ? EntityArgument.getPlayers(c, "targets")
                : List.of(c.getSource().getPlayerOrException());
        for (ServerPlayer p : players) {
            PacketDistributor.sendToPlayer(p, payload);
        }
        c.getSource().sendSuccess(() -> Component.literal(feedback), true);
        return players.size();
    }
}