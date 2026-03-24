package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import java.util.Collection;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.world.biome.BiomeBridge;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

@Mixin(value = TFCBiomes.class, remap = false)
public class TFCBiomesMixin {

    /**
     * Wrap tfc$getExtension call inside getExtension to use a different Supplier for 1.21 worldgen backport
     * Our Supplier tries TFG Extensions first, and only falls back to default TFC if none are found.
     * By wrapping it this way, the TFC caching mechanism works properly.
     */
    @WrapOperation(method = "getExtension", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/biome/BiomeBridge;tfc$getExtension(Ljava/util/function/Supplier;)Lnet/dries007/tfc/world/biome/BiomeExtension;"))
    private static BiomeExtension tfg$wrapGetExtension(BiomeBridge bridge, Supplier<BiomeExtension> original, Operation<BiomeExtension> op,
            @Local(argsOnly = true) CommonLevelAccessor level, @Local(argsOnly = true) Biome biome) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            final Supplier<BiomeExtension> supplier = () -> {
                final BiomeExtension ext = TFGBiomes.findExtension(level, biome);
                return ext != null ? ext : original.get();
            };
            return op.call(bridge, supplier);
        } else {
            return op.call(bridge, original);
        }
    }

    @Inject(method = "findExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$findExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            final BiomeExtension ext = TFGBiomes.findExtension(level, biome);
            cir.setReturnValue(ext);
        }
    }

    // Collection methods always only return 1.21 worldgen context.

    @Inject(method = "getAllKeys", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getAllKeys(CallbackInfoReturnable<Collection<ResourceKey<Biome>>> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            cir.setReturnValue(TFGBiomes.getAllKeys());
        }
    }

    @Inject(method = "getExtensions", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtensions(CallbackInfoReturnable<Collection<BiomeExtension>> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            cir.setReturnValue(TFGBiomes.getExtensions());
        }
    }

    @Inject(method = "getExtensionKeys", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtensionKeys(CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            cir.setReturnValue(TFGBiomes.getExtensionKeys());
        }
    }

    @Inject(method = "getById", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getById(ResourceLocation id, CallbackInfoReturnable<BiomeExtension> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            cir.setReturnValue(TFGBiomes.getById(id));
        }
    }
}
