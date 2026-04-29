package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.notenoughmail.tfcgenviewer.color.BiomeColors;
import com.notenoughmail.tfcgenviewer.color.ColorDefinition;

import net.dries007.tfc.util.RegisteredDataManager;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.compat.tfcgenviewer.TfcgenViewerBiomeColorContext;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

/**
 * Viewer biome colors: preview context for remaps ({@link TfcgenViewerBiomeColorContext}) and pre-registration so
 * 1.21-backport TFG biomes under {@code tfg:earth/...} get slots under {@code tfc:<name>} matching TFCGenViewer JSON.
 */
@Mixin(value = BiomeColors.class, remap = false)
public class BiomeColorsMixin {

    @WrapMethod(method = "color", remap = false)
    private int tfg$biomeColorWithContext(
            int biome, Int2ObjectOpenHashMap<int[]> colorDescriptors, Operation<Integer> op) {
        TfcgenViewerBiomeColorContext.enter();
        try {
            return op.call(biome, colorDescriptors);
        } finally {
            TfcgenViewerBiomeColorContext.leave();
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tfg$preRegisterTfgBiomeDataSlots(CallbackInfo ci) {
        RegisteredDataManager<ColorDefinition> self = (RegisteredDataManager<ColorDefinition>) (Object) this;
        for (ResourceLocation id : TFGBiomes.getExtensionKeys()) {
            if (TFGCore.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith("earth/")) {
                self.register(ResourceLocation.fromNamespaceAndPath("tfc", id.getPath().substring(6)));
            }
        }
    }
}
