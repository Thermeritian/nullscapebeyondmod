package net.thmrite.nullscapebeyond.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
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
        // /nsb level_block set <mode>          -> block you are looking at
        // /nsb level_block set <pos> <mode>    -> block at pos
        // /nsb level_block set all <mode>      -> every level_objet in loaded chunks around you
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");
        LiteralArgumentBuilder<CommandSourceStack> all = Commands.literal("all");
        RequiredArgumentBuilder<CommandSourceStack, ?> posArg =
                Commands.argument("pos", BlockPosArgument.blockPos());

        for (Mode mode : Mode.values()) {
            String name = mode.getSerializedName();
            set.then(Commands.literal(name).executes(ctx -> setLooked(ctx, mode)));
            all.then(Commands.literal(name).executes(ctx -> setAll(ctx, mode)));
            posArg.then(Commands.literal(name).executes(
                    ctx -> setAt(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), mode)));
        }
        set.then(all).then(posArg);

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
        if (!applyMode(ctx.getSource().getLevel(), pos, mode)) {
            ctx.getSource().sendFailure(Component.literal("No level_objet at " + pos.toShortString()));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "level_objet at " + pos.toShortString() + " set to " + mode.getSerializedName()), true);
        return 1;
    }

    private static int setAll(CommandContext<CommandSourceStack> ctx, Mode mode) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        int radius = level.getServer().getPlayerList().getViewDistance();

        // Collect first, modify afterwards.
        List<BlockPos> targets = new ArrayList<>();
        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (entry.getValue() instanceof LevelObjectBlockEntity) {
                        targets.add(entry.getKey());
                    }
                }
            }
        }

        int count = 0;
        for (BlockPos pos : targets) {
            if (applyMode(level, pos, mode)) count++;
        }
        int total = count;
        source.sendSuccess(() -> Component.literal(
                "Set " + total + " level_objet block(s) to " + mode.getSerializedName()), true);
        return total;
    }

    private static boolean applyMode(ServerLevel level, BlockPos pos, Mode mode) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LevelObjectBlock)) return false;
        level.setBlock(pos, state.setValue(LevelObjectBlock.MODE, mode)
                .setValue(LevelObjectBlock.COLLECTED, false), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof LevelObjectBlockEntity be) {
            be.reset();
        }
        return true;
    }
}