package su.terrafirmagreg.tfcambiental.api;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface BlockTemperatureProvider {
    Optional<TempModifier> getModifier(Player player, BlockPos pos, BlockState state);

    static void evaluateAll(Player player, TempModifierStorage storage) {
        BlockPos p = player.getOnPos();
        BlockPos pos1 = new BlockPos(p.getX() - 9, p.getY() - 3, p.getZ() - 9);
        BlockPos pos2 = new BlockPos(p.getX() + 9, p.getY() + 5, p.getZ() + 9);
        Iterable<BlockPos> allPositions = BlockPos.betweenClosed(pos1, pos2);
        for (BlockPos pos : allPositions) {
            if (!player.level().isLoaded(pos))
                continue;

            BlockState state = player.level().getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            // Distance modifier
            double distance = Math.sqrt(player.getOnPos().distSqr(pos));
            float distanceMultiplier = (float) distance / 9f;
            distanceMultiplier = Math.min(1f, Math.max(0f, distanceMultiplier));
            distanceMultiplier = 1f - distanceMultiplier;
            boolean isInside = EnvironmentalTemperatureProvider.getSkylight(player) < 14 && EnvironmentalTemperatureProvider.getBlockLight(player) > 3;
            if (isInside) {
                distanceMultiplier *= 1.3f;
            }
            final float finalDistanceMultiplier = distanceMultiplier;

            for (BlockTemperatureProvider provider : AmbientalRegistry.BLOCKS) {
                provider.getModifier(player, pos, state).ifPresent((mod) -> {
                    mod.setChange(mod.getChange() * finalDistanceMultiplier);
                    mod.setPotency(mod.getPotency() * finalDistanceMultiplier);
                    mod.setWetness(mod.getWetness() * finalDistanceMultiplier);
                    storage.add(mod);
                });
            }

            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (blockEntity != null) {
                for (BlockEntityTemperatureProvider provider : AmbientalRegistry.BLOCK_ENTITIES) {
                    provider.getModifier(player, blockEntity).ifPresent((mod) -> {
                        mod.setChange(mod.getChange() * finalDistanceMultiplier);
                        mod.setPotency(mod.getPotency() * finalDistanceMultiplier);
                        mod.setWetness(mod.getWetness() * finalDistanceMultiplier);
                        storage.add(mod);
                    });
                }
            }
        }
    }
}
