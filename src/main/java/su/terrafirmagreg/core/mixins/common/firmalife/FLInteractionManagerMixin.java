package su.terrafirmagreg.core.mixins.common.firmalife;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.misc.FLInteractionManager;
import com.eerussianguy.firmalife.common.util.FLAdvancements;

import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.BlockItemPlacement;
import net.dries007.tfc.util.InteractionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mixin for FLInteractionManager to remove the hardcoded check for bottom ovens
 * to place grills and pots, and just check for a solid face so that they can be used
 * by the gas burner.
 */
@Mixin(value = FLInteractionManager.class, remap = false)
public abstract class FLInteractionManagerMixin {

    /**
     * @author Redeix
     * @reason Check for sturdy faces instead of OvenBottomBlock for stovetop devices.
     */
    @Overwrite
    public static void init() {
        InteractionManager.register(new BlockItemPlacement(TFCItems.WOOL_YARN, FLBlocks.WOOL_STRING) {
            @Override
            public @NotNull InteractionResult postPlacement(@NotNull BlockPlaceContext context) {
                final Level level = context.getLevel();
                final BlockPos pos = context.getClickedPos();
                final BlockState state = level.getBlockState(pos);
                state.getBlock().setPlacedBy(level, pos, state, context.getPlayer(), context.getItemInHand());
                return super.postPlacement(context);
            }
        });

        InteractionManager.register(Ingredient.of(TFCItems.WROUGHT_IRON_GRILL.get()), false, (stack, context) -> {
            final Level level = context.getLevel();
            final BlockPos pos = context.getClickedPos();
            final BlockPos abovePos = pos.above();

            if (context.getClickedFace() == Direction.UP && level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP) && level.getBlockState(abovePos).isAir()) {
                level.setBlockAndUpdate(abovePos, FLBlocks.STOVETOP_GRILL.get().defaultBlockState());
                if (context.getPlayer() == null || !context.getPlayer().isCreative())
                    stack.shrink(1);
                if (context.getPlayer() instanceof ServerPlayer server) {
                    FLAdvancements.STOVETOP_GRILL.trigger(server);
                }
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        InteractionManager.register(Ingredient.of(TFCItems.POT.get()), false, (stack, context) -> {
            final Level level = context.getLevel();
            final BlockPos pos = context.getClickedPos();
            final BlockPos abovePos = pos.above();

            if (context.getClickedFace() == Direction.UP && level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP) && level.getBlockState(abovePos).isAir()) {
                level.setBlockAndUpdate(abovePos, FLBlocks.STOVETOP_POT.get().defaultBlockState());
                if (context.getPlayer() == null || !context.getPlayer().isCreative())
                    stack.shrink(1);
                if (context.getPlayer() instanceof ServerPlayer server) {
                    FLAdvancements.STOVETOP_POT.trigger(server);
                }
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}
