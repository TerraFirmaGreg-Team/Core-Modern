package su.terrafirmagreg.core.world.placements;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import su.terrafirmagreg.core.world.TFGPlacements;

public class FluidVeinPlacement extends PlacementModifier {
    public static final Codec<FluidVeinPlacement> PLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("vein_ids", null).forGetter(c -> c.valid_veins))
            .apply(instance, FluidVeinPlacement::new));

    @Nullable
    private final List<String> valid_veins;

    public FluidVeinPlacement(@Nullable List<String> id) {
        this.valid_veins = id;
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
        final var veinEntry = savedData.getFluidVeinWorldEntry(chunkPos.x, chunkPos.z);
        final var vein = veinEntry.getVein();

        // check if there's even a fluid vein
        if (vein == null || valid_veins == null)
            return Stream.empty();

        // Then check the vein IDs match
        final String veinId = veinEntry.getVeinId();
        if (veinId != null && this.valid_veins.contains(veinId))
            return Stream.of(blockPos);
        else
            return Stream.empty();
    }
}
