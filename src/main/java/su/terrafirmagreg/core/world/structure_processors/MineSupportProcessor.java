package su.terrafirmagreg.core.world.structure_processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.therighthon.afc.common.blocks.AFCWood;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.LampBlock;
import net.dries007.tfc.common.blocks.wood.HorizontalSupportBlock;
import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.TFGStructureProcessors;

public class MineSupportProcessor extends StructureProcessor {
    /// 1st Index = temperature
    /// 2nd Index = rainfall
    public static final RegistryWood[][] WOOD_CLIMATE_ARRAY = {
            { Wood.PINE, Wood.DOUGLAS_FIR, Wood.SPRUCE, Wood.ASPEN },
            { Wood.ASH, Wood.CHESTNUT, Wood.OAK, Wood.SEQUOIA },
            { AFCWood.BAOBAB, Wood.ACACIA, AFCWood.IPE, AFCWood.HEVEA },
            { Wood.PALM, AFCWood.IRONWOOD, Wood.KAPOK, AFCWood.TUALANG }
    };

    public static final Map<ChunkPos, RegistryWood> WOOD_CHUNK_CACHE = new HashMap<>();

    /// Records if the chunk has already had a rock data cache generated
    public static final Map<ChunkPos, Boolean> ROCK_CHUNK_CACHE = new HashMap<>();

    public static final Map<ChunkPos, PlacableLamp> LAMP_CHUNK_CACHE = new HashMap<>();

    public static final MineSupportProcessor INSTANCE = new MineSupportProcessor();
    public static final Codec<MineSupportProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private static final Block BRONZE_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_bronze_lamp"));
    private static final Block BLACK_BRONZE_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_black_bronze_lamp"));
    private static final Block BISMUTH_BRONZE_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_bismuth_bronze_lamp"));
    private static final Block WROUGHT_IRON_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_wrought_iron_lamp"));

    private RegistryWood getOrAddWoodCache(ChunkPos chunkPos, BlockPos blockPos, LevelReader levelReader) {
        //Checks if this chunk has already been cached
        if (WOOD_CHUNK_CACHE.containsKey(chunkPos)) {
            return WOOD_CHUNK_CACHE.get(chunkPos);
        }

        //Checks if adjacent chunks have been cached, and use cached value
        List<ChunkPos> adjChunks = findAdjChunks(chunkPos);
        for (var adjChunk : adjChunks) {
            if (WOOD_CHUNK_CACHE.containsKey(adjChunk)) {
                var woodType = WOOD_CHUNK_CACHE.get(adjChunk);
                WOOD_CHUNK_CACHE.put(chunkPos, woodType);

                return woodType;
            }
        }

        //Find wood if no nearby chunks have been cached
        if (levelReader instanceof ServerLevelAccessor levelAccessor) {

            ChunkDataProvider dataProv = ChunkDataProvider.get(levelAccessor.getLevel().getChunkSource().getGenerator());
            ChunkData data = dataProv.get(levelAccessor.getChunk(chunkPos.x, chunkPos.z));
            System.out.println(chunkPos);

            System.out.println(data.status());

            BlockPos testPos = chunkPos.getMiddleBlockPosition(blockPos.getY());
            var temp = data.getAverageTemp(testPos);
            var rain = data.getRainfall(testPos);

            System.out.println(temp);
            System.out.println(rain);

            int tempQuart = (int) Math.floor((temp + 20) / 15);
            int rainQuart = (int) Math.floor(rain / 125);

            System.out.println(tempQuart);
            System.out.println(rainQuart);
            RegistryWood woodType = WOOD_CLIMATE_ARRAY[tempQuart][rainQuart];

            WOOD_CHUNK_CACHE.put(chunkPos, woodType);
            System.out.println("added " + woodType + " at " + chunkPos);
            return woodType;
        }

        //Fallback if everything breaks
        return Wood.OAK;
    }

