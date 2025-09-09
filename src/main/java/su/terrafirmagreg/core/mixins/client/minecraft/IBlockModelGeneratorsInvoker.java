package su.terrafirmagreg.core.mixins.client.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.PropertyDispatch;

@Mixin(value = BlockModelGenerators.class)
public interface IBlockModelGeneratorsInvoker {

    @Invoker
    static PropertyDispatch callCreateFacingDispatch() {
        throw new UnsupportedOperationException();
    }
}
