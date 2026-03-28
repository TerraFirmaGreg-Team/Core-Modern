package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.HashMap;

import javax.annotation.ParametersAreNonnullByDefault;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidRegistry;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mixin(value = BedrockFluidVeinSavedData.class, remap = false)
public abstract class BedrockFluidVeinSavedDataMixin {

    @Final
    @Shadow
    public HashMap<ChunkPos, FluidVeinWorldEntry> veinFluids;

    @Final
    @Shadow
    private ServerLevel serverLevel;

    @Shadow
    public abstract int getTotalWeight(Holder<Biome> biome);

    @Inject(method = "getFluidVeinWorldEntry", at = @At("HEAD"), cancellable = true)
    private void tfg$getFluidVeinWorldEntry(int chunkX, int chunkZ,
            CallbackInfoReturnable<FluidVeinWorldEntry> cir) {

        if (TFGBedrockFluidRegistry.isEmpty())
            return;

        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (veinFluids.containsKey(pos))
            return;

        BlockPos blockPos = new BlockPos(chunkX << 4, 64, chunkZ << 4);
        var biome = serverLevel.getBiome(blockPos);

        int totalWeight = getTotalWeight(biome);

        // Total weight due to climate condition
        int tfgTotalWeight = totalWeight;
        for (var tfgDef : TFGBedrockFluidRegistry.getAll()) {
            tfgTotalWeight += tfgDef.getClimateWeight(serverLevel, blockPos);
        }

        if (tfgTotalWeight == totalWeight)
            return;

        int query = new XoroshiroRandomSource(
                serverLevel.getSeed() ^ ChunkPos.asLong(
                        BedrockFluidVeinSavedData.getVeinCoord(chunkX),
                        BedrockFluidVeinSavedData.getVeinCoord(chunkZ)))
                .nextInt();

        BedrockFluidDefinition selected = null;
        int weight = Math.abs(query % tfgTotalWeight);

        for (var fluidDefinition : GTRegistries.BEDROCK_FLUID_DEFINITIONS) {
            if (fluidDefinition.getDimensionFilter() != null &&
                    fluidDefinition.getDimensionFilter().stream().noneMatch(
                            dim -> WorldGeneratorUtils.isSameDimension(dim, serverLevel.dimension()))) {
                continue;
            }

            int veinWeight = fluidDefinition.getWeight()
                    + fluidDefinition.getBiomeWeightModifier().applyAsInt(biome);

            // Get iD through GT Registries
            var defId = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluidDefinition);
            if (defId != null) {
                var tfgDef = TFGBedrockFluidRegistry.get(defId);
                if (tfgDef != null) {
                    veinWeight += tfgDef.getClimateWeight(serverLevel, blockPos);
                }
            }

            if (veinWeight <= 0)
                continue;

            weight -= veinWeight;
            if (weight < 0) {
                selected = fluidDefinition;
                break;
            }
        }

        if (selected == null)
            return;

        var random = new XoroshiroRandomSource(
                serverLevel.getSeed() ^ ChunkPos.asLong(chunkX, chunkZ));

        int maximumYield;
        if (selected.getMaximumYield() - selected.getMinimumYield() <= 0) {
            maximumYield = selected.getMinimumYield();
        } else {
            maximumYield = random.nextInt(
                    selected.getMaximumYield() - selected.getMinimumYield())
                    + selected.getMinimumYield();
        }
        maximumYield = Math.min(maximumYield, selected.getMaximumYield());

        veinFluids.put(pos, new FluidVeinWorldEntry(selected, maximumYield,
                BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS));

        ((SavedData) (Object) this).setDirty();

        cir.setReturnValue(veinFluids.get(pos));
    }
}
