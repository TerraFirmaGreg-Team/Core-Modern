package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.notenoughmail.tfcgenviewer.color.Colors;

import net.dries007.tfc.util.DataManager;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.compat.tfcgenviewer.TfcgenViewerBiomeColorContext;
import su.terrafirmagreg.core.world.new_ow_wg.TfgClientPreviewState;

@Mixin(value = DataManager.class, remap = false)
public class DataManagerTfcgenRemapMixin {

    /** TFC's {@code <T> T getOrThrow} erases to Object in bytecode for the wrapper. */
    @WrapMethod(method = "getOrThrow", remap = false)
    private Object tfg$getOrThrowRemapTfgBiomeIdForTfcgenviewer(
            ResourceLocation id, Operation<Object> op) {
        if (!TfcgenViewerBiomeColorContext.isDrawingBiomePreview()) {
            return op.call(id);
        }
        // TFCGenViewer JSON keys are tfc:<short_name>. Biome keys are tfg:earth/<short_name> (TFG) or
        // tfc:earth/...; remap whenever we are resolving colors for the viewer, not only when
        // WorldgenVersionData on client matches the backport.
        ResourceLocation use = id;
        final String path = id.getPath();
        final String ns = id.getNamespace();
        if (path.startsWith("earth/") && (TFGCore.MOD_ID.equals(ns) || "tfc".equals(ns))) {
            use = ResourceLocation.fromNamespaceAndPath("tfc", path.substring(6));
        }
        try {
            return op.call(use);
        } catch (IllegalArgumentException e) {
            if (TfgClientPreviewState.useTfgOverworldPipeline() || !id.equals(use)) {
                return op.call(Colors.UNKNOWN);
            }
            throw e;
        }
    }
}
