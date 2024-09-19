package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.compat.kjs.events.TFGMaterialInfoModification;

@Mixin(value = ChemicalHelper.class, remap = false)
public abstract class ChemicalHelperMixin {

    @Inject(method = "registerMaterialInfo", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo, CallbackInfo ci) {
        var rl = ForgeRegistries.ITEMS.getKey(item.asItem());

        var res = TFGMaterialInfoModification.EXCLUDED_ITEMS.stream().filter(el -> el.equals(rl)).findFirst().orElse(null);
        if (res != null)
            ci.cancel();
    }
}