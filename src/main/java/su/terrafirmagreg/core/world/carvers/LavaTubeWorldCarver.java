package su.terrafirmagreg.core.world.carvers;

import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import com.eerussianguy.beneath.common.blocks.BeneathBlocks;
import com.mojang.serialization.Codec;

import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.carver.*;

public class LavaTubeWorldCarver extends CaveWorldCarver {
    public LavaTubeWorldCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean carveBlock(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor, CarvingMask carvingMask,
            BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos checkPos, Aquifer aquifer, MutableBoolean reachedSurface) {

        boolean carved = carveAir(context, config, chunk, pos, aquifer);
        if (pos.getY() <= config.lavaLevel.resolveY(context)) {
            carveEdge(chunk, pos, checkPos);
        }
        return carved;
    }

    private boolean carveAir(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk, BlockPos.MutableBlockPos pos, Aquifer aquifer) {

        final BlockState stateAt = chunk.getBlockState(pos);
        if (!Helpers.isBlock(stateAt, Blocks.BEDROCK)) {
            final BlockState carvingState = getAirCarveState(context, config, pos, aquifer);
            if (carvingState != null) {
                chunk.setBlockState(pos, carvingState, false);
                return true;
            }
        }
        return false;
    }

    private void carveEdge(ChunkAccess chunk, BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos checkPos) {
        final BlockState stateAt = chunk.getBlockState(pos);
        final BlockState crackrack = BeneathBlocks.CRACKRACK.get().defaultBlockState();
        if (stateAt == crackrack) {
            checkPos.setWithOffset(pos, Direction.NORTH);
            if (chunk.getBlockState(checkPos) != crackrack) {
                return;
            }

            checkPos.setWithOffset(pos, Direction.EAST);
            if (chunk.getBlockState(checkPos) != crackrack) {
                return;
            }

            checkPos.setWithOffset(pos, Direction.SOUTH);
            if (chunk.getBlockState(checkPos) != crackrack) {
                return;
            }

            checkPos.setWithOffset(pos, Direction.WEST);
            if (chunk.getBlockState(checkPos) != crackrack) {
                return;
            }

            chunk.setBlockState(checkPos, AIR, false);
        }
    }

    @Nullable
    private static BlockState getAirCarveState(CarvingContext context, CaveCarverConfiguration config, BlockPos pos, Aquifer aquifer) {
        if (pos.getY() <= config.lavaLevel.resolveY(context)) {
            return BeneathBlocks.CRACKRACK.get().defaultBlockState();
        } else {
            return aquifer.computeSubstance(new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()), 0);
        }
    }
}