    private PlacableLamp getOrAddLampCache(ChunkPos chunkPos, LevelReader levelReader) {
        //Checks if this chunk has already been cached
        if (LAMP_CHUNK_CACHE.containsKey(chunkPos)) {
            return LAMP_CHUNK_CACHE.get(chunkPos);
        }

        //Checks if adjacent chunks have been cached, and use cached value
        List<ChunkPos> adjChunks = findAdjChunks(chunkPos);
        for (var adjChunk : adjChunks) {
            if (LAMP_CHUNK_CACHE.containsKey(adjChunk)) {
                var lamp = LAMP_CHUNK_CACHE.get(adjChunk);
                LAMP_CHUNK_CACHE.put(chunkPos, lamp);

                return lamp;
            }
        }

        if (levelReader instanceof ServerLevelAccessor levelAccessor) {
            //40% chance for bronze. 20% chance for black bronze. 20% chance for bismuth bronze. 20% chance for wrought iron
            int randInt = levelAccessor.getRandom().nextInt(1, 10);

            PlacableLamp chosenMat;

            if (randInt <= 4)
                chosenMat = PlacableLamp.BRONZE;
            else if (randInt <= 6)
                chosenMat = PlacableLamp.BLACK_BRONZE;
            else if (randInt <= 8)
                chosenMat = PlacableLamp.BISMUTH_BRONZE;
            else
                chosenMat = PlacableLamp.WROUGHT_IRON;

            LAMP_CHUNK_CACHE.put(chunkPos, chosenMat);
            return chosenMat;
        }

        //Fallback if everything fails
        return PlacableLamp.BRONZE;
    }

    private List<ChunkPos> findAdjChunks(ChunkPos chunkPos) {
        List<ChunkPos> adjChunks = new ArrayList<>();
        adjChunks.add(new ChunkPos(chunkPos.x + 1, chunkPos.z));
        adjChunks.add(new ChunkPos(chunkPos.x - 1, chunkPos.z));
        adjChunks.add(new ChunkPos(chunkPos.x, chunkPos.z + 1));
        adjChunks.add(new ChunkPos(chunkPos.x, chunkPos.z - 1));

        return adjChunks;
    }

    private RockSettings getRockType(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader instanceof ServerLevelAccessor levelAccessor) {
            ChunkPos chunkPos = levelAccessor.getChunk(blockPos).getPos();
            ChunkDataProvider dataProv = ChunkDataProvider.get(levelAccessor.getLevel().getChunkSource().getGenerator());
            RockData data = dataProv.get(levelAccessor.getChunk(chunkPos.x, chunkPos.z)).getRockData();

            if (!ROCK_CHUNK_CACHE.containsKey(chunkPos)) {
                data.useCache(chunkPos);
                ROCK_CHUNK_CACHE.put(chunkPos, true);
            }

            return data.getRock(blockPos);
        }

