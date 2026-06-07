package su.terrafirmagreg.core.client.event;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.util.AEColor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.TFGKeybinds;
import su.terrafirmagreg.core.client.screen.ColorRadialMenuScreen;
import su.terrafirmagreg.core.common.item.ChameleonSprayCanItem;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.SelectColorPacket;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        if (TFGKeybinds.SPRAY_CAN_MENU.consumeClick()) {
            ItemStack stack = mc.player.getMainHandItem();
            if (stack.getItem() instanceof ChameleonSprayCanItem) {
                mc.setScreen(new ColorRadialMenuScreen(InteractionHand.MAIN_HAND));
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null)
            return;

        if (mc.options.keyShift.isDown()) {
            ItemStack stack = mc.player.getMainHandItem();
            if (stack.getItem() instanceof ChameleonSprayCanItem) {
                double scrollDelta = event.getScrollDelta();
                event.setCanceled(true);

                int currentColor = stack.getOrCreateTag().getInt("color");
                if (!stack.getOrCreateTag().contains("color"))
                    currentColor = -1;

                int direction = scrollDelta > 0 ? 1 : -1;
                int nextColor = currentColor + direction;

                if (nextColor < -1)
                    nextColor = 15;
                if (nextColor > 15)
                    nextColor = -1;

                TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, nextColor));
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.1f, 1.5f + (nextColor * 0.05f));
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        if (!event.isUseItem())
            return;
        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof ChameleonSprayCanItem))
            return;

        if (!mc.player.isShiftKeyDown())
            return;
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.MISS)
            return;

        event.setCanceled(true);
        mc.setScreen(new ColorRadialMenuScreen(InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public static void onPickBlockColor(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        var player = mc.player;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ChameleonSprayCanItem))
            return;

        var target = mc.hitResult;
        if (target == null || target.getType() != HitResult.Type.BLOCK)
            return;

        event.setCanceled(true);

        var level = player.level();
        var blockHit = (BlockHitResult) target;
        var pos = blockHit.getBlockPos();
        var be = level.getBlockEntity(pos);

        if (GTCEu.Mods.isAE2Loaded() && be instanceof IColorableBlockEntity colorable) {
            if (colorable.getColor().equals(AEColor.TRANSPARENT)) {
                TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, -1));
                return;
            }

            for (AEColor color : AEColor.values()) {
                if (color.equals(colorable.getColor())) {
                    TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, color.ordinal()));
                    return;
                }
            }
        } else if (be instanceof IPipeNode pipe) {
            if (!pipe.isPainted()) {
                TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, -1));
            } else {
                for (int i = 0; i < DyeColor.values().length; i++) {
                    DyeColor color = DyeColor.byId(i);
                    if (color.getMapColor().col == pipe.getPaintingColor()) {
                        TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, i));
                        return;
                    }
                }
            }
        } else if (be instanceof IPaintable paintable) {
            if (!paintable.isPainted()) {
                TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, -1));
            } else {
                for (int i = 0; i < DyeColor.values().length; i++) {
                    DyeColor color = DyeColor.byId(i);
                    if (color.getMapColor().col == paintable.getRealColor()) {
                        TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(InteractionHand.MAIN_HAND, i));
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        stack.getItem();
    }
}
