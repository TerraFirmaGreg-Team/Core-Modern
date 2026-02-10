package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

@Mixin(value = TFCBiomes.class, remap = false)
public class TFCBiomesMixin {

    @Inject(method = "getExtensionOrThrow", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtensionOrThrow(LevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.getExtensionOrThrow(level, biome));
        }
    }

    @Inject(method = "hasExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$hasExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<Boolean> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.hasExtension(level, biome));
        }
    }

    @Inject(method = "getExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.getExtension(level, biome));
        }
    }

    @Inject(method = "getAllKeys", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getAllKeys(CallbackInfoReturnable<Collection<ResourceKey<Biome>>> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.getAllKeys());
        }
    }

    @Inject(method = "getExtensions", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtensions(CallbackInfoReturnable<Collection<BiomeExtension>> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.getExtensions());
        }
    }

    @Inject(method = "getExtensionKeys", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtensionKeys(CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.getExtensionKeys());
        }
    }

    @Inject(method = "getById", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getById(ResourceLocation id, CallbackInfoReturnable<BiomeExtension> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.getById(id));
        }
    }

    @Inject(method = "findExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$findExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(TFGBiomes.findExtension(level, biome));
        }
    }
}
