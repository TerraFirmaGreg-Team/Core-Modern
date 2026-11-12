package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine;

@Mixin(value = CleanroomMachine.class, remap = false)
public class GTCleanroomMixin {
    /**
     * Change max number of door blocks to 9 to allow usage of ad astra sliding door which are 3x3
     */
    @ModifyConstant(method = "lambda$getPattern$5", constant = @Constant(intValue = 8))
    private static int get(int max) {
        return 9;
    }
}
