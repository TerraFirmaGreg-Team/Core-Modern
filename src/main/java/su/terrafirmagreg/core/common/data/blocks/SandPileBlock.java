package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.PileBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.utils.PlanetEnvironmentalHelpers;

import java.util.Optional;

public class SandPileBlock extends SandLayerBlock implements IForgeBlockExtension, EntityBlockExtension {

    public static boolean canPlaceSandPile(LevelAccessor level, BlockPos pos, BlockState state) {
        return Helpers.isBlock(state.getBlock(), TFCTags.Blocks.CAN_BE_SNOW_PILED) && TFGBlocks.MARS_SAND_PILE_BLOCK.get().defaultBlockState().canSurvive(level, pos);
    }

    // TODO
    public static void placeSandPile(LevelAccessor level, BlockPos pos, BlockState state, boolean byPlayer) {
        // Create a snow pile block, accounting for double piles.
        final BlockPos posAbove = pos.above();
        final BlockState aboveState = level.getBlockState(posAbove);
        final BlockState savedAboveState = Helpers.isBlock(aboveState.getBlock(), TFCTags.Blocks.CAN_BE_SNOW_PILED) ? aboveState : null;
        final BlockState sandPile = PlanetEnvironmentalHelpers.getSandBlockForBiome(level, pos, true).defaultBlockState();

        level.setBlock(pos, sandPile, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.getBlockEntity(pos, TFCBlockEntities.PILE.get()).ifPresent(entity -> entity.setHiddenStates(state, savedAboveState, byPlayer));

        if (savedAboveState != null) {
            Helpers.removeBlock(level, posAbove, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }

        // Then cause block updates
        level.blockUpdated(pos, TFCBlocks.SNOW_PILE.get());
        if (savedAboveState != null) {
            level.blockUpdated(posAbove, Blocks.AIR);
        }

        // TODO: update grass with the sandy property
        //        final BlockPos posBelow = pos.below();
        //        level.setBlock(posBelow, Helpers.setProperty(level.getBlockState(posBelow), SnowyDirtBlock.SNOWY, true), 2);
    }

    public static void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state) {
        removePileOrSand(level, pos, state, -1);
    }

    public static void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state, int expectedLayers) {
        //noinspection OptionalAssignedToNull
        removePileOrSand(level, pos, state, expectedLayers, null);
    }

    /**
     * @param expectedLayers The expected number of snow layers. -1 = no expectation, just remove a single layer. 0 = remove all snow layers.
     * @param sandPile If {@code null}, then there is no provided block entity and one should be queried from the world. If not-null, this
     *                 represents the block entity present in the world, possibly empty.
     */
    public static void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state, int expectedLayers, @Nullable Optional<PileBlockEntity> sandPile) {
        final int layers = state.getValue(SandLayerBlock.LAYERS);
        if (expectedLayers >= layers) {
            // If we expect more layers than actually exist, don't remove anything
            return;
        }
        if (layers > 1 && expectedLayers != 0) {
            // Remove layers, but keep the snow block intact
            level.setBlock(pos, state.setValue(SandLayerBlock.LAYERS, expectedLayers == -1 ? layers - 1 : expectedLayers), Block.UPDATE_ALL);
        }
        // sand layer blocks are unable to turn into full sand blocks so we don't need or want this behavior
        //        else if (state.getBlock() == Blocks.SNOW)
        //        {
        //            // Remove a single snow layer block
        //            level.removeBlock(pos, false);
        //        }
        else {
            // Otherwise, remove a snow pile, restoring the internal states
            if (sandPile == null)
                sandPile = level.getBlockEntity(pos, TFCBlockEntities.PILE.get());
            sandPile.ifPresent(pile -> {
                final BlockPos above = pos.above();

                level.setBlock(pos, pile.getInternalState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                if (pile.getAboveState() != null && level.isEmptyBlock(above)) {
                    level.setBlock(above, pile.getAboveState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }

                // Update neighbors shapes from the bottom block (this is important to get grass blocks to adjust to snowy/non-snowy states)
                pile.getInternalState().updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
                level.getBlockState(above).updateNeighbourShapes(level, above, Block.UPDATE_CLIENTS);

                // Block ticks after both blocks are placed
                level.blockUpdated(pos, pile.getInternalState().getBlock());
                if (pile.getAboveState() != null) {
                    level.blockUpdated(above, pile.getAboveState().getBlock());
                }
            });
        }
    }

    private final ExtendedProperties properties;
    private Block originBlock;

    public SandPileBlock(ExtendedProperties properties, Block originBlock) {
        this(properties);
        this.originBlock = originBlock;
    }

    public SandPileBlock(ExtendedProperties properties) {
        super(properties.properties());

        this.properties = properties;
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return properties;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity entity, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, entity, stack);
        removePileOrSand(level, pos, state, -1, entity instanceof PileBlockEntity pile ? Optional.of(pile) : Optional.empty());
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        playerWillDestroy(level, pos, state, player);
        removePileOrSand(level, pos, state);
        return true; // Cause drops and other stuff to occur
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        // Handle clicking on a snow pile with snow layers - increment the layer count
        // We have to handle the getStateForPlacement in a mixin to snow layer block
        // TODO: remove before modpack release
        // want to replace with logic like, "if item in hand has the same root type as the clicked block, then increment"
        if (context.getItemInHand().getItem() == TFGBlocks.MARS_SAND_LAYER_BLOCK.get().asItem() && state.getValue(LAYERS) < 8) {
            if (context.replacingClickedOnBlock()) {
                return context.getClickedFace() == Direction.UP;
            } else {
                return true;
            }
        }
        return false; // Don't allow replacement of single layers with anything (unlike vanilla snow)
    }

    @Nullable
    @Override
    // do i even need this?
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    //    @Override
    //    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
    //    {
    //        // TODO: remove before modpack release
    //        return new ItemStack(Blocks.SNOW);
    //    }

}
