package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.dries007.tfc.ForgeEventHandler;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.capabilities.size.Weight;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;

@Mixin(value = ForgeEventHandler.class, remap = false)
public class ForgeEventHandlerMixin {

    @WrapOperation(method = "onPlayerTick(Lnet/minecraftforge/event/TickEvent$PlayerTickEvent;)V", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/Helpers;countOverburdened(Lnet/minecraft/world/Container;)I", remap = false), remap = false)
    private static int tfg$redirectCountOverburdened(Container container, Operation<Integer> original) {
        int count = tfg$countOverburdenedFull(container);
        // Protein nutrition >85%: allow 1 extra hugeHeavy item before overburdened effect applies.
        if (container instanceof Inventory inventory) {
            Player player = inventory.player;
            if (NutrientEffectsHandler.hasProteinHeavyItemBoost(player.getUUID())) {
                count = Math.max(0, count - 1);
            }
        }
        return Math.min(count, 2);
    }

    // Remake overburdened check because TFC did it weird.
    @Unique
    private static int tfg$countOverburdenedFull(Container container) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                var size = ItemSizeManager.get(stack);
                if (size.getWeight(stack) == Weight.VERY_HEAVY && size.getSize(stack) == Size.HUGE) {
                    count++;
                }
            }
        }
        return count;
    }
}
