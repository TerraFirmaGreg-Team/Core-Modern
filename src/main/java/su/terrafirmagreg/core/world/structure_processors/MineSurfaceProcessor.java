package su.terrafirmagreg.core.world.structure_processors;

import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.VerticalSlabBlock;
import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.mojang.serialization.Codec;
import com.therighthon.afc.common.blocks.AFCWood;
import com.therighthon.rnr.common.block.AFCCompatBlocks;
import com.therighthon.rnr.common.block.RNRBlocks;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

        //Fixes domum panels
        if (isDomumPanel(originalBlockState)) {
            ResourceLocation plankKey = ForgeRegistries.BLOCKS
                    .getKey(MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader).getBlock(Wood.BlockType.PLANKS).get());

            CompoundTag originalTag = currentBlockInfo.nbt();
            assert originalTag != null;
            assert plankKey != null;

            originalTag.getCompound("textureData").putString("minecraft:block/oak_planks", plankKey.toString());

            return new StructureTemplate.StructureBlockInfo(blockPos, originalBlockState, originalTag);
        }

        //These three need to be combined or cleaned up
        if (isShingleBlock(originalBlockState)) {
            var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);
            Block shinglesBlock = getShinglesBlock(RNRBlocks.WOOD_SHINGLE_ROOFS, AFCCompatBlocks.WOOD_SHINGLE_ROOFS, woodType);

            return new StructureTemplate.StructureBlockInfo(blockPos, shinglesBlock.defaultBlockState(), currentBlockInfo.nbt());
        }

        if (isShingleStair(originalBlockState)) {
            var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

            Block shingleStair = getShinglesBlock(RNRBlocks.WOOD_SHINGLE_ROOF_STAIRS, AFCCompatBlocks.WOOD_SHINGLE_ROOF_STAIRS, woodType);

            return new StructureTemplate.StructureBlockInfo(blockPos, shingleStair.withPropertiesOf(originalBlockState), currentBlockInfo.nbt());
        }

        if (isShingleSlab(originalBlockState)) {
            var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

            Block shingleSlab = getShinglesBlock(RNRBlocks.WOOD_SHINGLE_ROOF_SLABS, AFCCompatBlocks.WOOD_SHINGLE_ROOF_SLABS, woodType);

            return new StructureTemplate.StructureBlockInfo(blockPos, shingleSlab.withPropertiesOf(originalBlockState), currentBlockInfo.nbt());
        }

        if (isBrickBlock(originalBlockState)) {
            Block brickBlock = MineshaftHelpers.getRuinedBrick(blockPos, levelReader, settings.getRandom(blockPos), true);

            return new StructureTemplate.StructureBlockInfo(blockPos, brickBlock.defaultBlockState(), currentBlockInfo.nbt());
        }

        if (isVerticalSlab(originalBlockState)) {
            String key = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(originalBlockState.getBlock())).getPath();

            ResourceLocation slabKey = null;

            if (key.contains("tfc.rock/bricks")) {
                Block brickBlock = MineshaftHelpers.getRuinedBrick(blockPos, levelReader, settings.getRandom(blockPos), true);
                String brickKey = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(brickBlock)).toString().replace(':', '.') + "_slab";

                slabKey = ResourceLocation.fromNamespaceAndPath(AdditionalPlacementsMod.MOD_ID, brickKey);

            } else if (key.contains("rnr.wood/shingles")) {
                var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

                String shingleKey = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(getShinglesBlock(RNRBlocks.WOOD_SHINGLE_ROOF_SLABS, AFCCompatBlocks.WOOD_SHINGLE_ROOF_SLABS, woodType)))
                        .toString().replace(':', '.');

                slabKey = ResourceLocation.fromNamespaceAndPath(AdditionalPlacementsMod.MOD_ID, shingleKey);
            }

            if (slabKey != null) {
                BlockState brickSlab = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(slabKey)).withPropertiesOf(originalBlockState);
                return new StructureTemplate.StructureBlockInfo(blockPos, brickSlab, currentBlockInfo.nbt());
            }

            return currentBlockInfo;
        }

        //Fallback if block doesn't need to be changed
        return currentBlockInfo;
    }

    private Block getShinglesBlock(Map<Wood, RegistryObject<Block>> registryTFC, Map<AFCWood, RegistryObject<Block>> registryAFC, RegistryWood woodType) {
        if (woodType instanceof Wood wood) {
            return registryTFC.get(wood).get();

        } else if (woodType instanceof AFCWood afcWood) {
            return registryAFC.get(afcWood).get();
        }

        //theoretically shouldn't get called
        return Blocks.AIR;
    }

    private boolean isDomumPanel(BlockState blockState) {
        return blockState.getBlock() instanceof PanelBlock;
    }

    private boolean isVerticalSlab(BlockState blockState) {
        return blockState.getBlock() instanceof VerticalSlabBlock;
    }

    private boolean isBrickBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.PINK_WOOL;
    }

    private boolean isShingleBlock(BlockState blockState) {
        return blockState.is(RNRBlocks.WOOD_SHINGLE_ROOFS.get(Wood.OAK).get());
    }

    private boolean isShingleStair(BlockState blockState) {
        return blockState.is(RNRBlocks.WOOD_SHINGLE_ROOF_STAIRS.get(Wood.OAK).get());
    }

    private boolean isShingleSlab(BlockState blockState) {
        return blockState.is(RNRBlocks.WOOD_SHINGLE_ROOF_SLABS.get(Wood.OAK).get());
    }

}
