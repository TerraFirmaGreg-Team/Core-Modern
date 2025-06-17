package su.terrafirmagreg.core.mixins.common.ldlib;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import su.terrafirmagreg.core.common.data.TFGFoodTraits;
import su.terrafirmagreg.core.common.data.tfgt.FoodRefrigeratorMachine;

@Mixin(value = ModularUIContainer.class)
public class ModularUIContainerMixin {

    @Redirect(
            method="quickMoveStack",
            at= @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack tfg$quickMoveStack$slot$getItem(Slot slot) {
        if (slot instanceof SlotWidget.WidgetSlotItemHandler widgetSlotItemHandler) {
            var handler = widgetSlotItemHandler.getItemHandler();
            if (handler instanceof FoodRefrigeratorMachine.RefrigeratedStorage storage) {
                var stack = widgetSlotItemHandler.getItem();
                FoodCapability.removeTrait(stack, TFGFoodTraits.REFRIGERATING);
                return stack;
            }
        }
        return slot.getItem();
    }
}
