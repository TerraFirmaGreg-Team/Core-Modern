package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.environment.PassabilityChecker;
import su.terrafirmagreg.core.common.environment.PassabilityChecker.PassInfo;
import su.terrafirmagreg.core.common.environment.PassabilityChecker.PassInfo.PassType;

/**
 * /tfg debug passability <pos>      : check passability of block at position
 * /tfg debug passability block <id> : check passability of block by resource key
 * /tfg debug passcache              : dump entire passability cache
 */
public class DebugPassabilityCommand {

    private static final TagKey<Item> HIDDEN_TAG = TagKey.create(
            ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath("c", "hidden_from_recipe_viewers"));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("passability")
                // /tfg debug passability <pos>
                .then(argument("pos", BlockPosArgument.blockPos())
                        .executes(c -> {
                            BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                            ServerLevel level = c.getSource().getLevel();
                            BlockState state = level.getBlockState(pos);
                            return showPassInfo(c.getSource(), state, pos);
                        })
                        .then(literal("shape")
                                .executes(c -> {
                                    BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
                                    ServerLevel level = c.getSource().getLevel();
                                    BlockState state = level.getBlockState(pos);
                                    return dumpShape(c.getSource(), level, state, pos);
                                })))
                // /tfg debug passability block <id>
                .then(literal("block")
                        .then(argument("id", ResourceLocationArgument.id())
                                .executes(c -> {
                                    ResourceLocation id = ResourceLocationArgument.getId(c, "id");
                                    return showPassInfoByKey(c.getSource(), id);
                                }))));

        debug.then(literal("passcache")
                .executes(c -> dumpCache(c.getSource())));

        //        debug.then(literal("suspicious")
        //                .executes(c -> findSuspicious(c.getSource())));
    }

    private static int showPassInfo(CommandSourceStack source, BlockState state, BlockPos pos) {
        var key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        PassInfo info = PassabilityChecker.getCachedPassInfo(state);

        source.sendSuccess(() -> Component.literal(String.format(
                "Block: %s at %s", key, pos.toShortString())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "  State: %s", state)), false);
        printPassInfo(source, info);
        return 1;
    }

    private static int showPassInfoByKey(CommandSourceStack source, ResourceLocation rl) {
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        if (block == null) {
            source.sendFailure(Component.literal("Unknown block: " + rl));
            return 0;
        }
        BlockState state = block.defaultBlockState();
        PassInfo info = PassabilityChecker.getCachedPassInfo(state);

        source.sendSuccess(() -> Component.literal(String.format("Block: %s", rl)), false);
        source.sendSuccess(() -> Component.literal(String.format("  State: %s", state)), false);
        printPassInfo(source, info);
        return 1;
    }

    private static void printPassInfo(CommandSourceStack source, PassInfo info) {
        source.sendSuccess(() -> Component.literal(String.format(
                "  Type: %s", info.type())), false);

        if (info.type() == PassType.COLLISION) {
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Open faces: %s", dirMask(info.openFaces()))), false);
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Open silhouettes: %s", dirMask(info.openSilhouettes()))), false);
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Full faces: %s", dirMask(info.fullFaces()))), false);
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Full silhouettes: %s", dirMask(info.fullSilhouettes()))), false);
        }
    }

    private static int dumpCache(CommandSourceStack source) {
        var cache = PassabilityChecker.getCache();
        if (cache.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Passability cache is empty"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal(String.format(
                "Passability cache (%d entries):", cache.size())), false);

        for (Map.Entry<BlockState, PassInfo> entry : cache.entrySet()) {
            BlockState state = entry.getKey();
            PassInfo info = entry.getValue();
            var key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            String suffix = info.type() == PassType.COLLISION
                    ? String.format(" faces=%s sil=%s", dirMask(info.fullFaces()), dirMask(info.fullSilhouettes()))
                    : "";
            source.sendSuccess(() -> Component.literal(String.format(
                    "  %s [%s]%s", key, info.type(), suffix)), false);
        }

        return 1;
    }

    private static int dumpShape(CommandSourceStack source, ServerLevel level, BlockState state, BlockPos pos) {
        var key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.empty());

        source.sendSuccess(() -> Component.literal(String.format("Shape dump: %s at %s", key, pos.toShortString())), false);

        if (shape.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Shape: EMPTY"), false);
            return 1;
        }

        // AABBs
        var boxes = shape.toAabbs();
        source.sendSuccess(() -> Component.literal(String.format("  AABBs (%d):", boxes.size())), false);
        for (var aabb : boxes) {
            source.sendSuccess(() -> Component.literal(String.format(
                    "    (%.3f, %.3f, %.3f) -> (%.3f, %.3f, %.3f)",
                    aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)), false);
        }

        source.sendSuccess(() -> Component.literal(String.format(
                "  isFullBlock: %s", Block.isShapeFullBlock(shape))), false);

        // Per-face analysis
        for (Direction dir : Direction.values()) {
            // getFaceShape uses the outward-looking convention
            Direction outward = dir.getOpposite();
            VoxelShape faceShape = shape.getFaceShape(outward);
            boolean full = PassabilityChecker.isFullFace(faceShape);
            boolean empty = PassabilityChecker.isEmptyFace(faceShape);
            String faceStatus = full ? "FULL" : empty ? "EMPTY" : "PARTIAL";
            var faceBoxes = faceShape.toAabbs();
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Face %s (incoming): %s (%d aabbs)", dir.getName(), faceStatus, faceBoxes.size())), false);
            for (var aabb : faceBoxes) {
                source.sendSuccess(() -> Component.literal(String.format(
                        "      (%.3f, %.3f, %.3f) -> (%.3f, %.3f, %.3f)",
                        aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)), false);
            }
        }

        // Per-axis silhouette
        for (Direction.Axis axis : Direction.Axis.values()) {
            boolean full = PassabilityChecker.hasFullSilhouette(shape, axis);
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Silhouette %s: %s", axis.getName(), full ? "FULL" : "OPEN")), false);
        }

        return 1;
    }

    /** Find all blocks for which getCollisionShape doesn't match getShape */
    private static int findSuspicious(CommandSourceStack source) {
        StringBuilder tsv = new StringBuilder();
        tsv.append("block\tname\tcollision\toutline\n");
        int count = 0;
        for (Block block : ForgeRegistries.BLOCKS) {
            BlockState state = block.defaultBlockState();

            if (state.is(TFGTags.Blocks.AtmosphereImpassable)
                    || state.is(TFGTags.Blocks.AtmospherePassable)
                    || state.is(TFGTags.Blocks.AtmosphereUseOutline)) {
                continue;
            }

            // Skip blocks hidden from recipe viewers — typically uncraftable/creative-only
            var item = block.asItem();
            if (item != net.minecraft.world.item.Items.AIR && item.builtInRegistryHolder().is(HIDDEN_TAG)) {
                continue;
            }

            VoxelShape collision;
            VoxelShape outline;
            try {
                collision = state.getCollisionShape(null, BlockPos.ZERO);
                outline = state.getShape(null, BlockPos.ZERO);
            } catch (NullPointerException e) {
                continue;
            }

            try {
                if (Shapes.joinIsNotEmpty(collision, outline, BooleanOp.NOT_SAME)) {
                    count++;
                    var key = ForgeRegistries.BLOCKS.getKey(block);
                    String name = block.getName().getString();
                    String collStr = Block.isShapeFullBlock(collision) ? "FULL" : collision.isEmpty() ? "EMPTY" : "PARTIAL";
                    String outStr = Block.isShapeFullBlock(outline) ? "FULL" : outline.isEmpty() ? "EMPTY" : "PARTIAL";
                    tsv.append("'").append(key).append("',\t").append(name).append('\t').append(collStr).append('\t').append(outStr).append('\n');
                }
            } catch (Exception e) {
                continue;
            }
        }

        Path outFile = Path.of("suspicious_blocks.tsv");
        try {
            Files.writeString(outFile, tsv.toString());
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write file: " + e.getMessage()));
            return 0;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal(String.format(
                "Wrote %d suspicious blocks to %s", finalCount, outFile.toAbsolutePath())), false);
        return 1;
    }

    private static String dirMask(byte mask) {
        if (mask == 0)
            return "none";
        StringBuilder sb = new StringBuilder();
        for (Direction dir : Direction.values()) {
            if ((mask & (1 << dir.ordinal())) != 0) {
                if (!sb.isEmpty())
                    sb.append(", ");
                sb.append(dir.getName());
            }
        }
        return sb.toString();
    }
}
