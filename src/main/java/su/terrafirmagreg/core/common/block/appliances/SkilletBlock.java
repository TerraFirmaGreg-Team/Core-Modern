package su.terrafirmagreg.core.common.block.appliances;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;

public class SkilletBlock extends CookingApplianceBlock {

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D),
            Block.box(2.0D, 1.0D, 2.0D, 14.0D, 3.0D, 3.0D),
            Block.box(2.0D, 1.0D, 13.0D, 14.0D, 3.0D, 14.0D),
            Block.box(2.0D, 1.0D, 3.0D, 3.0D, 3.0D, 13.0D),
            Block.box(13.0D, 1.0D, 3.0D, 14.0D, 3.0D, 13.0D));

    public SkilletBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            return state.setValue(FACING, context.getHorizontalDirection()).setValue(RECEIVED_HEAT, 0);
        }
        return null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (level.getBlockEntity(pos) instanceof CookingApplianceBlockEntity be) {
            int cookTime = be.getCookingTicks();
            int maxCookTime = be.getMaxDuration();
            float temperature = be.getTemperature();
            BlockState aboveState = level.getBlockState(pos.above());

            double x = pos.getX() + 0.3F + (rand.nextFloat() * 0.4F);
            double y = pos.getY() + 0.25F;
            double z = pos.getZ() + 0.3F + (rand.nextFloat() * 0.4F);

            // Louder sound on recipe start.
            if (cookTime > 1 && cookTime < 10) {
                level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, rand.nextFloat() * 0.02F, false);
            }

            if (cookTime > 10 && (maxCookTime - cookTime) > 30) {
                if (rand.nextInt(4) == 0) {
                    level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.05F, 0.01F + rand.nextFloat() * 0.01F, false);
                }

                if (aboveState.isAir()) {
                    if (temperature > 100) {
                        if (rand.nextInt(5) == 0) {
                            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.2F + (rand.nextFloat() * 0.3F), z, 0, 0.04D, 0);
                        }
                    }

                    if (rand.nextInt(4) == 0) {
                        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01D, 0);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
