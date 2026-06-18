package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.eerussianguy.beneath.common.blockentities.HellforgeBlockEntity;

import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = HellforgeBlockEntity.class, remap = false)
public class HellforgeBlockEntityMixin {

    // Make the hellforge provide heat capability to all 9 blocks above it, instead of just the middle

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/common/capabilities/heat/HeatCapability;provideHeatTo(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;F)V", shift = At.Shift.AFTER), remap = false)
    private static void tfg$serverTick(Level level, BlockPos pos, BlockState state, HellforgeBlockEntity forge, CallbackInfo ci) {
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;

                HeatCapability.provideHeatTo(level, cursor.setWithOffset(pos, x, 1, z), forge.getTemperature());
            }
        }
    }
}
