package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.negodya1.vintageimprovements.content.kinetics.vibration.VibratingTableBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = VibratingTableBlockEntity.class, remap = false)
public abstract class VibratingTableBlockEntityMixin extends KineticBlockEntity implements ITieredKineticBlockEntity {
    public VibratingTableBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return ITieredKineticBlockEntity.super.addToGoggleTooltip(tooltip, GTMaterials.Steel, getSpeed());
    }
}
