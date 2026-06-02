package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dan200.computercraft.shared.computer.terminal.TerminalState;

@Mixin(value = TerminalState.class, remap = false)
public interface TerminalStateAccessor {

    @Accessor("width")
    int tfg$getWidth();

    @Accessor("height")
    int tfg$getHeight();

    @Accessor("cursorX")
    int tfg$getCursorX();

    @Accessor("cursorY")
    int tfg$getCursorY();

    @Accessor("cursorBlink")
    boolean tfg$getCursorBlink();

    @Accessor("cursorBgColour")
    int tfg$getCursorBgColour();

    @Accessor("cursorFgColour")
    int tfg$getCursorFgColour();
}
