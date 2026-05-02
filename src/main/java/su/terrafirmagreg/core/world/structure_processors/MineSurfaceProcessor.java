package su.terrafirmagreg.core.world.structure_processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.mojang.serialization.Codec;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.utils.MineshaftHelpers;
import su.terrafirmagreg.core.world.TFGStructureProcessors;

public class MineSurfaceProcessor extends StructureProcessor {

    public static final MineSurfaceProcessor INSTANCE = new MineSurfaceProcessor();
    public static final Codec<MineSurfaceProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return TFGStructureProcessors.MINE_SURFACE_PROCESSOR.get();
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(@NotNull LevelReader levelReader, @NotNull BlockPos pos, @NotNull BlockPos pivot,
            StructureTemplate.@NotNull StructureBlockInfo rawBlockInfo, StructureTemplate.@NotNull StructureBlockInfo currentBlockInfo, @NotNull StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        BlockState originalBlockState = currentBlockInfo.state();
        BlockPos blockPos = currentBlockInfo.pos();

        //Quick exit to help performance
        if (originalBlockState.isAir()) {
            return currentBlockInfo;
        }

        if (isDomumPanel(originalBlockState)) {
            ResourceLocation plankKey = ForgeRegistries.BLOCKS
                    .getKey(MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader).getBlock(Wood.BlockType.PLANKS).get());

            CompoundTag originalTag = currentBlockInfo.nbt();
            assert originalTag != null;

            originalTag.getCompound("textureData");
            originalTag.put("textureData", new MaterialTextureData().serializeNBT());
        }

        //Fallback if block doesn't need to be changed
        return currentBlockInfo;
    }

    private boolean isDomumPanel(BlockState blockState) {
        return blockState.getBlock() instanceof PanelBlock;
    }

}
