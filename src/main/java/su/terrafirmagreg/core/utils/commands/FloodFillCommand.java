package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.environment.DiagnosticFloodFill;
import su.terrafirmagreg.core.common.environment.FloodFill;
import su.terrafirmagreg.core.common.environment.RoomScan;

public class FloodFillCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /tfg floodfill - run from player position with default config
        dispatcher.register(
                literal("tfg")
                        .then(literal("floodfill").requires(c -> c.hasPermission(2))
                                .executes(c -> {
                                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                                    return runFloodFill(c.getSource(), pos, 1_000_000, 256, false);
                                })
                                // /tfg floodfill <maxBlocks>
                                .then(argument("maxBlocks", IntegerArgumentType.integer(0, 1_000_000))
                                        .executes(c -> {
                                            BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                                            int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                            return runFloodFill(c.getSource(), pos, maxBlocks, 256, false);
                                        })
                                        // /tfg floodfill <maxBlocks> <pos>
                                        .then(argument("pos", BlockPosArgument.blockPos())
                                                .executes(c -> {
                                                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                                    int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                                    return runFloodFill(c.getSource(), pos, maxBlocks, 256, false);
                                                })
                                                .then(literal("diag").requires(c -> c.hasPermission(2))
                                                        .executes(c -> {
                                                            BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                                            int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                                            return runFloodFill(c.getSource(), pos, maxBlocks, 256, true);

                                                        })))))
                        .then(literal("uncacheable")
                                .executes(c -> {
                                    findUncacheableBlocks();
                                    return 1;
                                })));
    }

    private static int runFloodFill(CommandSourceStack source, BlockPos start, int maxBlocks, int maxHorizontalDistance, boolean diagnostic) {
        ServerLevel level = source.getLevel();

        source.sendSuccess(() -> Component.literal(String.format(
                "Running flood fill from %s with max %d blocks...",
                start.toShortString(), maxBlocks)), false);

        RoomScan result;
        long startTime = System.nanoTime();
        if (diagnostic) {
            result = DiagnosticFloodFill.fill(level, start, maxBlocks, maxHorizontalDistance);
        } else {
            result = FloodFill.fill(level, start, maxBlocks, maxHorizontalDistance);
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
                DiagnosticFloodFill.spawnTrace(level, result.escapePath());
            }
        }

        return 1;
    }

    public static void findUncacheableBlocks() {
        for (Block block : ForgeRegistries.BLOCKS) {
            BlockState state = block.defaultBlockState();
            if (state.is(TFGTags.Blocks.AtmospherePassable) || state.is(TFGTags.Blocks.AtmosphereImpassable)) {
                continue;
            }
            try {
                state.getCollisionShape(null, BlockPos.ZERO);
            } catch (NullPointerException e) {
                TFGCore.LOGGER.warn("Uncacheable block: {}", ForgeRegistries.BLOCKS.getKey(block));
            }
        }
    }
}
