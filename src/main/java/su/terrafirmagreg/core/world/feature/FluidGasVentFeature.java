package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGBlocks;

public class FluidGasVentFeature extends Feature<FluidGasVentConfig> {

    private static Block cachedDryIce = null;
    private static Block cachedGeyserite = null;

    public FluidGasVentFeature(Codec<FluidGasVentConfig> codec) {
        super(codec);
    }

    private static boolean loadBlocks() {
        if (cachedDryIce != null && cachedGeyserite != null)
            return true;
        cachedDryIce = TFGBlocks.DRY_ICE.get();
        cachedGeyserite = ForgeRegistries.BLOCKS
                .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "rock/raw/geyserite"));
        return cachedDryIce != null && cachedGeyserite != null;
    }

    @Override
    public boolean place(FeaturePlaceContext<FluidGasVentConfig> context) {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final var random = context.random();
        final FluidGasVentConfig config = context.config();

        if (random.nextFloat() > config.spawnChance())
            return false;

        if (!loadBlocks())
            return false;

        final int x = pos.getX();
        final int z = pos.getZ();
        final int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

        final BlockState dryIce = cachedDryIce.defaultBlockState();
        final BlockState geyserite = cachedGeyserite.defaultBlockState();
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        final int baseRadius = config.baseRadius();
        for (int dx = -baseRadius - 1; dx <= baseRadius + 1; dx++) {
            for (int dz = -baseRadius - 1; dz <= baseRadius + 1; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                double effectiveRadius = baseRadius + (random.nextDouble() - 0.5) * 1.5;

                if (dist > effectiveRadius)
                    continue;

                mutablePos.set(x + dx, surfaceY - 1, z + dz);
                if (level.isOutsideBuildHeight(mutablePos))
                    continue;

                if (dist < 1.0) {
                    level.getChunk(mutablePos).setBlockState(mutablePos, dryIce, false);
                    placed = true;
                } else if (random.nextFloat() < config.geyseriteChance()) {
                    level.getChunk(mutablePos).setBlockState(mutablePos, geyserite, false);
                    placed = true;
                }
            }
        }

        return placed;
    }
}
