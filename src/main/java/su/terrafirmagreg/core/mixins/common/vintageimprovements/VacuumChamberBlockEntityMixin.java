package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = VacuumChamberBlockEntity.class, remap = false)
public abstract class VacuumChamberBlockEntityMixin extends KineticBlockEntity implements ITieredKineticBlockEntity {

    public VacuumChamberBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    // TODO: Delete once https://github.com/SSWTLZZ69/Create-Vintage-Improvements/pull/22 gets merged
    @ModifyVariable(method = "tick", at = @At("STORE"), name = "recipeSpeed", ordinal = 1)
    private float tfg$adjustRecipeSpeed(float recipeSpeed) {
        return recipeSpeed * 3000f / (recipeSpeed * 100f + 3000f);
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"))
    private void tfg$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        addToGoggleTooltip(tooltip, GTMaterials.Steel, getSpeed());
    }
}
