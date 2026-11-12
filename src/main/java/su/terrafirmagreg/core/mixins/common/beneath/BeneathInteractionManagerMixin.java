package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.beneath.common.blocks.BeneathBlocks;
import com.eerussianguy.beneath.misc.BeneathInteractionManager;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.dries007.tfc.util.BlockItemPlacement;
import net.dries007.tfc.util.InteractionManager;

@Mixin(value = BeneathInteractionManager.class, remap = false)
public class BeneathInteractionManagerMixin {

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/InteractionManager;register(Lnet/dries007/tfc/util/BlockItemPlacement;)V"))
    private static void tfg$sulfurPowder(BlockItemPlacement blockItemPlacement) {
        InteractionManager.register(new BlockItemPlacement(() -> ChemicalHelper.get(TagPrefix.dustSmall, GTMaterials.Sulfur, 1).getItem(), BeneathBlocks.SULFUR));
    }
}
