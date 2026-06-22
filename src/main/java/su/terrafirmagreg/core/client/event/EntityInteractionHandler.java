package su.terrafirmagreg.core.client.event;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.item.ChameleonSprayCanItem;
import su.terrafirmagreg.core.common.item.behavior.ChameleonSprayCanBehaviour;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityInteractionHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof ChameleonSprayCanItem) {

            if (event.getTarget() instanceof Wolf wolf) {

                if (wolf.isTame()) {
                    DyeColor currentColor = ChameleonSprayCanBehaviour.getColor(stack);
                    if (currentColor != null) {
                        if (!event.getLevel().isClientSide()) {
                            wolf.setCollarColor(currentColor);
                        }
                        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
                        event.setCanceled(true);
                    }
                }
            } else if (event.getTarget() instanceof Cat cat) {
                if (cat.isTame()) {
                    DyeColor currentColor = ChameleonSprayCanBehaviour.getColor(stack);
                    if (currentColor != null) {
                        if (!event.getLevel().isClientSide()) {
                            cat.setCollarColor(currentColor);
                        }
                        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
