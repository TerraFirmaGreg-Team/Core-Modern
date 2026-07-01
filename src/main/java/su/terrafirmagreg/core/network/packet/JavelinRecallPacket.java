package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.common.entity.projectile.ILeashedJavelin;

/**
 * Packet to recall a leashed javelin.
 */
public class JavelinRecallPacket {
    public JavelinRecallPacket() {
    }

    public static void encode(JavelinRecallPacket pkt, FriendlyByteBuf buf) {
    }

    public static JavelinRecallPacket decode(FriendlyByteBuf buf) {
        return new JavelinRecallPacket();
    }

    public static void handle(JavelinRecallPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getOffhandItem().is(Items.LEAD)) {
                player.level().getEntitiesOfClass(net.minecraft.world.entity.Entity.class, player.getBoundingBox().inflate(64),
                        (e) -> e instanceof ILeashedJavelin leashed && leashed.tfg$isLeashed() && leashed.tfg$getLeasher() == player)
                        .forEach(e -> ((ILeashedJavelin) e).tfg$setRecalling(true));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
