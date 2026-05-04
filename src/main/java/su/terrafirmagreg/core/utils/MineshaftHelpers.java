package su.terrafirmagreg.core.utils;

import java.util.*;
import java.util.stream.Collectors;

import com.therighthon.afc.common.blocks.AFCWood;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;

public class MineshaftHelpers {

    /// Cache of determined wood types by chunk
    public static final Map<ChunkPos, RegistryWood> WOOD_CHUNK_CACHE = new HashMap<>();

    /// Records if the chunk has already had a rock data cache generated
    public static final Map<ChunkPos, Boolean> ROCK_CHUNK_CACHE = new HashMap<>();

    /// Cache of determined lamp material by chunk
    public static final Map<ChunkPos, MineshaftPlaceableLamp> LAMP_CHUNK_CACHE = new HashMap<>();

    /// 1st Index = temperature
    /// 2nd Index = rainfall
    public static final RegistryWood[][] WOOD_CLIMATE_ARRAY = {
            { Wood.PINE, Wood.DOUGLAS_FIR, Wood.SPRUCE, Wood.ASPEN },
            { Wood.ASH, Wood.CHESTNUT, Wood.OAK, Wood.SEQUOIA },
            { AFCWood.BAOBAB, Wood.ACACIA, AFCWood.IPE, AFCWood.HEVEA },
            { Wood.PALM, AFCWood.IRONWOOD, Wood.KAPOK, AFCWood.TUALANG }
    };

    public static final Map<Block, Rock> ROCK_ACCESOR_MAP = new HashMap<>();

    public static final Block FALLEN_BRONZE_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_bronze_lamp"));
    public static final Block FALLEN_BLACK_BRONZE_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_black_bronze_lamp"));
    public static final Block FALLEN_BISMUTH_BRONZE_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_bismuth_bronze_lamp"));
    public static final Block FALLEN_WROUGHT_IRON_LAMP = ForgeRegistries.BLOCKS.getValue(TFGCore.id("groundcover/fallen_wrought_iron_lamp"));

    public static RegistryWood getOrAddWoodCache(ChunkPos chunkPos, BlockPos blockPos, LevelReader levelReader) {
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

    public static MineshaftPlaceableLamp getOrAddLampCache(ChunkPos chunkPos, LevelReader levelReader) {
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

            MineshaftPlaceableLamp chosenMat;

            if (randInt <= 4)
                chosenMat = MineshaftPlaceableLamp.BRONZE;
            else if (randInt <= 6)
                chosenMat = MineshaftPlaceableLamp.BLACK_BRONZE;
            else if (randInt <= 8)
                chosenMat = MineshaftPlaceableLamp.BISMUTH_BRONZE;
            else
                chosenMat = MineshaftPlaceableLamp.WROUGHT_IRON;

            LAMP_CHUNK_CACHE.put(chunkPos, chosenMat);
            return chosenMat;
        }

        //Fallback if everything fails
        return MineshaftPlaceableLamp.BRONZE;
    }

    public static RockSettings getRockType(BlockPos blockPos, LevelReader levelReader) {
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

    public static List<ChunkPos> findAdjChunks(ChunkPos chunkPos) {
        List<ChunkPos> adjChunks = new ArrayList<>();
        adjChunks.add(new ChunkPos(chunkPos.x + 1, chunkPos.z));
        adjChunks.add(new ChunkPos(chunkPos.x - 1, chunkPos.z));
        adjChunks.add(new ChunkPos(chunkPos.x, chunkPos.z + 1));
        adjChunks.add(new ChunkPos(chunkPos.x, chunkPos.z - 1));

        return adjChunks;
    }

    public static Set<BlockPos> getBoxEdges(BoundingBox outerBox, BoundingBox innerBox) {
        var outerArea = BlockPos.betweenClosedStream(outerBox);
        //System.out.println(BlockPos.betweenClosedStream(outerBox).map(BlockPos::immutable).collect(Collectors.toSet()));
        var innerArea = BlockPos.betweenClosedStream(innerBox).map(BlockPos::immutable).collect(Collectors.toSet());
        //System.out.println(innerArea);

        return outerArea.map(BlockPos::immutable).filter(pos -> !innerArea.contains(pos)).collect(Collectors.toSet());
    }

    public static Block getRuinedBrick(BlockPos blockPos, LevelReader levelReader, RandomSource random, boolean mossy) {
        Rock rock = ROCK_ACCESOR_MAP.get(getRockType(blockPos, levelReader).raw());

        int i = random.nextInt(0, 11);
        if (i <= 7) {
            return rock.getBlock(Rock.BlockType.BRICKS).get();
        } else if (i <= 9 && mossy) {
            return rock.getBlock(Rock.BlockType.MOSSY_BRICKS).get();
        } else {
            return rock.getBlock(Rock.BlockType.CRACKED_BRICKS).get();
        }
    }

    //Just easier to do the map this way
    static {
        for (Rock rock : Rock.VALUES) {
            ROCK_ACCESOR_MAP.put(TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.RAW).get(), rock);
        }
    }

}
