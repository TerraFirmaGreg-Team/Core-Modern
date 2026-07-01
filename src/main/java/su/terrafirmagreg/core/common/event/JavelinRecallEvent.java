package su.terrafirmagreg.core.common.event;

import org.spongepowered.asm.mixin.Unique;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.JavelinRecallPacket;

/**
 * Event handler for javelin recall functionality.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class JavelinRecallEvent {

    @Unique
    private static final TagKey<Item> tfg$ROPE = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("forge", "rope"));

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
        if (player.level().isClientSide && player.getOffhandItem().is(tfg$ROPE)) {
            TFGNetworkHandler.INSTANCE.sendToServer(new JavelinRecallPacket());
        }
    }
}
