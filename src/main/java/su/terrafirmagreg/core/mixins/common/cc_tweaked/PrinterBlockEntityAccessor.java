package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import dan200.computercraft.shared.peripheral.printer.PrinterBlockEntity;

@Mixin(value = PrinterBlockEntity.class, remap = false)
public interface PrinterBlockEntityAccessor {

    @Invoker("setPageTitle")
    void tfg$setPageTitle(String title);
}
