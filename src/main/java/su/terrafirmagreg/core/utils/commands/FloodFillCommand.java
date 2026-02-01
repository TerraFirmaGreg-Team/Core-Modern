package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.atmosphere.RoomScan;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.utils.atmosphere.*;

public class FloodFillCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /tfg floodfill - run from player position with default config
        dispatcher.register(
                literal("tfg")
                        .then(literal("floodfill").requires(c -> c.hasPermission(2))
                                .executes(c -> {
                                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                                    return runFloodFill(c.getSource(), pos, FloodFillConfig.TIER_0, false);
                                })
                                // /tfg floodfill <tier>
                                .then(argument("tier", IntegerArgumentType.integer(0, 2))
                                        .executes(c -> {
                                            BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                                            int tier = IntegerArgumentType.getInteger(c, "tier");
                                            return runFloodFill(c.getSource(), pos, FloodFillConfig.forTier(tier), false);
                                        })
                                        // /tfg floodfill <tier> <pos>
                                        .then(argument("pos", BlockPosArgument.blockPos())
                                                .executes(c -> {
                                                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                                    int tier = IntegerArgumentType.getInteger(c, "tier");
                                                    return runFloodFill(c.getSource(), pos, FloodFillConfig.forTier(tier), false);
                                                })
                                                .then(literal("diag").requires(c -> c.hasPermission(2))
                                                        .executes(c -> {
                                                            BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                                            int tier = IntegerArgumentType.getInteger(c, "tier");
                                                            return runFloodFill(c.getSource(), pos, FloodFillConfig.forTier(tier), true);

                                                        })))))
                        .then(literal("uncacheable")
                                .executes(c -> {
                                    findUncacheableBlocks();
                                    return 1;
                                })));
    }

    private static int runFloodFill(CommandSourceStack source, BlockPos start, FloodFillConfig config, boolean diagnostic) {
        ServerLevel level = source.getLevel();

        source.sendSuccess(() -> Component.literal(String.format(
                "Running flood fill from %s with max %d blocks...",
                start.toShortString(), config.maxBlocks())), false);

        RoomScan result;
        long startTime = System.nanoTime();
        if (diagnostic) {
            result = DiagnosticFloodFill.fill(level, level, start, config);
        } else {
            result = FloodFill.fill(level, level, start, config);
        }
        long elapsed = System.nanoTime() - startTime;

        source.sendSuccess(() -> Component.literal(String.format(
                "Status: %s", result.status())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "Interior: %d blocks, Envelope: %d blocks",
                result.interiorSize(), result.envelopeSize())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "Time: %.2f ms", elapsed / 1_000_000.0)), false);

        if (result.hasEscapePoint()) {
            BlockPos escape = result.escapePoint();
            source.sendSuccess(() -> Component.literal(String.format(
                    "Escape point: %s", escape.toShortString())), false);
            if (diagnostic && result.escapePath() != null) {
                LeakTrace.spawn(level, result.escapePath());
            }
        }

        return 1;
    }

    public static void findUncacheableBlocks() {
        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            if (state.is(TFGTags.Blocks.AtmospherePassable) || state.is(TFGTags.Blocks.AtmosphereImpassable)) {
                continue;
            }
            try {
                state.getCollisionShape(null, BlockPos.ZERO);
            } catch (NullPointerException e) {
                TFGCore.LOGGER.warn("Uncacheable block: {}", BuiltInRegistries.BLOCK.getKey(block));
            }
        }
    }
}
