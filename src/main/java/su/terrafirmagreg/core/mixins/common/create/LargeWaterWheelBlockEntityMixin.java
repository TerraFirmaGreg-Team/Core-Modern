package su.terrafirmagreg.core.mixins.common.create;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = LargeWaterWheelBlockEntity.class, remap = false)
public class LargeWaterWheelBlockEntityMixin extends GeneratingKineticBlockEntity implements ITieredKineticBlockEntity {

    public LargeWaterWheelBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}
