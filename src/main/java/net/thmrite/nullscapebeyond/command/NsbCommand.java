package net.thmrite.nullscapebeyond.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock.Mode;
import net.thmrite.nullscapebeyond.block.entity.LevelObjectBlockEntity;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class NsbCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");
        RequiredArgumentBuilder<CommandSourceStack, ?> posArg =
                Commands.argument("pos", BlockPosArgument.blockPos());

        for (Mode mode : Mode.values()) {
            String name = mode.getSerializedName();
            set.then(Commands.literal(name).executes(ctx -> setLooked(ctx, mode)));
            posArg.then(Commands.literal(name).executes(
                    ctx -> setAt(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), mode)));
        }
        set.then(posArg);

        event.getDispatcher().register(Commands.literal("nsb")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("level_block").then(set)));
    }

    private static int setLooked(CommandContext<CommandSourceStack> ctx, Mode mode) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        HitResult hit = player.pick(20.0D, 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            return setAt(ctx, blockHit.getBlockPos(), mode);
        }
        ctx.getSource().sendFailure(Component.literal("You are not looking at a level_objet."));
        return 0;
    }

    private static int setAt(CommandContext<CommandSourceStack> ctx, BlockPos pos, Mode mode) {
        ServerLevel level = ctx.getSource().getLevel();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LevelObjectBlock)) {
            ctx.getSource().sendFailure(Component.literal("No level_objet at " + pos.toShortString()));
            return 0;
        }
        level.setBlock(pos, state.setValue(LevelObjectBlock.MODE, mode)
                .setValue(LevelObjectBlock.COLLECTED, false), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof LevelObjectBlockEntity be) {
            be.reset();
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "level_objet at " + pos.toShortString() + " set to " + mode.getSerializedName()), true);
        return 1;
    }
}