package su.terrafirmagreg.core.mixins.common.gtceu.bedrock_fluids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.level.chunk.LevelChunk;

import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidDefinition;

@Mixin(targets = "com.gregtechceu.gtceu.api.gui.misc.ProspectorMode$2", remap = false)
public class ProspectorModeMixin {

    @Redirect(method = "scan([[[Lcom/gregtechceu/gtceu/api/gui/misc/ProspectorMode$FluidInfo;Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "INVOKE", target = "com/gregtechceu/gtceu/api/data/worldgen/bedrockfluid/BedrockFluidVeinSavedData.getFluidVeinWorldEntry (II)Lcom/gregtechceu/gtceu/api/data/worldgen/bedrockfluid/FluidVeinWorldEntry;"))
    private FluidVeinWorldEntry tfg$makeFluidVeinSafe(BedrockFluidVeinSavedData instance, int fluidDefinition, int weight, @Local(argsOnly = true) LevelChunk chunk) {
        return TFGBedrockFluidDefinition.safelyGetFluidVein(chunk, instance);
    }
}
