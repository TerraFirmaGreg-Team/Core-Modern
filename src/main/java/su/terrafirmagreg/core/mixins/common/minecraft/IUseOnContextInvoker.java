package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.context.UseOnContext;

@Mixin(value = UseOnContext.class)
public interface IUseOnContextInvoker {

//    @Invoker
//    BlockHitResult invokeGetHitResult();

}
