package su.terrafirmagreg.core.mixins.common.beneath;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.eerussianguy.beneath.common.blocks.HellforgeBlock;

import net.dries007.tfc.util.MultiBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = HellforgeBlock.class, remap = false)
public interface HellforgeBlockInvoker {

    // This has to be in a separate interface otherwise the game crashes

    @Invoker("makeMultiblock")
    static MultiBlock makeMultiblock(Predicate<BlockState> centerTest, Predicate<BlockState> sideTest) {
        throw new AssertionError();
    }

}
