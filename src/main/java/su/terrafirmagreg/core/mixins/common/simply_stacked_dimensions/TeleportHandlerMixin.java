package su.terrafirmagreg.core.mixins.common.simply_stacked_dimensions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simplystacked.Teleporting.TeleportHandler;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = TeleportHandler.class)
public abstract class TeleportHandlerMixin {

    @Unique
    private final static BlockState m_air = Blocks.AIR.defaultBlockState();

    @WrapOperation(method = "onLivingTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean tfg$expandAirPocket(ServerLevel level, BlockPos pos, BlockState blockState, Operation<Boolean> original, @Local LivingEntity entity) {

        // Expand the hole outwards into a 3x3
        tfg$clearRock(level, pos.north());
        tfg$clearRock(level, pos.north().east());

        tfg$clearRock(level, pos.east());
        tfg$clearRock(level, pos.east().south());

        tfg$clearRock(level, pos.south());
        tfg$clearRock(level, pos.south().west());

        tfg$clearRock(level, pos.west());
        tfg$clearRock(level, pos.west().north());

        // Add some slow falling and fire res
        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30 * 20));
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 30 * 20));

        return original.call(level, pos, blockState);
    }

    @Unique
    private static void tfg$clearRock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(TFCTags.Blocks.CAN_COLLAPSE) || state.is(TFCTags.Blocks.CAN_LANDSLIDE)) {
            level.setBlockAndUpdate(pos, m_air);
        }
    }
}
