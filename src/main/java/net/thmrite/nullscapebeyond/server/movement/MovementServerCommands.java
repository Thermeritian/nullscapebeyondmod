package net.thmrite.nullscapebeyond.server.movement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.attribute.ModAttributes;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class MovementServerCommands {
    private MovementServerCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("nsbserver")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reset")
                                .executes(ctx -> reset(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                        .then(Commands.literal("reload")
                                .executes(ctx -> reload(ctx.getSource()))));
    }

    /** Every attribute that shapes movement: the vanilla ones the HUD shows, plus all of ours. */
    private static List<Holder<Attribute>> movementAttributes() {
        List<Holder<Attribute>> attributes = new ArrayList<>();
        attributes.add(Attributes.GRAVITY);
        attributes.add(Attributes.JUMP_STRENGTH);
        attributes.add(Attributes.MOVEMENT_SPEED);
        attributes.add(Attributes.STEP_HEIGHT);
        attributes.addAll(ModAttributes.ATTRIBUTES.getEntries());
        return attributes;
    }

    private static int reset(CommandSourceStack source, Collection<ServerPlayer> targets) {
        // The player's own default values (e.g. movement speed is 0.1 for players, not the
        // attribute's generic default), including the defaults of our custom attributes.
        AttributeSupplier defaults = DefaultAttributes.getSupplier(EntityType.PLAYER);
        List<Holder<Attribute>> attributes = movementAttributes();

        for (ServerPlayer player : targets) {
            for (Holder<Attribute> attribute : attributes) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null && defaults.hasAttribute(attribute)) {
                    instance.setBaseValue(defaults.getBaseValue(attribute));
                }
            }
        }

        int count = targets.size();
        source.sendSuccess(() -> Component.literal(
                "Reset movement attributes for " + count + (count == 1 ? " player" : " players")), true);
        return count;
    }

    private static int reload(CommandSourceStack source) {
        // TODO: reload movement presets/config here once they exist.
        source.sendSuccess(() -> Component.literal("Movement reload: nothing to reload yet"), false);
        return 1;
    }
}