package su.terrafirmagreg.core.world.structure_processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.world.TFGStructureProcessors;

public class MineSpiderProcessor extends StructureProcessor {

    public static final MineSpiderProcessor INSTANCE = new MineSpiderProcessor();
    public static final Codec<MineSpiderProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private static final Block COBWEB_SKELETON_A = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("realmrpg_skeletons", "spider_victim_skeleton"));
    private static final Block COBWEB_SKELETON_B = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("realmrpg_skeletons", "webbed_skeleton"));

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return TFGStructureProcessors.MINE_SPIDER_PROCESSOR.get();
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(@NotNull LevelReader levelReader, @NotNull BlockPos pos, @NotNull BlockPos pivot,
            StructureTemplate.@NotNull StructureBlockInfo rawBlockInfo, StructureTemplate.@NotNull StructureBlockInfo currentBlockInfo, @NotNull StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        BlockState originalBlockState = currentBlockInfo.state();
        BlockPos blockPos = currentBlockInfo.pos();

        //Quick exit to help performance
        if (originalBlockState.isAir()) {
            RandomSource random = settings.getRandom(blockPos);

            if (random.nextBoolean() && random.nextBoolean()) {
                return new StructureTemplate.StructureBlockInfo(blockPos, TFGBlocks_Earth.SPIDER_WEB.get().defaultBlockState(), new CompoundTag());
            }

            return currentBlockInfo;
        }

        //        if (isCobwebBlock(originalBlockState)) {
        //            RandomSource random = settings.getRandom(blockPos);
        //
        //            if (random.nextBoolean()) {
        //                return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
        //            }
        //
        //            return currentBlockInfo;
        //        }

        if (isSkeletonBlock(originalBlockState)) {
            RandomSource random = settings.getRandom(blockPos);

            return switch (random.nextIntBetweenInclusive(0, 8)) {
                case 0 ->
                    new StructureTemplate.StructureBlockInfo(blockPos, COBWEB_SKELETON_A.defaultBlockState(), new CompoundTag());
                case 1 ->
                    new StructureTemplate.StructureBlockInfo(blockPos, COBWEB_SKELETON_B.defaultBlockState(), new CompoundTag());
                case 2, 3, 4 ->
                    new StructureTemplate.StructureBlockInfo(blockPos, TFGBlocks_Earth.SPIDER_WEB.get().defaultBlockState(), new CompoundTag());
				default ->
                    new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
            };
        }

        //Fallback if block doesn't need to be changed
        return currentBlockInfo;
    }

    private boolean isSkeletonBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.RED_CONCRETE;
    }

}
