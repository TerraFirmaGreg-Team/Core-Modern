package su.terrafirmagreg.core.common.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.JavelinRecallPacket;

/**
 * Event handler for javelin recall functionality.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class JavelinRecallEvent {

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        handleRecall(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleRecall(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleRecall(event.getEntity());
    }

    private static void handleRecall(Player player) {
        if (player.level().isClientSide && player.getOffhandItem().is(Items.LEAD)) {
            TFGNetworkHandler.INSTANCE.sendToServer(new JavelinRecallPacket());
        }
    }
}
