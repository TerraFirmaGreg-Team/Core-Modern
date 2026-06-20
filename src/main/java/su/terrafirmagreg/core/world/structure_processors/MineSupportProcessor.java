package su.terrafirmagreg.core.world.structure_processors;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.blocks.DeadWallTorchBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.LampBlock;
import net.dries007.tfc.common.blocks.wood.HorizontalSupportBlock;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.utils.MineshaftHelpers;
import su.terrafirmagreg.core.world.TFGStructureProcessors;

public class MineSupportProcessor extends StructureProcessor {

    public static final MineSupportProcessor INSTANCE = new MineSupportProcessor();
    public static final Codec<MineSupportProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private static final Block FORCED_LAMP = TFCBlocks.METALS.get(Metal.Default.BLUE_STEEL).get(Metal.BlockType.LAMP).get();

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return TFGStructureProcessors.MINE_SUPPORT_PROCESSOR.get();
    }

    //LevelReader here is actually a ServerLevelAccessor, since it's only called by a method that passes a ServerLevelAccessor
    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(@NotNull LevelReader levelReader, @NotNull BlockPos pos, @NotNull BlockPos pivot,
            StructureTemplate.@NotNull StructureBlockInfo rawBlockInfo, StructureTemplate.@NotNull StructureBlockInfo currentBlockInfo, @NotNull StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        BlockState originalBlockState = currentBlockInfo.state();
        BlockPos blockPos = currentBlockInfo.pos();
        if (blockPos.equals(pos)) {
            //System.out.println("first pos of piece");
            if (template != null) {
                BoundingBox pieceBounds = template.getBoundingBox(settings, pos);
                //System.out.println("piece bounds " + pieceBounds);
                var outerEdge = new BoundingBox(pieceBounds.minX() - 1, pieceBounds.minY(), pieceBounds.minZ() - 1, pieceBounds.maxX() + 1, pieceBounds.maxY() + 1, pieceBounds.maxZ() + 1);

                //System.out.println("outer edge " + outerEdge);
                Set<BlockPos> outerBlocks = MineshaftHelpers.getBoxEdges(outerEdge, pieceBounds);

                if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                    outerBlocks.forEach(checkedPos -> {
                        if (levelAccessor.getBlockState(checkedPos).getFluidState() != Fluids.EMPTY.defaultFluidState()) {
                            //System.out.println("found liquid at " + checkedPos);
                            levelAccessor.setBlock(checkedPos,
                                    MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader).getBlock(Wood.BlockType.PLANKS).get().defaultBlockState(), 2);
                        }
                    });
                }
            }
        }

        //Quick exit to help performance
        if (originalBlockState.isAir()) {
            return currentBlockInfo;
        }

        if (isSpiderWeb(originalBlockState)) {
            return currentBlockInfo;
        }

        //Fills in air gaps in the floor with regions wood planks
        if (isFloorBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                BlockState levelBlockState = levelAccessor.getBlockState(blockPos);

                if (levelBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.UP)) {
                    return new StructureTemplate.StructureBlockInfo(blockPos, levelBlockState, currentBlockInfo.nbt());
                }

                var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

                return new StructureTemplate.StructureBlockInfo(blockPos, woodType.getBlock(Wood.BlockType.PLANKS).get().defaultBlockState(), currentBlockInfo.nbt());
            }
        }

        //Changes supports to regions wood type
        if (isSupportBlock(originalBlockState)) {
            var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

            Block newBlock;

            if (isHorizSupportBlock(originalBlockState)) {
                newBlock = woodType.getBlock(Wood.BlockType.HORIZONTAL_SUPPORT).get();
            } else {
                newBlock = woodType.getBlock(Wood.BlockType.VERTICAL_SUPPORT).get();
            }

            BlockState newBlockState = newBlock.withPropertiesOf(originalBlockState);

            return new StructureTemplate.StructureBlockInfo(blockPos, newBlockState, currentBlockInfo.nbt());
        }

        //Adds some random air pockets to simulate mining
        if (isRandomRawRock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                RandomSource random = levelAccessor.getRandom();
                FluidState worldFluid = levelAccessor.getFluidState(blockPos);
                Block newBlock = random.nextBoolean() && worldFluid == Fluids.EMPTY.defaultFluidState() ? levelAccessor.getBlockState(blockPos).getBlock() : Blocks.AIR;

                return new StructureTemplate.StructureBlockInfo(blockPos, newBlock.defaultBlockState(), currentBlockInfo.nbt());
            }
        }

        //Cyan wool acts as anti-fluid structure void
        if (isFluidVoid(originalBlockState)) {
            BlockState levelBlockState = levelReader.getBlockState(blockPos);
            if (levelBlockState.getFluidState() != Fluids.EMPTY.defaultFluidState()) {
                var newBlock = MineshaftHelpers.getRockType(blockPos, levelReader).raw();

                return new StructureTemplate.StructureBlockInfo(blockPos, newBlock.defaultBlockState(), currentBlockInfo.nbt());
            }

            return new StructureTemplate.StructureBlockInfo(blockPos, levelBlockState, currentBlockInfo.nbt());
        }

        //Cyan concrete is just there so the start pos can be recognized
        if (isTempVoid(originalBlockState)) {
            BlockState levelBlockState = levelReader.getBlockState(blockPos);
            return new StructureTemplate.StructureBlockInfo(blockPos, levelBlockState, currentBlockInfo.nbt());
        }

        //Changes wood planks to regions wood type
        if (isPlankBlock(originalBlockState)) {
            var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

            Block newBlock = woodType.getBlock(Wood.BlockType.PLANKS).get();

            return new StructureTemplate.StructureBlockInfo(blockPos, newBlock.defaultBlockState(), currentBlockInfo.nbt());
        }

        //Changes logs to regions wood type
        if (isLogBlock(originalBlockState)) {
            var woodType = MineshaftHelpers.getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

            Block newBlock = woodType.getBlock(Wood.BlockType.LOG).get();

            return new StructureTemplate.StructureBlockInfo(blockPos, newBlock.withPropertiesOf(originalBlockState), currentBlockInfo.nbt());
        }

        //Adds hardened stone to unstable roofs
        if (isHardenedRock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                BlockState levelBlockState = levelAccessor.getBlockState(blockPos);

                if (levelBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.DOWN)) {
                    var hardRock = MineshaftHelpers.getRockType(blockPos, levelReader).hardened().defaultBlockState();
                    return new StructureTemplate.StructureBlockInfo(blockPos, hardRock, currentBlockInfo.nbt());
                }

                return new StructureTemplate.StructureBlockInfo(blockPos, levelBlockState, currentBlockInfo.nbt());
            }
        }

        //Adds correct stone brick
        if (isBrickBlock(originalBlockState)) {
            var brickBlock = MineshaftHelpers.getRuinedBrick(blockPos, levelReader, settings.getRandom(blockPos), false);

            return new StructureTemplate.StructureBlockInfo(blockPos, brickBlock.defaultBlockState(), currentBlockInfo.nbt());
        }

        //"fixes" torches most of the time
        if (isTorchBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {

                if (levelAccessor.getBlockState(blockPos.north()).isFaceSturdy(levelAccessor, blockPos, Direction.SOUTH)) {
                    return currentBlockInfo;
                } else if (levelAccessor.getBlockState(blockPos.south()).isFaceSturdy(levelAccessor, blockPos, Direction.NORTH)) {
                    return currentBlockInfo;
                } else if (levelAccessor.getBlockState(blockPos.west()).isFaceSturdy(levelAccessor, blockPos, Direction.EAST)) {
                    return currentBlockInfo;
                } else if (levelAccessor.getBlockState(blockPos.east()).isFaceSturdy(levelAccessor, blockPos, Direction.WEST)) {
                    return currentBlockInfo;
                }
            }

            return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());

        }

        //Adds fuel to hanging lamps, changes material, and checks for block above
        if (isHangingLampBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                RandomSource random = levelAccessor.getRandom();

                BlockState aboveBlockState = levelAccessor.getBlockState(blockPos.above());

                if (aboveBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.DOWN) || originalBlockState.is(FORCED_LAMP)) {
                    CompoundTag lampTag = currentBlockInfo.nbt();
                    assert lampTag != null;
                    var tankTag = lampTag.getCompound("tank");

                    tankTag.putString("FluidName", "gtceu:seed_oil");
                    tankTag.putInt("Amount", random.nextInt(0, 80));
                    lampTag.put("tank", tankTag);

                    BlockState newLamp = MineshaftHelpers.getOrAddLampCache(levelAccessor.getChunk(blockPos).getPos(), levelReader).getHangingLamp().withPropertiesOf(originalBlockState);

                    return new StructureTemplate.StructureBlockInfo(blockPos, newLamp, lampTag);
                }

                return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
            }
        }

        //Changes material of fallen lamps
        if (isFallenLampBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                BlockState belowBlockState = levelAccessor.getBlockState(blockPos.below());

                if (belowBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.UP) || originalBlockState.is(FORCED_LAMP)) {
                    BlockState newLamp = MineshaftHelpers.getOrAddLampCache(levelAccessor.getChunk(blockPos).getPos(), levelReader).getFallenLamp().withPropertiesOf(originalBlockState);

                    return new StructureTemplate.StructureBlockInfo(blockPos, newLamp, currentBlockInfo.nbt());
                }

                return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
            }
        }

        // Remove placeholder bamboo block
        if (originalBlockState.getBlock() == Blocks.BAMBOO_BLOCK) {
            return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
        }

        //Fallback if block doesn't need to be changed
        return currentBlockInfo;
    }

    @Override
    public @NotNull List<StructureTemplate.StructureBlockInfo> finalizeProcessing(@NotNull ServerLevelAccessor serverLevel, @NotNull BlockPos offset, @NotNull BlockPos pos,
            @NotNull List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
            @NotNull List<StructureTemplate.StructureBlockInfo> processedBlockInfos, StructurePlaceSettings settings) {

        settings.setKeepLiquids(false);
        return super.finalizeProcessing(serverLevel, offset, pos, originalBlockInfos, processedBlockInfos, settings);
    }

    private boolean isSupportBlock(BlockState blockState) {
        return blockState.getBlock() instanceof VerticalSupportBlock;
    }

    private boolean isHorizSupportBlock(BlockState blockState) {
        return blockState.getBlock() instanceof HorizontalSupportBlock;
    }

    private boolean isFloorBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.LIME_WOOL;
    }

    private boolean isPlankBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.ORANGE_WOOL;
    }

    private boolean isLogBlock(BlockState blockState) {
        return blockState.getBlock() instanceof LogBlock;
    }

    private boolean isBrickBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.PINK_WOOL;
    }

    private boolean isRandomRawRock(BlockState blockState) {
        return blockState.getBlock() == Blocks.MAGENTA_WOOL;
    }

    private boolean isHardenedRock(BlockState blockState) {
        return blockState.getBlock() == Blocks.PURPLE_WOOL;
    }

    private boolean isHangingLampBlock(BlockState blockState) {
        return blockState.getBlock() instanceof LampBlock;
    }

    private boolean isFallenLampBlock(BlockState blockState) {
        return blockState.getBlock() == MineshaftHelpers.FALLEN_BRONZE_LAMP || blockState.getBlock() == MineshaftHelpers.FALLEN_BLACK_BRONZE_LAMP
                || blockState.getBlock() == MineshaftHelpers.FALLEN_BISMUTH_BRONZE_LAMP || blockState.getBlock() == MineshaftHelpers.FALLEN_WROUGHT_IRON_LAMP;
    }

    private boolean isTorchBlock(BlockState blockState) {
        return blockState.getBlock() instanceof DeadWallTorchBlock;
    }

    private boolean isFluidVoid(BlockState blockState) {
        return blockState.getBlock() == Blocks.CYAN_WOOL;
    }

    private boolean isTempVoid(BlockState blockState) {
        return blockState.getBlock() == Blocks.CYAN_CONCRETE;
    }

    private boolean isSpiderWeb(BlockState blockState) {
        return blockState.getBlock() == TFGBlocks_Earth.SPIDER_WEB.get();
    }

}
