package su.terrafirmagreg.core.mixins.common.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket;

import net.minecraft.network.FriendlyByteBuf;

import su.terrafirmagreg.core.world.new_ow_wg.TfgClientPreviewState;
import su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData;

@Mixin(value = ViewerResponsePacket.class, remap = false)
public class ViewerResponsePacketMixin {

    @WrapMethod(method = "decode", remap = false)
    private static ViewerResponsePacket tfg$decodeWithOverworldTail(FriendlyByteBuf data, Operation<ViewerResponsePacket> op) {
        ViewerResponsePacket packet = op.call(data);
        if (data.readableBytes() > 0) {
            TfgClientPreviewState.setPendingPacketOverworldVersion(data.readVarInt());
        } else {
            TfgClientPreviewState.setPendingPacketOverworldVersion(0);
        }
        return packet;
    }

    @Inject(method = "encode", at = @At("TAIL"), remap = false)
    private void tfg$appendServerOverworldVersionTail(FriendlyByteBuf data, CallbackInfo ci) {
        data.writeVarInt(WorldgenVersionData.OVERWORLD_VERSION);
    }
}
