package su.terrafirmagreg.core.mixins.client.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;

@Mixin(value = ScreenStyle.class, remap = false)
public interface ScreenStyleAccessor {
    @Accessor("terminalStyle")
    void tfg$setTerminalStyle(TerminalStyle terminalStyle);
}
