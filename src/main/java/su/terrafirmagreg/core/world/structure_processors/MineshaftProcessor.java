package su.terrafirmagreg.core.world.structure_processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import su.terrafirmagreg.core.world.TFGStructureProcessors;

public class MineshaftProcessor extends StructureProcessor {

    public static final MineshaftProcessor INSTANCE = new MineshaftProcessor();
    public static final Codec<MineshaftProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return TFGStructureProcessors.MINESHAFT_PROCESSOR.get();
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(@NotNull LevelReader levelReader, @NotNull BlockPos pos, @NotNull BlockPos pivot,
            StructureTemplate.@NotNull StructureBlockInfo originalBlockInfo, StructureTemplate.@NotNull StructureBlockInfo currentBlockInfo, @NotNull StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        if (currentBlockInfo.state().is(Blocks.COBBLESTONE)) {
            System.out.println(currentBlockInfo.pos());

            if (levelReader instanceof ServerLevel level) {
                System.out.println("match");
                var gen = level.getChunkSource().getGenerator();
                System.out.println(gen);
                var dataProv = ChunkDataProvider.get(gen);
                System.out.println(dataProv);
                //Need to somehow to get ChunkDataProvider since this is during worldgen
                var data = dataProv.get(level.getChunk(originalBlockInfo.pos()));
                System.out.println(data.status()); //Getting INVALID ?????

                var newBlock = new StructureTemplate.StructureBlockInfo(originalBlockInfo.pos(), Blocks.GREEN_GLAZED_TERRACOTTA.defaultBlockState(), currentBlockInfo.nbt());
                System.out.println(newBlock.pos());
                System.out.println(newBlock.state());
                System.out.println(newBlock.nbt());

                return newBlock;

                /*
                if (data.status() == ChunkData.Status.INVALID)
                return originalBlockInfo;
                System.out.println(data.getRockData());
                var rock = data.getRockData().getRock(pos);
                
                return new StructureTemplate.StructureBlockInfo(
                    originalBlockInfo.pos(),
                    rock.cobble().defaultBlockState(),
                    originalBlockInfo.nbt());*/
            }
        }

        return currentBlockInfo;
    }
}
