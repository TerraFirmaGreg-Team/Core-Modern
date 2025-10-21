package su.terrafirmagreg.core.common;

import net.dries007.tfc.util.InteractionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.SandPileBlock;

public class TFGInteractionManager {
    public static void init(FMLCommonSetupEvent event) {
        /*InteractionManager.register(
                Ingredient.of(TFGBlocks.MARS_SAND_LAYER_BLOCK.get().asItem()),
                false,
                (stack, context) -> {
                    Player player = context.getPlayer();
                    if (player != null && !player.getAbilities().mayBuild) {
                        return InteractionResult.PASS;
                    } else {
                        final BlockPlaceContext blockContext = new BlockPlaceContext(context);
                        final Level level = blockContext.getLevel();
                        final BlockPos pos = blockContext.getClickedPos();
                        final BlockState stateAt = level.getBlockState(blockContext.getClickedPos());
                        if (SandPileBlock.canPlaceSandPile(level, pos, stateAt)) {
                            SandPileBlock.placeSandPile(level, pos, stateAt, true);
                            final BlockState placedState = level.getBlockState(pos);
                            final SoundType placementSound = placedState.getSoundType(level, pos, player);
                            level.playSound(
                                    player,
                                    pos,
                                    placedState.getSoundType(level, pos, player).getPlaceSound(),
                                    SoundSource.BLOCKS,
                                    (placementSound.getVolume() + 1.0F) / 2.0F,
                                    placementSound.getPitch() * 0.8F);
                            if (player == null || !player.getAbilities().instabuild) {
                                stack.shrink(1);
                            }

                            InteractionResult result = InteractionResult.sidedSuccess(level.isClientSide);
                            if (player != null && result.consumesAction()) {
                                player.awardStat(Stats.ITEM_USED.get(TFGBlocks.MARS_SAND_LAYER_BLOCK.get().asItem()));
                            }
                            return result;
                        }

                        // Default behavior
                        // Handles layering behavior of both snow piles and snow layers via the
                        // blocks replacement / getStateForPlacement
                        if (TFGBlocks.MARS_SAND_LAYER_BLOCK.get().asItem() instanceof BlockItem blockItem) {
                            return blockItem.place(blockContext);
                        }
                        return InteractionResult.FAIL;
                    }
                });*/

        //		InteractionManager.register(Ingredient.of(TFCItems.POWDERS.get(Powder.WOOD_ASH).get()), false, (stack, context) -> {
        //			Player player = context.getPlayer();
        //			if (player != null && !player.getAbilities().mayBuild)
        //			{
        //				return InteractionResult.PASS;
        //			}
        //			else
        //			{
        //				final Level level = context.getLevel();
        //				final BlockPos pos = context.getClickedPos();
        //				final BlockState stateAt = level.getBlockState(pos);
        //				final Block pile = TFGBlocks.WOOD_ASH_PILE.get();
        //
        //				if (player != null &&
        //					(player.blockPosition().equals(pos)
        //						|| (player.blockPosition().equals(pos.above())
        //							&& Helpers.isBlock(stateAt, pile)
        //							&& stateAt.getValue(LayerBlock.LAYERS) == 8)))
        //				{
        //					return InteractionResult.FAIL;
        //				}
        //
        //				if (Helpers.isBlock(stateAt, pile))
        //				{
        //					int layers = stateAt.getValue(LayerBlock.LAYERS);
        //					if (layers != 8)
        //					{
        //						stack.shrink(1);
        //						level.setBlockAndUpdate(pos, stateAt.setValue(LayerBlock.LAYERS, layers + 1));
        //						Helpers.playSound(level, pos, SoundType.SAND.getPlaceSound());
        //						return InteractionResult.SUCCESS;
        //					}
        //				}
        //
        //				if (level.isEmptyBlock(pos.above()) && stateAt.isFaceSturdy(level, pos, Direction.UP))
        //				{
        //					stack.shrink(1);
        //					level.setBlockAndUpdate(pos.above(), pile.defaultBlockState());
        //					Helpers.playSound(level, pos, SoundType.SAND.getPlaceSound());
        //					return InteractionResult.SUCCESS;
        //				}
        //				return InteractionResult.FAIL;
        //			}
        //		});
    }
}
