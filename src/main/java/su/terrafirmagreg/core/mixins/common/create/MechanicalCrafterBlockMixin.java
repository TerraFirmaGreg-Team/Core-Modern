package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.ItemStack;

@Mixin(value = MechanicalCrafterBlock.class, remap = false)
public class MechanicalCrafterBlockMixin {
    @Redirect(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0), remap = true)
    private boolean tfg$wrenchedTag(ItemEntry instance, ItemStack stack) {
        return AllTags.AllItemTags.WRENCH.matches(stack);
    }
}
