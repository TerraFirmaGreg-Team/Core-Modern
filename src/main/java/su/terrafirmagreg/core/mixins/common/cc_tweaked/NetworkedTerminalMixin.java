package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;

import dan200.computercraft.core.terminal.Palette;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;
import dan200.computercraft.shared.computer.terminal.NetworkedTerminal;
import dan200.computercraft.shared.computer.terminal.TerminalState;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TerminalStateAccess;
import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = NetworkedTerminal.class, remap = false)
public class NetworkedTerminalMixin {

    @Unique
    private static final String tfg$BASE_16 = "0123456789abcdef";

    @Unique
    private TerminalState tfg$createVanillaState(NetworkedTerminal terminal, byte[] contents) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeBoolean(terminal.isColour());
        buf.writeVarInt(terminal.getWidth());
        buf.writeVarInt(terminal.getHeight());
        buf.writeVarInt(terminal.getCursorX());
        buf.writeVarInt(terminal.getCursorY());
        buf.writeBoolean(terminal.getCursorBlink());
        buf.writeByte(terminal.getBackgroundColour() << 4 | terminal.getTextColour());
        buf.writeByteArray(contents);

        return new TerminalState(buf);
    }

    @Unique
    private byte[] tfg$createVanillaContents(NetworkedTerminal terminal) {
        var width = terminal.getWidth();
        var height = terminal.getHeight();
        var palette = terminal.getPalette();

        var contents = new byte[width * height * 2 + Palette.PALETTE_SIZE * 3];
        var idx = 0;

        for (var y = 0; y < height; y++) {
            var textLine = terminal.getLine(y);
            var textColourLine = terminal.getTextColourLine(y);
            var backColourLine = terminal.getBackgroundColourLine(y);

            for (var x = 0; x < width; x++) {
                contents[idx++] = (byte) (textLine.charAt(x) & 0xFF);
            }

            for (var x = 0; x < width; x++) {
                contents[idx++] = (byte) (Terminal.getColour(backColourLine.charAt(x), Colour.BLACK) << 4 |
                        Terminal.getColour(textColourLine.charAt(x), Colour.WHITE));
            }
        }

        for (var i = 0; i < Palette.PALETTE_SIZE; i++) {
            for (var channel : palette.getColour(i)) {
                contents[idx++] = (byte) ((int) (channel * 0xFF) & 0xFF);
            }
        }

        return contents;
    }

    @Inject(method = "write", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$writeUtf8State(CallbackInfoReturnable<TerminalState> cir) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var terminal = (NetworkedTerminal) (Object) this;
        var width = terminal.getWidth();
        var height = terminal.getHeight();
        var palette = terminal.getPalette();

        var textContents = new int[width * height];
        var colours = new byte[width * height];
        var paletteBytes = new byte[Palette.PALETTE_SIZE * 3];

        var textIdx = 0;
        var colourIdx = 0;
        var paletteIdx = 0;

        for (var y = 0; y < height; y++) {
            var textLine = terminal.getLine(y);
            var textColourLine = terminal.getTextColourLine(y);
            var backColourLine = terminal.getBackgroundColourLine(y);

            var access = (CcUtf8TextBufferAccess) (Object) textLine;

            for (var x = 0; x < width; x++) {
                textContents[textIdx++] = access.tfg$codePointAt(x);
            }

            for (var x = 0; x < width; x++) {
                colours[colourIdx++] = (byte) (Terminal.getColour(backColourLine.charAt(x), Colour.BLACK) << 4 |
                        Terminal.getColour(textColourLine.charAt(x), Colour.WHITE));
            }
        }

        for (var i = 0; i < Palette.PALETTE_SIZE; i++) {
            for (var channel : palette.getColour(i)) {
                paletteBytes[paletteIdx++] = (byte) ((int) (channel * 0xFF) & 0xFF);
            }
        }

        var state = tfg$createVanillaState(terminal, tfg$createVanillaContents(terminal));
        ((CcUtf8TerminalStateAccess) state).tfg$setUtf8Data(textContents, colours, paletteBytes);

        cir.setReturnValue(state);
    }

    @Inject(method = "read", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$readUtf8State(TerminalState state, CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var utf8State = (CcUtf8TerminalStateAccess) state;
        var textContents = utf8State.tfg$getUtf8Text();
        var colours = utf8State.tfg$getUtf8Colours();
        var paletteBytes = utf8State.tfg$getUtf8Palette();

        if (textContents == null || colours == null || paletteBytes == null) {
            return;
        }

        var access = (TerminalStateAccessor) (Object) state;
        var terminal = (NetworkedTerminal) (Object) this;

        terminal.resize(access.tfg$getWidth(), access.tfg$getHeight());
        terminal.setCursorPos(access.tfg$getCursorX(), access.tfg$getCursorY());
        terminal.setCursorBlink(access.tfg$getCursorBlink());
        terminal.setBackgroundColour(access.tfg$getCursorBgColour());
        terminal.setTextColour(access.tfg$getCursorFgColour());

        var width = terminal.getWidth();
        var height = terminal.getHeight();

        var textIdx = 0;
        var colourIdx = 0;
        var paletteIdx = 0;

        for (var y = 0; y < height; y++) {
            var textLine = terminal.getLine(y);
            var textColourLine = terminal.getTextColourLine(y);
            var backColourLine = terminal.getBackgroundColourLine(y);

            var textAccess = (CcUtf8TextBufferAccess) (Object) textLine;

            for (var x = 0; x < width; x++) {
                textAccess.tfg$setCodePoint(x, textContents[textIdx++]);
            }

            for (var x = 0; x < width; x++) {
                var packedColour = colours[colourIdx++];

                backColourLine.setChar(x, tfg$BASE_16.charAt((packedColour >> 4) & 0xF));
                textColourLine.setChar(x, tfg$BASE_16.charAt(packedColour & 0xF));
            }
        }

        var palette = terminal.getPalette();

        for (var i = 0; i < Palette.PALETTE_SIZE; i++) {
            var r = (paletteBytes[paletteIdx++] & 0xFF) / 255.0;
            var g = (paletteBytes[paletteIdx++] & 0xFF) / 255.0;
            var b = (paletteBytes[paletteIdx++] & 0xFF) / 255.0;

            palette.setColour(i, r, g, b);
        }

        terminal.setChanged();
        ci.cancel();
    }
}
