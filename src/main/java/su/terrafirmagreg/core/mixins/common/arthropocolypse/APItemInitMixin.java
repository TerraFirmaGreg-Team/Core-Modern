//package su.terrafirmagreg.core.mixins.common.arthropocolypse;
//
//import dev.dh.arthropocolypse.init.APItemInit;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(value = APItemInit.class, remap = false)
//public class APItemInitMixin {
//
//	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item$Properties;food(Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/Item$Properties;"))
//	private static void tfg$removeFood() {
//
//	}
//}
