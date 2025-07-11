package su.terrafirmagreg.core.common.data.blocks;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ParticleEmitterBlock extends Block {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final VoxelShape DEFAULT_SHAPE = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F);
	private final VoxelShape shape;
	private final Supplier<SimpleParticleType> particleType;
	private final double offsetX, offsetY, offsetZ;
	private final double velocityX, velocityY, velocityZ;
	private final int particleCount;
	private final boolean particleForced;
	private final boolean useDustOptions;
	private final float red, green, blue, scale;

	public ParticleEmitterBlock(
			Properties properties,
			VoxelShape shape,
			Supplier<Item> itemSupplier,
			Supplier<SimpleParticleType> particleType,
			double offsetX, double offsetY, double offsetZ,
			double velocityX, double velocityY, double velocityZ,
			int particleCount,
			boolean particleForced,
			boolean useDustOptions,
			float red, float green, float blue, float scale
	) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
		this.shape = shape;
		this.particleType = particleType != null ? particleType : () -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;
		this.particleCount = Math.max(1, particleCount);
		this.particleForced = particleForced;

		this.useDustOptions = useDustOptions;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.scale = scale;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape != null ? shape : super.getShape(state, level, pos, context);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Info("create an always visible particle if render type is 'forced' and enable color settings if the particle type is 'minecraft:dust'")
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + 0.5 + random.nextDouble() * offsetX * (random.nextBoolean() ? 1 : -1);
			double y = pos.getY() + random.nextDouble() * offsetY;
			double z = pos.getZ() + 0.5 + random.nextDouble() * offsetZ * (random.nextBoolean() ? 1 : -1);

			if (useDustOptions) {
				var dust = new net.minecraft.core.particles.DustParticleOptions(
						new Vector3f(red, green, blue), scale
				);
				if (particleForced) {
					level.addAlwaysVisibleParticle(dust, true, x, y, z, velocityX, velocityY, velocityZ);
				} else {
					level.addParticle(dust, x, y, z, velocityX, velocityY, velocityZ);
				}
			} else {
				SimpleParticleType type = particleType.get();
				if (particleForced) {
					level.addAlwaysVisibleParticle(type, true, x, y, z, velocityX, velocityY, velocityZ);
				} else {
					level.addParticle(type, x, y, z, velocityX, velocityY, velocityZ);
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
