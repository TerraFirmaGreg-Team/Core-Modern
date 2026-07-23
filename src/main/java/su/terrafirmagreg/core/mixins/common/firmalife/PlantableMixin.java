package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.eerussianguy.firmalife.common.util.Plantable;
import com.google.gson.JsonObject;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Mixin to Firmalife {@link Plantable} to set seed output slot to stackNonDecaying.
 */
@Mixin(value = Plantable.class, remap = false)
public abstract class PlantableMixin {

    @Shadow
    @Final
    @Mutable
    private ItemStack seed;

    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)V", at = @At("TAIL"))
    private void onInit(ResourceLocation id, JsonObject json, CallbackInfo ci) {
        if (this.seed != null && !this.seed.isEmpty()) {
            this.seed = FoodCapability.setStackNonDecaying(this.seed);
        }
    }
}
