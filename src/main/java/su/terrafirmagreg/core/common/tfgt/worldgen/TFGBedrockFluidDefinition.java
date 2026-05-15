package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.ArrayList;
import java.util.List;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import lombok.Getter;

@Getter
public class TFGBedrockFluidDefinition {

    private final ResourceLocation id;
    private final List<ClimateWeightModifier> climateModifiers = new ArrayList<>();

    public TFGBedrockFluidDefinition(ResourceLocation id) {
        this.id = id;
    }

    public void addClimateModifier(ClimateWeightModifier modifier) {
        climateModifiers.add(modifier);
    }

    public int getClimateWeight(ServerLevel level, BlockPos pos) {
        int total = 0;
        for (ClimateWeightModifier modifier : climateModifiers) {
            total += modifier.applyAsInt(level, pos);
        }
        return total;
    }

    /// Wraps the fluid vein get in chunk access cache stuff
    /// @param chunk ChunkAccess of the current chunk
    /// @param savedData BedrockFluidVeinSavedData of the current level
    /// @return FluidVeinWorldEntry in that chunk
    public static FluidVeinWorldEntry safelyGetFluidVein(ChunkAccess chunk, BedrockFluidVeinSavedData savedData) {

        ClimateWeightModifier.CHUNK_ACCESS_CACHE.put(chunk.getPos(), chunk);
        FluidVeinWorldEntry fluidVein = savedData.getFluidVeinWorldEntry(chunk.getPos().x, chunk.getPos().z);
        ClimateWeightModifier.CHUNK_ACCESS_CACHE.remove(chunk.getPos());

        return fluidVein;
    }

    public boolean hasClimateModifiers() {
        return !climateModifiers.isEmpty();
    }

}
