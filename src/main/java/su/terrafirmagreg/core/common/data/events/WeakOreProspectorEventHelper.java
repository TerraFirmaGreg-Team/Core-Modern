package su.terrafirmagreg.core.common.data.events;

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

import java.util.*;

public class WeakOreProspectorEventHelper {

    private final double length;
    private final double halfWidth;
    private final double halfHeight;
    private final TagKey<Item> itemTag;
    private final TagKey<Block> oreTag = net.minecraftforge.common.Tags.Blocks.ORES;

    public WeakOreProspectorEventHelper(double length, double halfWidth, double halfHeight, TagKey<Item> itemTag) {
        this.length = length;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.itemTag = itemTag;
    }

    public double getLength() {
        return length;
    }

    public double getHalfWidth() {
        return halfWidth;
    }

    public double getHalfHeight() {
        return halfHeight;
    }

    public TagKey<Item> getItemTag() {
        return itemTag;
    }

    public void handleRightClick(PlayerInteractEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide()) return;

        ItemStack held = player.getItemInHand(event.getHand());
        if (!held.is(itemTag)) return;

        if (player.getCooldowns().isOnCooldown(held.getItem())) return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle().normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = lookDir.cross(up).normalize();
        if (right.lengthSqr() == 0) right = new Vec3(1, 0, 0);
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
                    if (!checkedPositions.add(pos)) continue;

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
            player.sendSystemMessage(Component.translatable("tfg.toast.ore_prospector_none").withStyle(ChatFormatting.GRAY));
        } else {

            player.sendSystemMessage(
                    Component.translatable(
                            "tfg.toast.ore_prospector_message",
                            length,
                            "??"
                    ).withStyle(ChatFormatting.GOLD)
            );
            oreCounts.forEach((name, oreCount) ->
                    player.sendSystemMessage(Component.literal("- ")
                            .append(name)
                            .withStyle(ChatFormatting.AQUA))
            );
        }

        if (player instanceof ServerPlayer sp) {
            player.swing(event.getHand(), true);
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND,
                    net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 0.1f);

            held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(event.getHand()));
            player.getCooldowns().addCooldown(held.getItem(), 40);

        }

        // Cancel the event so no default right-click logic happens
        event.setCanceled(true);
    }
}
