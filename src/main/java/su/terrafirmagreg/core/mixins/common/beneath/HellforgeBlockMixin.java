package su.terrafirmagreg.core.mixins.common.beneath;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.beneath.common.blocks.HellforgeBlock;

import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.util.MultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = HellforgeBlock.class, remap = false)
public abstract class HellforgeBlockMixin {

    // Lets the hellforge accept any kind of aqueduct instead of only the blackstone one

    @Redirect(method = "makeMultiblock", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/MultiBlock;match(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Lnet/dries007/tfc/util/MultiBlock;", ordinal = 3), remap = false)
    private static MultiBlock tfg$makeMultiblock(MultiBlock mb, BlockPos posOffset, Predicate<BlockState> stateMatcher) {
        return mb.match(posOffset, s -> s.getBlock() instanceof AqueductBlock && s.getFluidState().is(FluidTags.LAVA));
    }
}
