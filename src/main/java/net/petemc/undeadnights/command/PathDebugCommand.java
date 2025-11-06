package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.util.Helpers;

import java.util.List;

public class PathDebugCommand {

    public PathDebugCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("path")
                .then(Commands.literal("find")
                    .executes(ctx -> findPathLookedAt(ctx.getSource()))
                    .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                            .then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(ctx -> findPathToCoords(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))))))));
    }

    private int findPathLookedAt(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

         // Ray trace from player to find a block they're looking at (max 50 blocks)
         HitResult hit = player.pick(50.0D, 0.0F, false);
         if (hit.getType() != HitResult.Type.BLOCK) {
             player.sendSystemMessage(Component.literal("No block in sight within 50 blocks."));
             return 0;
         }
         BlockHitResult bhr = (BlockHitResult) hit;
         BlockPos target = bhr.getBlockPos();
         return computeAndReportPath(source, player.blockPosition(), target);
     }

     private int findPathToCoords(CommandSourceStack source, int x, int y, int z) throws CommandSyntaxException {
         ServerPlayer player = source.getPlayerOrException();
         BlockPos target = new BlockPos(x, y, z);
         return computeAndReportPath(source, player.blockPosition(), target);
     }

     private int computeAndReportPath(CommandSourceStack source, BlockPos start, BlockPos end) throws CommandSyntaxException {
         ServerPlayer player = source.getPlayerOrException();
         Level level = source.getLevel();

         // parameters: small mob
         float mobWidth = 0.6f;
         float mobHeight = 1.8f;
         int maxNodes = 8000;

         UndeadNights.LOGGER.debug("PathDebugCommand: finding path from {} to {} (mob {}/{})", start, end, mobWidth, mobHeight);
         List<BlockPos> path = Helpers.findPathAStar(level, start, end, mobWidth, mobHeight, maxNodes, 4, 1, 0);
         if (path.isEmpty()) {
             player.sendSystemMessage(Component.literal("No path found (debug)."));
             UndeadNights.LOGGER.debug("PathDebugCommand: no path found from {} to {}", start, end);
             return 0;
         }

         StringBuilder sb = new StringBuilder();
         sb.append("Path found: length=").append(path.size()).append('\n');
         int limit = Math.min(path.size(), 50);
         for (int i = 0; i < limit; i++) {
             BlockPos p = path.get(i);
             sb.append(i).append(": ").append(p.toShortString()).append('\n');
         }
         if (path.size() > limit) sb.append("... (truncated)\n");

         player.sendSystemMessage(Component.literal(sb.toString()));
         UndeadNights.LOGGER.debug("PathDebugCommand: path found length={} (printed {} entries)", path.size(), limit);
         return 1;
     }
 }
