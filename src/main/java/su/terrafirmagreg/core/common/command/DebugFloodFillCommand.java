package su.terrafirmagreg.core.common.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

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
import su.terrafirmagreg.core.common.environment.PressureFloodFill;
import su.terrafirmagreg.core.common.environment.PressureRoomScan;
import su.terrafirmagreg.core.common.environment.RoomScan;

/**
 * /tfg debug floodfill [maxBlocks] [pos]
 * /tfg debug floodfill diag [maxBlocks] [pos] : Spawn trace
 * /tfg debug uncacheable
 */
public class DebugFloodFillCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("floodfill")
                // /tfg debug floodfill
                .executes(c -> {
                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                    return runFloodFill(c.getSource(), pos, 1_000_000, 256, false);
                })
                // /tfg debug floodfill diag [maxBlocks] [pos]
                .then(literal("diag")
                        .executes(c -> {
                            BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                            return runFloodFill(c.getSource(), pos, 1_000_000, 256, true);
                        })
                        .then(argument("maxBlocks", IntegerArgumentType.integer(0, 1_000_000))
                                .executes(c -> {
                                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                                    int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                    return runFloodFill(c.getSource(), pos, maxBlocks, 256, true);
                                })
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(c -> {
                                            BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                            int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                            return runFloodFill(c.getSource(), pos, maxBlocks, 256, true);
                                        }))))
                // /tfg debug floodfill <maxBlocks> [pos]
                .then(argument("maxBlocks", IntegerArgumentType.integer(0, 1_000_000))
                        .executes(c -> {
                            BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                            int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                            return runFloodFill(c.getSource(), pos, maxBlocks, 256, false);
                        })
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes(c -> {
                                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                    int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                    return runFloodFill(c.getSource(), pos, maxBlocks, 256, false);
                                }))));

        debug.then(literal("pressure")
                // /tfg debug pressure
                .executes(c -> {
                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                    return runPressureFill(c.getSource(), pos, 1_000_000, 256);
                })
                // /tfg debug pressure <maxBlocks> [pos]
                .then(argument("maxBlocks", IntegerArgumentType.integer(0, 1_000_000))
                        .executes(c -> {
                            BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                            int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                            return runPressureFill(c.getSource(), pos, maxBlocks, 256);
                        })
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes(c -> {
                                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                    int maxBlocks = IntegerArgumentType.getInteger(c, "maxBlocks");
                                    return runPressureFill(c.getSource(), pos, maxBlocks, 256);
                                }))));

        debug.then(literal("uncacheable")
                .executes(c -> {
                    findUncacheableBlocks(c.getSource());
                    return 1;
                }));
    }

    private static int runFloodFill(CommandSourceStack source, BlockPos start, int maxBlocks, int maxHorizontalDistance, boolean diagnostic) {
        ServerLevel level = source.getLevel();

        source.sendSuccess(() -> Component.literal(String.format(
                "Running %sflood fill from %s with max %d blocks...",
                diagnostic ? "diagnostic " : "", start.toShortString(), maxBlocks)), false);

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
            assert escape != null;
            source.sendSuccess(() -> Component.literal(String.format(
                    "Escape point: %s", escape.toShortString())), false);
            if (diagnostic && result.escapePath() != null) {
                DiagnosticFloodFill.spawnTrace(level, result.escapePath());
            }
        }

        return 1;
    }

    private static int runPressureFill(CommandSourceStack source, BlockPos start, int maxBlocks, int maxHorizontalDistance) {
        ServerLevel level = source.getLevel();

        source.sendSuccess(() -> Component.literal(String.format(
                "Running pressure flood fill from %s with max %d blocks...",
                start.toShortString(), maxBlocks)), false);

        long startTime = System.nanoTime();
        PressureRoomScan result = PressureFloodFill.fill(level, start, maxBlocks, maxHorizontalDistance,
                TFGTags.Blocks.PressureResistant);
        long elapsed = System.nanoTime() - startTime;

        source.sendSuccess(() -> Component.literal(String.format("Status: %s", result.status())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "Interior: %d blocks, Envelope: %d blocks, Parts: %d",
                result.roomScan().interiorSize(), result.roomScan().envelopeSize(),
                result.partPositions().size())), false);
        source.sendSuccess(() -> Component.literal(String.format("Time: %.2f ms", elapsed / 1_000_000.0)), false);

        if (result.roomScan().hasEscapePoint()) {
            BlockPos escape = result.roomScan().escapePoint();
            assert escape != null;
            source.sendSuccess(() -> Component.literal(String.format("Escape point: %s", escape.toShortString())), false);
        }

        return 1;
    }

    private static void findUncacheableBlocks(CommandSourceStack source) {
        int count = 0;
        for (Block block : ForgeRegistries.BLOCKS) {
            BlockState state = block.defaultBlockState();
            if (state.is(TFGTags.Blocks.AtmosphereImpassable) || state.is(TFGTags.Blocks.AtmospherePassable)) {
                continue;
            }
            try {
                state.getCollisionShape(null, BlockPos.ZERO);
            } catch (NullPointerException e) {
                count++;
                var key = ForgeRegistries.BLOCKS.getKey(block);
                source.sendSuccess(() -> Component.literal("  Uncacheable: " + key), false);
                TFGCore.LOGGER.warn("Uncacheable block: {}", key);
            }
        }
        int finalCount = count;
        source.sendSuccess(() -> Component.literal(String.format("Found %d uncacheable blocks", finalCount)), false);
    }
}
