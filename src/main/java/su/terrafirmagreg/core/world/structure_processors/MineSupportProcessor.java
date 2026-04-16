package su.terrafirmagreg.core.world.structure_processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import su.terrafirmagreg.core.world.TFGStructureProcessors;

public class MineSupportProcessor extends StructureProcessor {

    public static final MineSupportProcessor INSTANCE = new MineSupportProcessor();
    public static final Codec<MineSupportProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return TFGStructureProcessors.MINE_SUPPORT_PROCESSOR.get();
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(@NotNull LevelReader levelReader, @NotNull BlockPos pos, @NotNull BlockPos pivot,
            StructureTemplate.@NotNull StructureBlockInfo rawBlockInfo, StructureTemplate.@NotNull StructureBlockInfo currentBlockInfo, @NotNull StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        BlockState mutableBlockState = currentBlockInfo.state();
        BlockPos blockPos = currentBlockInfo.pos();

        if (isSupportBlock(mutableBlockState)) {
            //System.out.println("Found Support");
            //Thread.dumpStack();
            //System.out.println(currentBlockInfo.pos());
            //System.out.println(mutableBlockState.getValues());

            mutableBlockState.setValue(BlockStateProperties.NORTH, isSupportBlock(levelReader.getBlockState(blockPos.north())));
            mutableBlockState.setValue(BlockStateProperties.EAST, isSupportBlock(levelReader.getBlockState(blockPos.east())));
            mutableBlockState.setValue(BlockStateProperties.SOUTH, isSupportBlock(levelReader.getBlockState(blockPos.south())));
            mutableBlockState.setValue(BlockStateProperties.WEST, isSupportBlock(levelReader.getBlockState(blockPos.west())));

            //System.out.println(mutableBlockState.getValues());

            return new StructureTemplate.StructureBlockInfo(blockPos, mutableBlockState, currentBlockInfo.nbt());
        }

        return currentBlockInfo;
    }

    private boolean isSupportBlock(BlockState blockState) {
        var test = blockState.getBlock() instanceof VerticalSupportBlock;
        //System.out.println(blockState.getBlock().toString() + test);
        return test;
    }

}
