package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import java.util.Collection;

import net.dries007.tfc.world.biome.BiomeBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

@Mixin(value = TFCBiomes.class, remap = false)
public class TFCBiomesMixin {

    // Lookup methods fall through to TFC if TFG doesn't know the biome, handling
    // mixed-version chunk boundaries where legacy tfc: biomes appear alongside tfgcore: biomes.
    // We use TFGBiomes.findExtension (direct registry lookup) rather than getExtension to avoid
    // storing null in the biome cache when looking up a TFC biome we don't own.

    @Unique
    private static BiomeExtension tfg$findAndCache(CommonLevelAccessor level, Biome biome) {
        final BiomeExtension ext = TFGBiomes.findExtension(level, biome);
        if (ext != null)
            ((BiomeBridge) (Object) biome).tfc$getExtension(() -> ext);
        return ext;
    }

    @Inject(method = "getExtensionOrThrow", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtensionOrThrow(LevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            final BiomeExtension ext = tfg$findAndCache(level, biome);
            if (ext != null)
                cir.setReturnValue(ext);
        }
    }

    @Inject(method = "hasExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$hasExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<Boolean> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            if (tfg$findAndCache(level, biome) != null)
                cir.setReturnValue(true);
        }
    }

    @Inject(method = "getExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            final BiomeExtension ext = tfg$findAndCache(level, biome);
            if (ext != null)
                cir.setReturnValue(ext);
        }
    }

    @Inject(method = "findExtension", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$findExtension(CommonLevelAccessor level, Biome biome, CallbackInfoReturnable<BiomeExtension> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            final BiomeExtension ext = TFGBiomes.findExtension(level, biome);
            if (ext != null)
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
