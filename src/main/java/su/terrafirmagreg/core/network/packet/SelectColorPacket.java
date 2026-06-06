package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.common.item.ChameleonSprayCanItem;
import su.terrafirmagreg.core.common.item.behavior.ChameleonSprayCanBehaviour;

public class SelectColorPacket {

    private final InteractionHand hand;
    private final int selectedIndex;

    public SelectColorPacket(InteractionHand hand, int selectedIndex) {
        this.hand = hand;
        this.selectedIndex = selectedIndex;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(hand);
        buf.writeVarInt(selectedIndex);
    }

    public static SelectColorPacket decode(FriendlyByteBuf buf) {
        return new SelectColorPacket(buf.readEnum(InteractionHand.class), buf.readVarInt());
    }

    public static void handle(SelectColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null)
                return;

            ItemStack stack = player.getItemInHand(msg.hand);
            if (stack.getItem() instanceof ChameleonSprayCanItem) {

                if (stack.hasTag()) {
                    stack.getTag().remove("chromatic_code");
                }

                DyeColor[] colors = DyeColor.values();
                DyeColor selectedColor = null;

                if (msg.selectedIndex >= 0 && msg.selectedIndex < colors.length) {
                    selectedColor = colors[msg.selectedIndex];
                }

                ChameleonSprayCanBehaviour.setColor(stack, selectedColor);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
