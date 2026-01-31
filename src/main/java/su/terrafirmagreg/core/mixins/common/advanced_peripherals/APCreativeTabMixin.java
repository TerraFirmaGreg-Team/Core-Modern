package su.terrafirmagreg.core.mixins.common.advanced_peripherals;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.item.CreativeModeTab;

@Pseudo
@Mixin(targets = "de.srendi.advancedperipherals.APCreativeTab", remap = false)
public class APCreativeTabMixin {

    @Redirect(method = "lambda$populateCreativeTabBuilder$0(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V", at = @At(value = "INVOKE", target = "net/minecraft/world/item/CreativeModeTab$Output.m_246601_ (Ljava/util/Collection;)V"))
    private static void tfg$removeTurtles(CreativeModeTab.Output instance, Collection collection) {
    }
}
