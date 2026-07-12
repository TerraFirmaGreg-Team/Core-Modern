package su.terrafirmagreg.tfcambiental.capability;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class TemperaturePacket {
    public CompoundTag tag;

    public TemperaturePacket(CompoundTag tag) {
        this.tag = tag;
    }

    public TemperaturePacket(FriendlyByteBuf buffer) {
        this.tag = buffer.readNbt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(tag);
    }

    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                context.getSender().getCapability(TemperatureCapability.CAPABILITY).ifPresent(cap -> {
                    cap.deserializeNBT(tag);
                });
            } else {
                ((Player) Minecraft.getInstance().getCameraEntity()).getCapability(TemperatureCapability.CAPABILITY).ifPresent(cap -> {
                    cap.deserializeNBT(tag);
                });
            }
        });
    }
}
