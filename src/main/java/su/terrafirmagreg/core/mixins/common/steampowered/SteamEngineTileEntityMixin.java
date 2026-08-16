package su.terrafirmagreg.core.mixins.common.steampowered;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;
import com.teammoeg.steampowered.content.engine.SteamEngineTileEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.capability.templates.FluidTank;

// Adds redstone support to the steam engine

@Mixin(value = SteamEngineTileEntity.class, remap = false)
public class SteamEngineTileEntityMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;isEmpty()Z"), remap = false)
    private boolean tfg$isEmpty(FluidTank tank, @Local(name = "state") BlockState state) {
        return tank.isEmpty() || state.getValue(BlockStateProperties.POWERED);
    }
}
