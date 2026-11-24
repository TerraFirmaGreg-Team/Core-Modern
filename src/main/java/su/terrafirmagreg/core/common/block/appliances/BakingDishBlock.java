package su.terrafirmagreg.core.common.block.appliances;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.BottomSupportedDeviceBlock;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import su.terrafirmagreg.core.common.blockentity.appliances.BakingDishBlockEntity;

@SuppressWarnings("deprecation")
public class BakingDishBlock extends SealableDeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape OPENED_SHAPE = Shapes.or(
            box(3D, 0D, 1D, 13D, 5D, 15D));

    public static final VoxelShape CLOSED_SHAPE = Shapes.or(
            OPENED_SHAPE,
            box(4D, 5D, 2D, 12D, 6D, 14D),
            box(6.5D, 6D, 6.5D, 9.5D, 7D, 9.5D));

    private static final Map<Direction, VoxelShape> OPENED_SHAPES = createCardinalShapes(OPENED_SHAPE);
    private static final Map<Direction, VoxelShape> CLOSED_SHAPES = createCardinalShapes(CLOSED_SHAPE);

    private static Map<Direction, VoxelShape> createCardinalShapes(VoxelShape northShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, northShape);
        shapes.put(Direction.EAST, rotateClockwise(northShape));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[] { Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            buffer[0] = Shapes.or(buffer[0], Shapes.box(1D - maxZ, minY, minX, 1D - minZ, maxY, maxX));
        });
        return buffer[0];
    }

    public static <T extends BakingDishBlockEntity> void toggleSeal(Level level, BlockPos pos, BlockState state, BlockEntityType<T> type) {
        level.getBlockEntity(pos, type).ifPresent(vessel -> {
            final boolean previousSealed = state.getValue(SEALED);
            level.setBlockAndUpdate(pos, state.setValue(SEALED, !previousSealed));

            if (previousSealed) {
                vessel.onUnseal();
            } else {
                vessel.onSeal();
            }
        });
    }

    public BakingDishBlock(ExtendedProperties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(SEALED, false).setValue(POWERED, false));
    }

    // Break instantly if sealed.
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (state.hasProperty(SEALED) && state.getValue(SEALED)) {
            return 1.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    // "Pick up" block if broken while sealed.
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.hasProperty(SEALED) && state.getValue(SEALED)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, blockEntity, player, player.getMainHandItem());

            for (ItemStack drop : drops) {
                if (!player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
            }

            level.removeBlock(pos, false);
            super.playerWillDestroy(level, pos, state, player);
            return;
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean sealed = state.getValue(SEALED);
        return (sealed ? CLOSED_SHAPES : OPENED_SHAPES).get(facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return canSurvive(state, level, currentPos) ? super.updateShape(state, facing, facingState, level, currentPos, facingPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BottomSupportedDeviceBlock.canSurvive(level, pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * Reports all item stacks using a comparator.
     */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            return Mth.clamp(blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(ItemHandlerHelper::calcRedstoneFromInventory)
                    .orElse(0) * 2, 0, 15);
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        final CompoundTag tag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if (tag != null) {
            final CompoundTag inventoryTag = tag.getCompound("inventory");
            final ItemStackHandler inventory = new ItemStackHandler();

            inventory.deserializeNBT(inventoryTag);

            if (!Helpers.isEmpty(inventory) && !TFCConfig.CLIENT.displayItemContentsAsImages.get()) {
                tooltip.add(Component.translatable("tfc.tooltip.contents").withStyle(ChatFormatting.DARK_GREEN));
                Helpers.addInventoryTooltipInfo(inventory, tooltip);
            }
            addExtraInfo(tooltip, inventoryTag);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (TFCConfig.CLIENT.displayItemContentsAsImages.get()) {
            final CompoundTag tag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
            if (tag != null) {
                final CompoundTag inventoryTag = tag.getCompound("inventory");
                final ItemStackHandler inventory = new ItemStackHandler();

                inventory.deserializeNBT(inventoryTag);

                if (!Helpers.isEmpty(inventory)) {
                    return Helpers.getTooltipImage(inventory, 3, 2, 0, LargeVesselBlockEntity.SLOTS - 1);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            toggleSeal(level, pos, state, getExtendedProperties().blockEntity());
        } else {
            if (!state.getValue(SEALED) && !heldItem.isEmpty() && heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                if (level.getBlockEntity(pos) instanceof BakingDishBlockEntity bakingDish) {
                    if (BakingDishBlockEntity.depositFluid(heldItem, bakingDish, player, hand)) {
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }
            level.getBlockEntity(pos, getExtendedProperties().<BakingDishBlockEntity>blockEntity()).ifPresent(dish -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    Helpers.openScreen(serverPlayer, dish, pos);
                }
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof BakingDishBlockEntity bakingDish) {
            handleNeighborChanged(state, level, pos, bakingDish::onSeal, bakingDish::onUnseal);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
