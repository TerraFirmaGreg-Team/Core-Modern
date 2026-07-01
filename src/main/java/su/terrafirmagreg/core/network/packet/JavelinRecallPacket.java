package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Unique;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.entity.projectile.ILeashedJavelin;

/**
 * Packet to recall a leashed javelin.
 */
@SuppressWarnings("unused")
public class JavelinRecallPacket {
    public JavelinRecallPacket() {
    }

    @Unique
    private static final TagKey<Item> tfg$ROPE = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("forge", "rope"));

    public static void encode(JavelinRecallPacket pkt, FriendlyByteBuf buf) {
    }

    public static JavelinRecallPacket decode(FriendlyByteBuf buf) {
        return new JavelinRecallPacket();
    }

    public static void handle(JavelinRecallPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getOffhandItem().is(tfg$ROPE)) {
                player.level().getEntitiesOfClass(net.minecraft.world.entity.Entity.class, player.getBoundingBox().inflate(64),
                        (e) -> e instanceof ILeashedJavelin leashed && leashed.tfg$isLeashed() && leashed.tfg$getLeasher() == player)
                        .forEach(e -> ((ILeashedJavelin) e).tfg$setRecalling(true));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
