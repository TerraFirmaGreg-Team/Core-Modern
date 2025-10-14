package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_2.Cruncher;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

@Mixin(value = Cruncher.class)
public class CruncherMixin {

    // Give it some armor
    @Inject(method = "createAttributes", at = @At("RETURN"), remap = false, cancellable = true)
    private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(Attributes.ARMOR, 12F));
    }

    // Disable the inventory thing
    @Inject(method = "openCustomInventoryScreen", at = @At("HEAD"), cancellable = true)
    public void openCustomInventoryScreen(Player player, CallbackInfo ci) {
        ci.cancel();
    }
}
