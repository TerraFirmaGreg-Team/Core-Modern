package su.terrafirmagreg.core.mixins.common.gtceu;

import static com.gregtechceu.gtceu.api.GTValues.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(GTMachineUtils.class)
public class GTMachineUtilsMixin {

    @ModifyExpressionValue(method = "registerLaserHatch(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;ILcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;)[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At(value = "FIELD", target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;HIGH_TIERS:[I"), remap = false)
    private static int[] modifyLaserHatchTiers(int[] original) {
        return GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? OpV : UHV);
    }
}
