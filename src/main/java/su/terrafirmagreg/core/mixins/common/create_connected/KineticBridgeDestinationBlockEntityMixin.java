package su.terrafirmagreg.core.mixins.common.create_connected;

import org.spongepowered.asm.mixin.Mixin;

import com.hlysine.create_connected.content.kineticbridge.KineticBridgeDestinationBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = KineticBridgeDestinationBlockEntity.class, remap = false)
public abstract class KineticBridgeDestinationBlockEntityMixin extends GeneratingKineticBlockEntity implements ITieredKineticBlockEntity {

    protected KineticBridgeDestinationBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getMaxCapacityFromBlock(Block block) {
        return getGeneratedSpeed() * calculateAddedStressCapacity();
    }
}
