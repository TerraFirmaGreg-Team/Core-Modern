package su.terrafirmagreg.core.common.data.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.terrafirmagreg.core.common.data.TFGTags;

import java.util.*;

public class HarvesterEvent {

    private static final TagKey<Item> HARVESTER_ITEM_TAG = TFGTags.Items.Harvester;
    private static final TagKey<Block> HARVESTABLE_BLOCK_TAG = TFGTags.Blocks.HarvesterHarvestable;

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Check if the held item has the tfg:harvester tag.
        if (!held.is(HARVESTER_ITEM_TAG)) {
            return;
        }

        // Check if the clicked block has the tfg:harvester_harvestable tag.
        if (!clickedState.is(HARVESTABLE_BLOCK_TAG)) {
            return;
        }

        // Sets the number of iterations for the search function.
        int radius = 7;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(clickedPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) continue;

            BlockState state = level.getBlockState(current);
            if (!state.is(HARVESTABLE_BLOCK_TAG)) continue;

            // Simulate Use
            BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(current),
                    Direction.UP,
                    current,
                    false
            );

            InteractionResult result = state.use(level, player, hand, hit);

            // Iterates through connected and diagonal neighbors
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (neighbor.closerThan(clickedPos, radius)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer) {
            // Swing Animation
            player.swing(hand, true);

            // Play Sound
            level.playSound(
                    null,
                    clickedPos,
                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    2.0f,
                    0.2f
            );

            // Damage Item
            held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
        }

        // Prevents default logic
        event.setCanceled(true);
    }
}

