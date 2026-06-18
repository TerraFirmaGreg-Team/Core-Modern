package su.terrafirmagreg.core.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.NotNull;

import com.therighthon.rnr.common.recipe.BlockModRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import su.terrafirmagreg.core.utils.TFGModsResolver;

/**
 * The TrowelItem allows players to place random blocks from their hotbar or, if the RnR mod is loaded,
 * to use RnR road recipes on base course blocks using items from the hotbar.
 */
@SuppressWarnings("deprecation")
public class TrowelItem extends Item {

    public TrowelItem(Properties props) {
        super(props);
    }

    /**
     * Called when the player uses the trowel on a block. If the RnR mod is loaded and the clicked block is
     * base course, it attempts to apply a matching RnR BlockModRecipe using a hotbar item. Otherwise, places
     * a random block from the player's hotbar.
     *
     * @param context The use context, including player, world, and target block information.
     * @return The result of the interaction.
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (!(level instanceof ServerLevel))
            return InteractionResult.PASS;

        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();

        BlockPos targetPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(targetPos);
        ResourceLocation clickedBlockId = clickedState.getBlock().builtInRegistryHolder().key().location();

        // RNR road exception. Use BlockModRecipe to find a matching recipe.
        if (TFGModsResolver.RNR.isLoaded() && clickedBlockId.toString().equals("rnr:base_course")) {
            List<HotbarEntry> validEntries = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack hotbarStack = player.getInventory().getItem(i);
                if (!hotbarStack.isEmpty()) {
                    ItemStack oneStack = hotbarStack.copy();
                    oneStack.setCount(1);
                    BlockModRecipe recipe = BlockModRecipe.getRecipe(clickedState, oneStack);
                    if (recipe != null && recipe.getOutputBlock() != null && recipe.getOutputBlock() != clickedState) {
                        validEntries.add(new HotbarEntry(i, hotbarStack, recipe));
                    }
                }
            }

            if (validEntries.isEmpty())
                return InteractionResult.PASS;

            HotbarEntry chosen = validEntries.get(new Random().nextInt(validEntries.size()));
            BlockState newState = chosen.recipe.getOutputBlock();

            level.setBlock(targetPos, newState, Block.UPDATE_ALL);
            level.updateNeighborsAt(targetPos, newState.getBlock());

            level.playSound(null, targetPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 0.4f);

            if (!player.isCreative() && Boolean.TRUE.equals(chosen.recipe.consumesItem())) {
                chosen.stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        // Normal block behavior.
        List<ItemStack> blockItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack hotbarStack = player.getInventory().getItem(i);
            if (hotbarStack.getItem() instanceof BlockItem) {
                blockItems.add(hotbarStack);
            }
        }

        if (blockItems.isEmpty())
            return InteractionResult.PASS;

        // Caches trowel tool item.
        int originalCount = stack.getCount();

        ItemStack randomStack = blockItems.get(new Random().nextInt(blockItems.size()));
        BlockItem blockItem = (BlockItem) randomStack.getItem();
        // Gets context like waterlogged, rotated, etc.
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        // Places block--respecting placement logic.
        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        BlockState newState = blockItem.getBlock().getStateForPlacement(placeContext);

        if (newState == null ||
                !newState.canSurvive(level, placePos) ||
                !level.isUnobstructed(newState, placePos, CollisionContext.of(player))) {
            return InteractionResult.FAIL;
        }

        InteractionResult result = blockItem.place(placeContext);
        // Restores trowel tool
        stack.setCount(originalCount);

        level.playSound(null, placePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 0.4f);

        // Decreases item count unless in creative.
        if (!player.isCreative()) {
            randomStack.shrink(1);
        }

        return result;
    }

    /**
     * Represents a hotbar entry containing the slot index, item stack, and associated BlockModRecipe.
     *
     * @param slot   The hotbar slot index.
     * @param stack  The item stack in the slot.
     * @param recipe The matching BlockModRecipe for this stack and block state.
     */
    private record HotbarEntry(int slot, ItemStack stack, BlockModRecipe recipe) {
    }
}
