package su.terrafirmagreg.core.mixins.common.gtceu.materials;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

@Mixin(value = GTMaterialBlocks.class, remap = false)
public class GTMaterialBlocksMixin {

    @Inject(method = "lambda$registerOreIndicator$16(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;", at = @At("HEAD"), cancellable = true)
    private static void tfg$hideIndicatorsOnMap(BlockBehaviour.Properties p, CallbackInfoReturnable<BlockBehaviour.Properties> cir) {
        cir.setReturnValue(p.noLootTable().strength(0.25F).mapColor(MapColor.NONE));
    }
}
