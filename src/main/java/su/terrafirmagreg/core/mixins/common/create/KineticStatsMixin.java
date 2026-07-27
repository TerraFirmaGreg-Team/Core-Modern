package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.item.KineticStats;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Mixin into {@link KineticStats} to add support for {@link IRotate} blocks.
 */
@Mixin(value = KineticStats.class, remap = false)
public abstract class KineticStatsMixin {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void onCreate(Item item, CallbackInfoReturnable<KineticStats> cir) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof IRotate && !(block instanceof KineticBlock)) {
                cir.setReturnValue(new KineticStats(block));
            }
        }
    }
}
