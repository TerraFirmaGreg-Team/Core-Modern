package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

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

    public boolean hasClimateModifiers() {
        return !climateModifiers.isEmpty();
    }

    public ResourceLocation getId() {
        return id;
    }
}
