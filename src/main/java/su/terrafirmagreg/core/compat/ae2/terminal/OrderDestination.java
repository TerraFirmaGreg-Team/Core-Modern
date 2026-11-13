package su.terrafirmagreg.core.compat.ae2.terminal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import su.terrafirmagreg.core.compat.ae2.provider.StorageProviderHost;
import su.terrafirmagreg.core.compat.ae2.provider.StorageProviderLogic;

public record OrderDestination(String id, AEItemKey icon, Component name, StorageProviderLogic.Status status, int waitingStacks, int crafts, List<GenericStack> preview) {

    private static final int MAX_PREVIEW = 10;

    public static OrderDestination of(StorageProviderHost host) {
        var logic = host.getLogic();
        var buffer = logic.getBuffer();
        return new OrderDestination(logic.getId().toString(), logic.getDestinationIcon(), logic.getDestinationName(), logic.getStatus(), buffer.size(), logic.getCraftCount(),
                List.copyOf(buffer.subList(0, Math.min(buffer.size(), MAX_PREVIEW))));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        icon.writeToPacket(buffer);
        buffer.writeComponent(name);
        buffer.writeEnum(status);
        buffer.writeVarInt(waitingStacks);
        buffer.writeVarInt(crafts);
        buffer.writeVarInt(preview.size());
        for (var stack : preview) {
            GenericStack.writeBuffer(stack, buffer);
        }
    }

    public static OrderDestination read(FriendlyByteBuf buffer) {
        var id = buffer.readUtf();
        var icon = AEItemKey.fromPacket(buffer);
        var name = buffer.readComponent();
        var status = buffer.readEnum(StorageProviderLogic.Status.class);
        int waitingStacks = buffer.readVarInt();
        int crafts = buffer.readVarInt();
        int previewSize = buffer.readVarInt();
        var preview = new ArrayList<GenericStack>(previewSize);
        for (int i = 0; i < previewSize; i++) {
            preview.add(GenericStack.readBuffer(buffer));
        }
        return new OrderDestination(id, icon, name, status, waitingStacks, crafts, preview);
    }
}
