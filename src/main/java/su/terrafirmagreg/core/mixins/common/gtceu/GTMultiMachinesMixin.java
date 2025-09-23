package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

@Mixin(value = GTMultiMachines.class, remap = false)
public class GTMultiMachinesMixin {

    // WOODEN_MULTIBLOCK_TANK
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;registerMultiblockTank(Ljava/lang/String;Ljava/lang/String;ILjava/util/function/Supplier;Ljava/util/function/Supplier;Lcom/gregtechceu/gtceu/api/fluids/PropertyFluidFilter;Ljava/util/function/BiConsumer;)Lcom/gregtechceu/gtceu/api/machine/MultiblockMachineDefinition;", ordinal = 0), index = 2)
    private static int patchWoodenTankCapacity(int original) {
        return 1_000_000;
    }

    // BRONZE_MULTIBLOCK_TANK
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;registerMultiblockTank(Ljava/lang/String;Ljava/lang/String;ILjava/util/function/Supplier;Ljava/util/function/Supplier;Lcom/gregtechceu/gtceu/api/fluids/PropertyFluidFilter;Ljava/util/function/BiConsumer;)Lcom/gregtechceu/gtceu/api/machine/MultiblockMachineDefinition;", ordinal = 1), index = 2)
    private static int patchBronzeTankCapacity(int original) {
        return 4_000_000;
    }

    // STEEL_MULTIBLOCK_TANK
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;registerMultiblockTank(Ljava/lang/String;Ljava/lang/String;ILjava/util/function/Supplier;Ljava/util/function/Supplier;Lcom/gregtechceu/gtceu/api/fluids/PropertyFluidFilter;Ljava/util/function/BiConsumer;)Lcom/gregtechceu/gtceu/api/machine/MultiblockMachineDefinition;", ordinal = 2), index = 2)
    private static int patchSteelTankCapacity(int original) {
        return 8_000_000;
    }
}
