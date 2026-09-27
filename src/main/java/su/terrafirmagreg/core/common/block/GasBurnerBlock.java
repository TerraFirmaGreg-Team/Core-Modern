package su.terrafirmagreg.core.common.block;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class GasBurnerBlock extends DeviceBlock {
    public static final IntegerProperty HEAT = TFCBlockStateProperties.HEAT_LEVEL;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SET_LEVEL = IntegerProperty.create("set_level", 0, 10);

    protected static final VoxelShape SHAPE = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D), Block.box(1.0D, 9.0D, 1.0D, 15.0D, 16.0D, 15.0D));

    public GasBurnerBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any()
                .setValue(HEAT, 0)
                .setValue(LIT, false)
                .setValue(SET_LEVEL, 0)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(HEAT) == 0)
            return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.875D;
        double z = pos.getZ() + 0.5D;

        float[] offsets = Helpers.square(rand, 0.5f);
        double spawnX = x + offsets[0];
        double spawnY = pos.getY() + 1.0D;
        double spawnZ = z + offsets[1];

        if (rand.nextInt(10) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.6F, false);
        }
        for (int i = 0; i < rand.nextInt(2); i++) {
            level.addParticle(ParticleTypes.SMOKE, spawnX, y + (rand.nextDouble() / 2), spawnZ, 0, 0.005D, 0);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, spawnX, spawnY + (rand.nextFloat() / 10), spawnZ, 0, 0, 0);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity) && level.getBlockState(pos).getValue(HEAT) > 0) {
            entity.hurt(entity.damageSources().hotFloor(), 1f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HEAT, LIT, FACING, SET_LEVEL));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            return state.setValue(FACING, facing);
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        var heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmItem) {
            return InteractionResult.FAIL;
        }

        GasBurnerBlockEntity burner = level.getBlockEntity(pos, TFGBlockEntities.GAS_BURNER.get()).orElse(null);
        if (burner != null) {
            if (FluidHelpers.transferBetweenBlockEntityAndItem(heldItem, burner, player, hand)) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                Helpers.openScreen(serverPlayer, burner, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (state.getValue(HEAT) > 0) {
            Helpers.fireSpreaderTick(level, pos.above(), rand, 3);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
