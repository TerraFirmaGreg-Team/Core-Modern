package su.terrafirmagreg.core.mixins.common.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.parts.CableBusContainer;

import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/** AE2 facades are not stored in the block state so we need to handle them specially when doing an oxygen distributor floodfill */
@Mixin(value = CableBusContainer.class, remap = false)
public abstract class CableBusContainerFacadeMixin {
    @Shadow
    public abstract BlockEntity getBlockEntity();

    @Inject(method = "facadeChanged", at = @At("TAIL"))
    private void tfg$facadeChanged(Direction side, CallbackInfo ci) {
        BlockEntity blockEntity = getBlockEntity();
        if (blockEntity == null || !(blockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }
        EnvironmentSystem.dispatchToMachines(level, blockEntity.getBlockPos());
    }
}