        //This won't ever exist
        return null;
    }

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

        //Quick exit to help performance
        if(originalBlockState.isAir()){
            return currentBlockInfo;
        }

        //Fills in air gaps in the floor with regions wood planks
        if (isFloorBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                BlockState levelBlockState = levelAccessor.getBlockState(blockPos);

                if (levelBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.UP)) {
                    return new StructureTemplate.StructureBlockInfo(blockPos, levelBlockState, currentBlockInfo.nbt());
                }

                var woodType = getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

                return new StructureTemplate.StructureBlockInfo(blockPos, woodType.getBlock(Wood.BlockType.PLANKS).get().defaultBlockState(), currentBlockInfo.nbt());
            }
        }

        //Changes supports to regions wood type
        if (isSupportBlock(originalBlockState)) {
            var woodType = getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

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
                Block newBlock = random.nextBoolean() ? levelAccessor.getBlockState(blockPos).getBlock() : Blocks.AIR;

                return new StructureTemplate.StructureBlockInfo(blockPos, newBlock.defaultBlockState(), currentBlockInfo.nbt());
            }
        }

        //Changes wood planks to regions wood type
        if (isPlankBlock(originalBlockState)) {
            var woodType = getOrAddWoodCache(levelReader.getChunk(blockPos).getPos(), blockPos, levelReader);

            Block newBlock = woodType.getBlock(Wood.BlockType.PLANKS).get();

            return new StructureTemplate.StructureBlockInfo(blockPos, newBlock.defaultBlockState(), currentBlockInfo.nbt());
        }

        //Adds hardened stone to unstable roofs
        if (isHardenedRock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                BlockState levelBlockState = levelAccessor.getBlockState(blockPos);

                if (levelBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.DOWN)) {
                    var hardRock = getRockType(blockPos, levelReader).hardened().defaultBlockState();
                    return new StructureTemplate.StructureBlockInfo(blockPos, hardRock, currentBlockInfo.nbt());
                }

                return new StructureTemplate.StructureBlockInfo(blockPos, levelBlockState, currentBlockInfo.nbt());
            }
        }

        //Adds fuel to hanging lamps, changes material, and checks for block above
        if (isHangingLampBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                RandomSource random = levelAccessor.getRandom();

                BlockState aboveBlockState = levelAccessor.getBlockState(blockPos.above());

                if (aboveBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.DOWN)) {
                    CompoundTag lampTag = currentBlockInfo.nbt();
                    assert lampTag != null;
                    var tankTag = lampTag.getCompound("tank");

                    tankTag.putString("FluidName", "gtceu:seed_oil");
                    tankTag.putInt("Amount", random.nextInt(0, 80));
                    lampTag.put("tank", tankTag);

                    BlockState newLamp = getOrAddLampCache(levelAccessor.getChunk(blockPos).getPos(), levelReader).hangingLamp.withPropertiesOf(originalBlockState);

                    return new StructureTemplate.StructureBlockInfo(blockPos, newLamp, lampTag);
                }

                return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
            }
        }

        if (isFallenLampBlock(originalBlockState)) {
            if (levelReader instanceof ServerLevelAccessor levelAccessor) {
                BlockState belowBlockState = levelAccessor.getBlockState(blockPos.below());

                if (belowBlockState.isFaceSturdy(levelAccessor, blockPos, Direction.UP)) {
                    BlockState newLamp = getOrAddLampCache(levelAccessor.getChunk(blockPos).getPos(), levelReader).fallenLamp.withPropertiesOf(originalBlockState);

                    return new StructureTemplate.StructureBlockInfo(blockPos, newLamp, currentBlockInfo.nbt());
                }

                return new StructureTemplate.StructureBlockInfo(blockPos, Blocks.CAVE_AIR.defaultBlockState(), new CompoundTag());
            }
        }

        //Fallback if block doesn't need to be changed
        return currentBlockInfo;
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
        return blockState.getBlock() == BRONZE_LAMP || blockState.getBlock() == BLACK_BRONZE_LAMP || blockState.getBlock() == BISMUTH_BRONZE_LAMP || blockState.getBlock() == WROUGHT_IRON_LAMP;
    }

    @Getter
    public enum PlacableLamp {
        BRONZE(TFCBlocks.METALS.get(Metal.Default.BRONZE).get(Metal.BlockType.LAMP).get(), BRONZE_LAMP),
        BLACK_BRONZE(TFCBlocks.METALS.get(Metal.Default.BLACK_BRONZE).get(Metal.BlockType.LAMP).get(), BLACK_BRONZE_LAMP),
        BISMUTH_BRONZE(TFCBlocks.METALS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.BlockType.LAMP).get(), BISMUTH_BRONZE_LAMP),
        WROUGHT_IRON(TFCBlocks.METALS.get(Metal.Default.WROUGHT_IRON).get(Metal.BlockType.LAMP).get(), WROUGHT_IRON_LAMP);

        private final Block hangingLamp;
        private final Block fallenLamp;

        PlacableLamp(Block hangingLamp, Block fallenLamp) {
            this.hangingLamp = hangingLamp;
            this.fallenLamp = fallenLamp;
        }
    }

}
