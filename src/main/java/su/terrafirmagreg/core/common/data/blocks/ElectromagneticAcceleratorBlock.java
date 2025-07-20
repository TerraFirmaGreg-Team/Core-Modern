package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

public class ElectromagneticAcceleratorBlock extends Block {

	public static final VoxelShape SHAPE = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F);

	public ElectromagneticAcceleratorBlock(Properties pProperties){
			super(pProperties);
	}


		@Override
		public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
		{
			return SHAPE;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
			for (int i = 0; i < 4; i++) {
				if (level.isClientSide) {
					ParticleType<?> pt = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("ae2", "lightning_fx"));
					if (pt instanceof SimpleParticleType) {
						level.addAlwaysVisibleParticle((SimpleParticleType) pt, true,
								pos.getX() + 0.5 + (random.nextFloat() - 0.5) * 0.5,
								pos.getY() + random.nextDouble(),
								pos.getZ() + 0.5 + (random.nextFloat() - 0.5) * 0.5,
								0.1, 0.1, 0.1);
					} else {
						// Fallback with vanilla particle
						level.addAlwaysVisibleParticle(ParticleTypes.END_ROD, true,
								pos.getX() + 0.5,
								pos.getY() + 0.5,
								pos.getZ() + 0.5,
								0.1, 0.1, 0.1);
					}
				}
			}
		}

		@Override
		@SuppressWarnings("deprecation")
		public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
			super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

		}
	}
