package su.terrafirmagreg.core.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import appeng.api.stacks.GenericStack;

import su.terrafirmagreg.core.compat.ae2.terminal.OrderDestination;
import su.terrafirmagreg.core.compat.ae2.terminal.OrderTerminalMenu;
import su.terrafirmagreg.core.compat.ae2.terminal.StagedStack;

public record OrderDestinationsPacket(List<OrderDestination> destinations, List<GenericStack> waiting, List<StagedStack> staged) {

    public static void encode(OrderDestinationsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.destinations.size());
        for (var destination : packet.destinations) {
            destination.write(buffer);
        }
        buffer.writeVarInt(packet.waiting.size());
        for (var stack : packet.waiting) {
            GenericStack.writeBuffer(stack, buffer);
        }
        buffer.writeVarInt(packet.staged.size());
        for (var stack : packet.staged) {
            stack.write(buffer);
        }
    }

    public static OrderDestinationsPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        var destinations = new ArrayList<OrderDestination>(size);
        for (int i = 0; i < size; i++) {
            destinations.add(OrderDestination.read(buffer));
        }
        size = buffer.readVarInt();
        var waiting = new ArrayList<GenericStack>(size);
        for (int i = 0; i < size; i++) {
            waiting.add(GenericStack.readBuffer(buffer));
        }
        size = buffer.readVarInt();
        var staged = new ArrayList<StagedStack>(size);
        for (int i = 0; i < size; i++) {
            staged.add(StagedStack.read(buffer));
        }
        return new OrderDestinationsPacket(destinations, waiting, staged);
    }

    public static void handle(OrderDestinationsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(OrderDestinationsPacket packet) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof OrderTerminalMenu menu) {
            menu.applySync(packet);
        }
    }
}
