package su.terrafirmagreg.core.common.data.events;

import java.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.PacketDistributor;

import lombok.Getter;

import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.OreHighlightPacket;
import su.terrafirmagreg.core.network.packet.OreHighlightVeinPacket;

public class AdvancedOreProspectorEventHelper {

    // distance required to consider ores part of different veins
    private static final int VEIN_SEPARATION_RADIUS = 10;

    @Getter
    private final double length;
    @Getter
    private final double halfWidth;
    @Getter
    private final double halfHeight;
    @Getter
    private final TagKey<Item> itemTag;
    @Getter
    private final TagKey<Block> oreTag = net.minecraftforge.common.Tags.Blocks.ORES;

    // toggle whether to send one particle per ore block, or one per vein center
    @Getter
    private final boolean centersOnly;

    public AdvancedOreProspectorEventHelper(double length, double halfWidth, double halfHeight, TagKey<Item> itemTag,
            boolean centersOnly) {
        this.length = length;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.itemTag = itemTag;
        this.centersOnly = centersOnly;
    }

    public void handleRightClick(PlayerInteractEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide())
            return;

        ItemStack held = player.getItemInHand(event.getHand());
        if (!held.is(itemTag))
            return;
        if (player.getCooldowns().isOnCooldown(held.getItem()))
            return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle().normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = lookDir.cross(up).normalize();
        if (right.lengthSqr() == 0)
            right = new Vec3(1, 0, 0);
        up = right.cross(lookDir).normalize();

        Set<BlockPos> checkedPositions = new HashSet<>();
        Map<Component, Integer> oreCounts = new HashMap<>();
        List<BlockPos> orePositions = new ArrayList<>();

        int stepsX = (int) Math.ceil(halfWidth) * 2;
        int stepsY = (int) Math.ceil(halfHeight) * 2;
        int stepsZ = (int) Math.ceil(length);

        for (int ix = -stepsX / 2; ix <= stepsX / 2; ix++) {
            for (int iy = -stepsY / 2; iy <= stepsY / 2; iy++) {
                for (int iz = 0; iz <= stepsZ; iz++) {
                    Vec3 localPos = right.scale(ix + 0.5)
                            .add(up.scale(iy + 0.5))
                            .add(lookDir.scale(iz + 0.5));
                    Vec3 worldPos = eyePos.add(localPos);

                    BlockPos pos = BlockPos.containing(worldPos);
                    if (!checkedPositions.add(pos))
                        continue;

                    Block block = level.getBlockState(pos).getBlock();
                    if (block.defaultBlockState().is(oreTag)) {
                        Component name = block.getName();
                        oreCounts.put(name, oreCounts.getOrDefault(name, 0) + 1);
                        orePositions.add(pos);
                    }
                }
            }
        }

        if (oreCounts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("tfg.toast.ore_prospector_none")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            int totalCount = oreCounts.values().stream().mapToInt(Integer::intValue).sum();
            player.sendSystemMessage(
                    Component.translatable("tfg.toast.ore_prospector_message", length, totalCount)
                            .withStyle(ChatFormatting.GOLD));
            oreCounts.forEach((name, oreCount) -> player.sendSystemMessage(Component.literal("- ")
                    .append(name)
                    .append(Component.literal(": " + oreCount))
                    .withStyle(ChatFormatting.AQUA)));
        }

        if (player instanceof ServerPlayer sp) {
            player.swing(event.getHand(), true);
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND,
                    net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 0.1f);

            held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(event.getHand()));
            player.getCooldowns().addCooldown(held.getItem(), 40);

            if (!orePositions.isEmpty()) {
                if (centersOnly) {
                    List<BlockPos> centers = computeVeinCentersByRadius(orePositions, VEIN_SEPARATION_RADIUS);
                    if (!centers.isEmpty()) {
                        TFGNetworkHandler.INSTANCE.send(
                                PacketDistributor.PLAYER.with(() -> sp),
                                new OreHighlightVeinPacket(centers));
                    }
                } else {
                    TFGNetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> sp),
                            new OreHighlightPacket(orePositions));
                }
            }
        }

        event.setCanceled(true);
    }

    /*
     * Vein grouping by minimum separation radius: Any ores within 'radius' of an existing cluster join that cluster.
     * Clusters are connected components under "distance <= radius".
     */
    private static List<BlockPos> computeVeinCentersByRadius(List<BlockPos> orePositions, int radius) {
        final int r2 = radius * radius;

        List<BlockPos> centers = new ArrayList<>();
        Set<BlockPos> unassigned = new HashSet<>(orePositions);

        while (!unassigned.isEmpty()) {
            // start a new cluster
            BlockPos seed = unassigned.iterator().next();
            unassigned.remove(seed);

            List<BlockPos> cluster = new ArrayList<>();
            ArrayDeque<BlockPos> q = new ArrayDeque<>();
            q.add(seed);

            while (!q.isEmpty()) {
                BlockPos cur = q.poll();
                cluster.add(cur);

                List<BlockPos> toAttach = new ArrayList<>();
                for (BlockPos candidate : unassigned) {
                    if (dist2(cur, candidate) <= r2) {
                        toAttach.add(candidate);
                    }
                }
                for (BlockPos cand : toAttach) {
                    unassigned.remove(cand);
                    q.add(cand);
                }
            }

            centers.add(averageBlockPos(cluster));
        }

        return centers;
    }

    private static int dist2(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static BlockPos averageBlockPos(List<BlockPos> list) {
        long sx = 0, sy = 0, sz = 0;
        for (BlockPos p : list) {
            sx += p.getX();
            sy += p.getY();
            sz += p.getZ();
        }
        int n = list.size();
        return new BlockPos(Math.round(sx / (float) n), Math.round(sy / (float) n), Math.round(sz / (float) n));
    }
}
