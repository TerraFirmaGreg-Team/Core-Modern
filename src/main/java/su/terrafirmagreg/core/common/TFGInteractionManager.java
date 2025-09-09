package su.terrafirmagreg.core.common;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class TFGInteractionManager {
    public static void init(FMLCommonSetupEvent event) {
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
