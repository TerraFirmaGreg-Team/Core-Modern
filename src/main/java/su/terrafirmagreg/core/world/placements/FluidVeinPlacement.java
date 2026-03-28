package su.terrafirmagreg.core.world.placements;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.world.TFGPlacements;

public class FluidVeinPlacement extends PlacementModifier {
    public static final Codec<FluidVeinPlacement> PLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.FLUID.optionalFieldOf("fluid", Fluids.EMPTY).forGetter(c -> c.fluid),
            Codec.INT.optionalFieldOf("min_yield", 0).forGetter(c -> c.min_yield))
            .apply(instance, FluidVeinPlacement::new));

    @Nullable
    private final Fluid fluid;
    private final int min_yield;

    public FluidVeinPlacement(@Nullable Fluid fluid, int min_yield) {
        this.fluid = fluid;
        this.min_yield = min_yield;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TFGPlacements.FLUID_VEIN.get();
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource randomSource, BlockPos blockPos) {
        final WorldGenLevel level = context.getLevel();
        final var savedData = BedrockFluidVeinSavedData.getOrCreate(level.getLevel());
        final ChunkPos chunkPos = new ChunkPos(blockPos);
        final var vein = savedData.getFluidVeinWorldEntry(chunkPos.x, chunkPos.z).getVein();

        // check if there's even a fluid vein
        if (vein == null)
            return Stream.empty();

        // check that the vein's minimum yield is above the config
        if (vein.getMinimumYield() <= min_yield)
            return Stream.empty();

        // if no fluid is specified, place
        if (fluid == Fluids.EMPTY)
            return Stream.of(blockPos);

        // check if the fluid matches
        if (vein.getStoredFluid().get() == fluid)
            return Stream.of(blockPos);
        else
            return Stream.empty();
    }
}
