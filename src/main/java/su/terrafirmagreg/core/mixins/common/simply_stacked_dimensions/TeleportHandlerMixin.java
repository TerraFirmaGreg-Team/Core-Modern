package su.terrafirmagreg.core.mixins.common.simply_stacked_dimensions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simplystacked.SimplyStackedDimensions;
import com.simplystacked.Teleporting.TeleportHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TeleportHandler.class)
public abstract class TeleportHandlerMixin {

	@WrapOperation(method = "onLivingTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private static boolean tfg$onLivingTick(ServerLevel instance, BlockPos pos, BlockState blockState, Operation<Boolean> original) {

		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState cloud = SimplyStackedDimensions.CLOUD.get().defaultBlockState();
		BlockState lava = Blocks.LAVA.defaultBlockState();

		// Expand the hole outwards into a 3x3
		instance.setBlockAndUpdate(pos.north(), air);
		instance.setBlockAndUpdate(pos.north().east(), air);

		instance.setBlockAndUpdate(pos.east(), air);
		instance.setBlockAndUpdate(pos.east().south(), air);

		instance.setBlockAndUpdate(pos.south(), air);
		instance.setBlockAndUpdate(pos.south().west(), air);

		instance.setBlockAndUpdate(pos.west(), air);
		instance.setBlockAndUpdate(pos.west().north(), air);

		// Then put a layer of cloud above, to block lava
		tfg$placeCloudIfLava(instance, pos.above(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.north().above(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.north().east().above(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.east().above(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.east().south().above(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.south().above(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.south().west().above(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.west().above(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.west().north().above(), lava, cloud);

		// And then a layer of cloud underneath in case of lava because I can't mixin to generateCloudPlatform for some reason
		tfg$placeCloudIfLava(instance, pos.below().below(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.north().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.north().below().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.north().east().below().below(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.east().above().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.east().above().below().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.east().south().above().below().below(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.south().above().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.south().above().below().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.south().west().above().below().below(), lava, cloud);

		tfg$placeCloudIfLava(instance, pos.west().above().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.west().above().below().below(), lava, cloud);
		tfg$placeCloudIfLava(instance, pos.west().north().above().below().below(), lava, cloud);

		return original.call(instance, pos, blockState);
	}

	@Unique
	private static void tfg$placeCloudIfLava(ServerLevel instance, BlockPos pos, BlockState lava, BlockState cloud)
	{
		if (instance.getBlockState(pos) == lava)
		{
			instance.setBlockAndUpdate(pos, cloud);
		}
	}
}
