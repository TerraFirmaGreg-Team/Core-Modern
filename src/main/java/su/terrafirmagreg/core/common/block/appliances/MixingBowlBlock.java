package su.terrafirmagreg.core.common.block.appliances;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;

import net.dries007.tfc.client.IHighlightHandler;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.capabilities.size.IItemSize;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.capabilities.size.Weight;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import su.terrafirmagreg.core.client.renderer.CookingApplianceBlockEntityRenderer;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.MixingBowlBlockEntity;
import su.terrafirmagreg.core.common.recipe.attachment.MixerAttachment;

public class MixingBowlBlock extends CookingApplianceBlock implements IItemSize, IHighlightHandler {

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0D, 1.0D, 3.0D, 13.0D, 2.0D, 13.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 3.0D),
            Block.box(2.0D, 0.0D, 13.0D, 14.0D, 8.0D, 14.0D),
            Block.box(2.0D, 0.0D, 3.0D, 3.0D, 8.0D, 13.0D),
            Block.box(13.0D, 0.0D, 3.0D, 14.0D, 8.0D, 13.0D));

    public static final VoxelShape[] TOOL_SHAPES = Helpers.computeHorizontalShapes(dir -> Helpers.rotateShape(dir, 3.0D, 2.0D, 3.0D, 7.0D, 12.0D, 7.0D));
    public static final VoxelShape[] FULL_SHAPES = new VoxelShape[] {
            Shapes.or(SHAPE, TOOL_SHAPES[0]),
            Shapes.or(SHAPE, TOOL_SHAPES[1]),
            Shapes.or(SHAPE, TOOL_SHAPES[2]),
            Shapes.or(SHAPE, TOOL_SHAPES[3])
    };
    public static final AABB[] TOOL_AABBS = new AABB[] {
            TOOL_SHAPES[0].bounds().inflate(0.01D),
            TOOL_SHAPES[1].bounds().inflate(0.01D),
            TOOL_SHAPES[2].bounds().inflate(0.01D),
            TOOL_SHAPES[3].bounds().inflate(0.01D)
    };

    public MixingBowlBlock(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmItem) {
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl) {
            final Vec3 hitLoc = hit.getLocation();
            Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
            int index = facing.get2DDataValue();
            if (!bowl.getToolStack().isEmpty() && TOOL_AABBS[index].move(pos).contains(hitLoc)) {
                MechanicalMixerBlockEntity mixer = MixerAttachment.getMixer(bowl);
                if (mixer != null && mixer.running) {
                    return InteractionResult.PASS;
                }
                if (bowl.isManualMixing()) {
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
                if (bowl.startManualMixing()) {
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }

            if (bowl.getTotalFluidTanks() > 0 && !heldItem.isEmpty() && heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                if (CookingApplianceBlockEntity.depositFluid(heldItem, bowl, player, hand, -1)) {
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }

            if (player instanceof ServerPlayer serverPlayer) {
                Helpers.openScreen(serverPlayer, bowl, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean drawHighlight(Level level, BlockPos pos, Player player, BlockHitResult rayTrace, PoseStack stack, MultiBufferSource buffers, Vec3 rendererPosition) {
        if (level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl) {
            if (!bowl.getToolStack().isEmpty() && bowl.getCookingTicks() <= 0 && !bowl.isManualMixing()) {
                MechanicalMixerBlockEntity mixer = MixerAttachment.getMixer(bowl);
                if (mixer != null && mixer.running) {
                    return false;
                }
                BlockState state = level.getBlockState(pos);
                Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
                int index = facing.get2DDataValue();
                final Vec3 hit = rayTrace.getLocation();
                if (TOOL_AABBS[index].move(pos).contains(hit)) {
                    IHighlightHandler.drawBox(stack, TOOL_SHAPES[index], buffers, pos, rendererPosition, 1.0F, 0.7F, 0.3F, 0.7F);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (level.getBlockEntity(pos) instanceof CookingApplianceBlockEntity be) {

            // Mixing Effects.
            float fluidLevel = CookingApplianceBlockEntityRenderer.getHighestFluidLevel(be);
            int mixTime = be.getCookingTicks();
            int maxCookTime = be.getMaxDuration();

            double x = pos.getX() + 0.3F + (rand.nextFloat() * 0.3F);
            double y = pos.getY() + fluidLevel + (rand.nextFloat() * 0.2F);
            double z = pos.getZ() + 0.3F + (rand.nextFloat() * 0.3F);

            if (mixTime > 0 && (maxCookTime - mixTime) > 0) {
                if (rand.nextFloat() > 0) {
                    level.playLocalSound(x, y, z, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.1F + (rand.nextFloat() * 0.2f), rand.nextFloat() + 0.3F, false);
                }
            }
        }
    }

    @Override
    public Size getSize(ItemStack stack) {
        return Size.HUGE;
    }

    @Override
    public Weight getWeight(ItemStack stack) {
        // Sets the weight to very heavy if it contains anything. Like large vessels and barrels.
        return stack.getTag() == null ? Weight.HEAVY : Weight.VERY_HEAVY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl && !bowl.getToolStack().isEmpty()) {
            Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
            return FULL_SHAPES[facing.get2DDataValue()];
        }
        return SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
