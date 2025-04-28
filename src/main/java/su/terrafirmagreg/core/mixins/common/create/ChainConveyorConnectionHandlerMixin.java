package su.terrafirmagreg.core.mixins.common.create;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.compat.create.ChainGTMaterialInterface;

@Mixin(value = ChainConveyorConnectionHandler.class, remap = false)
public abstract class ChainConveyorConnectionHandlerMixin {

    @Inject(method = "isChain(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$isChain(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if(itemStack.is(TFGTags.Items.Chains))
        {
            cir.setReturnValue(true);
        }
    }
}
