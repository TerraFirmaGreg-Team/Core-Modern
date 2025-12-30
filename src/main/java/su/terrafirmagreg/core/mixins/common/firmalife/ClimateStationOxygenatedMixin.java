package su.terrafirmagreg.core.mixins.common.firmalife;

import java.util.Collections;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.greenhouse.ClimateStationBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import earth.terrarium.adastra.api.systems.OxygenApi;

@Mixin(ClimateStationBlock.class)
public class ClimateStationOxygenatedMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void tfg$oxygenGate(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean hasAdjOxygen = false;
        for (Direction dir : Direction.values()) {
            if (OxygenApi.API.hasOxygen(serverLevel, pos.relative(dir))) {
                hasAdjOxygen = true;
                break;
            }
        }

        if (!hasAdjOxygen) {
            level.getBlockEntity(pos, FLBlockEntities.CLIMATE_STATION.get()).ifPresent(station -> {
                station.setPositions(Collections.emptySet());
                station.updateValidity(false, 0);
            });

            if (state.hasProperty(ClimateStationBlock.STASIS) && state.getValue(ClimateStationBlock.STASIS)) {
                level.setBlockAndUpdate(pos, state.setValue(ClimateStationBlock.STASIS, false));
            }

            player.displayClientMessage(
                    Component.translatable("tfg.tooltip.firmalife_greenhouse.oxygen_required"),
                    true);

            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }
}
