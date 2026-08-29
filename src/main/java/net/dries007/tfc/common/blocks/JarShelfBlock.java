/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks;

import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.IHighlightHandler;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.JarsBlockEntity;
import net.dries007.tfc.common.blockentities.PlacedItemBlockEntity;
import net.dries007.tfc.common.blocks.devices.BottomSupportedDeviceBlock;
import net.dries007.tfc.util.Helpers;

public class JarShelfBlock extends JarsBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape TOP_SHAPE = box(0, 15, 0, 16, 16, 16);

    private static Map<BlockState, VoxelShape> makeShapesWithShelf(ImmutableList<BlockState> possibleStates)
    {
        final Map<BlockState, VoxelShape> shapes = makeShapes(possibleStates);
        final ImmutableMap.Builder<BlockState, VoxelShape> map = new ImmutableMap.Builder<>();
        for (Map.Entry<BlockState, VoxelShape> entry : shapes.entrySet())
        {
            map.put(entry.getKey(), Shapes.or(entry.getValue(), TOP_SHAPE));
        }
        return map.build();
    }

    public static boolean canHangOnWall(LevelReader level, BlockPos pos, BlockState state)
    {
        final Direction facing = state.getValue(FACING);
        final BlockPos facePos = pos.relative(facing);
        final BlockState faceState = level.getBlockState(facePos);
        return faceState.isFaceSturdy(level, facePos, facing.getOpposite());
    }

    private final Map<BlockState, VoxelShape> cachedShapes;

    public JarShelfBlock(ExtendedProperties properties)
    {
        super(properties, false);
        this.cachedShapes = makeShapesWithShelf(getStateDefinition().getPossibleStates());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return cachedShapes.get(state);
    }

    @Override
    protected boolean isPersistentWithNoItems()
    {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        return canHangOnWall(level, pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (BottomSupportedDeviceBlock.canSurvive(level, pos))
        {
            // prevent adding jars in the case where there's no block underneath
            return super.use(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        final BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return canHangOnWall(context.getLevel(), context.getClickedPos(), state) ? state : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        // if there's no supported block underneath, eject the jars
        if (level.getBlockEntity(currentPos) instanceof JarsBlockEntity jars && !canSurvive(level, currentPos))
        {
            jars.ejectInventory();
            jars.clearContent();
        }
        return facing == state.getValue(FACING) && !facingState.isFaceSturdy(level, facingPos, facing.getOpposite()) ? Blocks.AIR.defaultBlockState() : updateStateValues(level, currentPos, state);
    }

    @Override
    public boolean drawHighlight(Level level, BlockPos pos, Player player, BlockHitResult rayTrace, PoseStack stack, MultiBufferSource buffers, Vec3 rendererPosition)
    {
        final int slot = PlacedItemBlockEntity.getSlotSelected(rayTrace);
        final boolean lookingAtJar = BOUNDS[slot].move(pos).contains(rayTrace.getLocation());
        if (lookingAtJar)
        {
            return super.drawHighlight(level, pos, player, rayTrace, stack, buffers, rendererPosition);
        }

        final BlockPos above = pos.above();
        final BlockState aboveState = level.getBlockState(above);
        final boolean holdingJar = Helpers.isItem(player.getItemInHand(InteractionHand.MAIN_HAND), TFCTags.Items.JARS) || Helpers.isItem(player.getItemInHand(InteractionHand.OFF_HAND), TFCTags.Items.JARS);
        final boolean filledSlotAbove = aboveState.getBlock() instanceof JarsBlock && aboveState.getValue(ITEM_PROPERTIES[slot]);

        IHighlightHandler.drawBox(stack, TOP_SHAPE, buffers, pos, rendererPosition, 0f, 0f, 0f, 0.4f);
        if (holdingJar && !filledSlotAbove)
        {
            IHighlightHandler.drawBox(stack, SHAPES[slot], buffers, above, rendererPosition, 1f, 0f, 0f, 1f);
        }

        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
