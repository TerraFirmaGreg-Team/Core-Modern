package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.ItemStack;

@Mixin(MechanicalCrafterBlock.class)
public class MechanicalCrafterBlockMixin {
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"), remap = false)
    private boolean tfg$wrenchedTag(ItemEntry instance, ItemStack stack) {
        return AllTags.AllItemTags.WRENCH.matches(stack);
    }
}
