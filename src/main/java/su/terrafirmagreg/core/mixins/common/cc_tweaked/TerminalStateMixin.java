package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.FriendlyByteBuf;

import dan200.computercraft.shared.computer.terminal.TerminalState;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Saves and restores UTF-8 terminal data in terminal state packets.
 */

@Mixin(value = TerminalState.class, remap = false)
public class TerminalStateMixin implements CcUtf8TerminalStateAccess {

    @Unique
    private static final int tfg$UTF8_MARKER = 0x54464755;

    @Unique
    private int[] tfg$utf8Text;

    @Unique
    private byte[] tfg$utf8Colours;

    @Unique
    private byte[] tfg$utf8Palette;

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"), remap = false)
    private void tfg$readUtf8Data(FriendlyByteBuf buf, CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (buf.readableBytes() < Integer.BYTES) {
            return;
        }

        var readerIndex = buf.readerIndex();

        if (buf.readInt() != tfg$UTF8_MARKER) {
            buf.readerIndex(readerIndex);
            return;
        }

        var textLength = buf.readVarInt();
        var text = new int[textLength];

        for (var i = 0; i < textLength; i++) {
            text[i] = buf.readVarInt();
        }

        tfg$utf8Text = text;
        tfg$utf8Colours = buf.readByteArray();
        tfg$utf8Palette = buf.readByteArray();
    }

    @Inject(method = "write", at = @At("TAIL"), remap = false)
    private void tfg$writeUtf8Data(FriendlyByteBuf buf, CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (tfg$utf8Text == null || tfg$utf8Colours == null || tfg$utf8Palette == null) {
            return;
        }

        buf.writeInt(tfg$UTF8_MARKER);

        buf.writeVarInt(tfg$utf8Text.length);
        for (var codepoint : tfg$utf8Text) {
            buf.writeVarInt(codepoint);
        }

        buf.writeByteArray(tfg$utf8Colours);
        buf.writeByteArray(tfg$utf8Palette);
    }

    @Inject(method = "size", at = @At("RETURN"), cancellable = true, remap = false)
    private void tfg$size(CallbackInfoReturnable<Integer> cir) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (tfg$utf8Text == null || tfg$utf8Colours == null || tfg$utf8Palette == null) {
            return;
        }

        cir.setReturnValue(cir.getReturnValue() + tfg$utf8Text.length * Integer.BYTES + tfg$utf8Colours.length + tfg$utf8Palette.length + 8);
    }

    @Override
    public int[] tfg$getUtf8Text() {
        return tfg$utf8Text;
    }

    @Override
    public byte[] tfg$getUtf8Colours() {
        return tfg$utf8Colours;
    }

    @Override
    public byte[] tfg$getUtf8Palette() {
        return tfg$utf8Palette;
    }

    @Override
    public void tfg$setUtf8Data(int[] text, byte[] colours, byte[] palette) {
        tfg$utf8Text = text;
        tfg$utf8Colours = colours;
        tfg$utf8Palette = palette;
    }
}
