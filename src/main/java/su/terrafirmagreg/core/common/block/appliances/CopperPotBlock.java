package su.terrafirmagreg.core.common.block.appliances;

import com.negodya1.vintageimprovements.VintageBlocks;

import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.client.renderer.CookingApplianceBlockEntityRenderer;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.data.TFGSounds;

public class CopperPotBlock extends CookingApplianceBlock {

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D),
            Block.box(2.0D, 1.0D, 2.0D, 14.0D, 16.0D, 3.0D),
            Block.box(2.0D, 1.0D, 13.0D, 14.0D, 16.0D, 14.0D),
            Block.box(2.0D, 1.0D, 3.0D, 3.0D, 16.0D, 13.0D),
            Block.box(13.0D, 1.0D, 3.0D, 14.0D, 16.0D, 13.0D));

    public CopperPotBlock(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (level.getBlockEntity(pos) instanceof CookingApplianceBlockEntity be) {

            // Cooking Effects.
            float fluidLevel = CookingApplianceBlockEntityRenderer.getHighestFluidLevel(be);
            int cookTime = be.getCookingTicks();
            int maxCookTime = be.getMaxDuration();
            float temperature = be.getTemperature();
            BlockState aboveState = level.getBlockState(pos.above());
            boolean hasVacuumChamber = level.getBlockState(pos.above(2)).is(VintageBlocks.VACUUM_CHAMBER.get());
            float volume = rand.nextFloat() * 0.8f;

            // Lower volume if there is something above the pot.
            if (!aboveState.isAir())
                volume = volume - 0.2F;

            double x = pos.getX() + 0.3F + (rand.nextFloat() * 0.3F);
            double y = pos.getY() + fluidLevel + (rand.nextFloat() * 0.2F);
            double z = pos.getZ() + 0.3F + (rand.nextFloat() * 0.3F);

            if (cookTime > 10 && (maxCookTime - cookTime) > 30) {
                if (rand.nextInt(10) == 0) {
                    level.playLocalSound(x, y, z, TFGSounds.COOKING.getMainEvent(), SoundSource.BLOCKS, volume, rand.nextFloat() * 0.7F + 0.1F, false);
                }

                // Only render particles if there is nothing above the pot and no attachments.
                if (aboveState.isAir() && !hasVacuumChamber) {
                    // Only render steam particles if the pot is hot.
                    if (temperature > 100) {
                        if (rand.nextInt(5) == 0) {
                            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.5F + (rand.nextFloat() * 0.5F), z, 0, 0.04D, 0);
                        }
                        if (rand.nextInt(5) == 0) {
                            level.addParticle(TFCParticles.STEAM.get(), x, y + (rand.nextFloat() * 0.5F), z, 0, 0.04D, 0);
                        }
                    }

                    int bubbleChance = rand.nextInt(4);
                    for (int i = 0; i < bubbleChance; i++) {
                        level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0, 0);
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
