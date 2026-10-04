package su.terrafirmagreg.core.mixins.common.buildinggadgets2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.Protection;

@Mixin(value = ServerTickHandler.class, remap = false)
public class ServerTickHandlerMixin {

    @Inject(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 0, remap = true), cancellable = true)
    private static void tfg$build(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local BlockPos blockPos) {
        checkPermissions(player, blockPos, ci);
    }

    @Inject(method = "exchange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0, remap = true), cancellable = true)
    private static void tfg$exchange(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local BlockPos blockPos) {
        checkPermissions(player, blockPos, ci);
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0, remap = true), cancellable = true)
    private static void tfg$remove(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local BlockPos blockPos) {
        checkPermissions(player, blockPos, ci);
    }

    @Inject(method = "undoDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0, remap = true), cancellable = true)
    private static void tfg$undoDestroy(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local BlockPos blockPos) {
        checkPermissions(player, blockPos, ci);
    }

    @Inject(method = "cut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0, remap = true), cancellable = true)
    private static void tfg$cut(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local BlockPos blockPos) {
        checkPermissions(player, blockPos, ci);
    }

    private static void checkPermissions(Player player, BlockPos blockPos, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer &&
                FTBChunksAPI.api().getManager().shouldPreventInteraction(serverPlayer, InteractionHand.MAIN_HAND, blockPos, Protection.EDIT_BLOCK, null)) {
            ci.cancel();
        }
    }
}
