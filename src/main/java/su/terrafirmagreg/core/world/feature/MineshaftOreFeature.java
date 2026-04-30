package su.terrafirmagreg.core.world.feature;

import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;

import net.dries007.tfc.util.collections.IWeighted;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.feature.vein.*;
import net.dries007.tfc.world.noise.Metaballs2D;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;

public class MineshaftOreFeature extends Feature<MineshaftOreConfig> {
    public static final Map<String, VeinConfig> configMap = new HashMap<>();
    public static final Map<String, Integer> sizeMap = new HashMap<>();
    public static final Map<String, ConfiguredFeature<?, ?>> featureMap = new HashMap<>();

    public MineshaftOreFeature(Codec<MineshaftOreConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MineshaftOreConfig> context) {
        System.out.println("start ore place");

        if (configMap.isEmpty()) {
            System.out.println("maps are empty");
            var registry = context.level().getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
            System.out.println("got registry");

            registry.forEach(configuredFeature -> {
                var feature = configuredFeature.feature();
                var config = configuredFeature.config();

                if (feature instanceof ClusterVeinFeature clusterFeature && config instanceof ClusterVeinConfig clusterConfig) {
                    var id = registry.getKey(configuredFeature);

                    assert id != null;
                    if (id.toString().startsWith("tfg:earth/vein/")) {
                        String name = id.toString().split("tfg:earth/vein/")[1];

                        featureMap.put(name, configuredFeature);
                        sizeMap.put(name, clusterConfig.size());
                        configMap.put(name, clusterConfig.config());
                    }
                } else if (feature instanceof DiscVeinFeature discFeature && config instanceof DiscVeinConfig discConfig) {
                    var id = registry.getKey(configuredFeature);

                    assert id != null;
                    if (id.toString().startsWith("tfg:earth/vein/")) {
                        String name = id.toString().split("tfg:earth/vein/")[1];

                        featureMap.put(name, configuredFeature);
                        sizeMap.put(name, discConfig.size());
                        configMap.put(name, discConfig.config());
                    }
                }
            });
        }

        System.out.println("check veins");

        Map<String, VeinData> potentialVeins = new HashMap<>();
        int offset = 0;
        //There is a chance the feature is centered at a y where there is no veins, this fixes that
        while (potentialVeins.isEmpty()) {
            int tempOffset = offset;
            configMap.forEach((name, config) -> {
                boolean validVein = checkWorldForVein(name, config, sizeMap.get(name), context, tempOffset, potentialVeins);
            });
            offset += 30;
        }

        System.out.println(potentialVeins.keySet());

        VeinData selectedVein = pickFromWeightedVeins(potentialVeins, context.random());

        System.out.println("Selected vein: " + selectedVein.name);

        placeMetaball(context, selectedVein);
        return true;
    }

    private boolean checkWorldForVein(String name, VeinConfig veinConfig, int size, FeaturePlaceContext<MineshaftOreConfig> context, int offset, Map<String, VeinData> veinDataMap) {
        Optional<TagKey<Biome>> biomes = veinConfig.biomes();
        int minY = veinConfig.minY();
        int maxY = veinConfig.maxY();
        Map<Block, IWeighted<BlockState>> blocks = veinConfig.states();
        float density = veinConfig.density();
        int rarity = veinConfig.rarity();

        BlockPos origin = context.origin().below(offset);
        WorldGenLevel wgLevel = context.level();

        //Stops if origin is not within bounds
        if (!(origin.getY() >= minY && origin.getY() <= maxY)) {
            return false;
        }
        //System.out.println("valid y");

        //If there is a biome tag, stops if origin is not within said biome
        if (biomes.isPresent() && !wgLevel.getBiome(origin).containsTag(biomes.get())) {
            return false;
        }

        //System.out.println("valid biome");
        //Doesn't work for tuff and basalt since it uses tfc rock layers
        RockSettings originRockType = ChunkDataProvider.get(wgLevel.getLevel()).get(wgLevel.getChunk(origin)).getRockData().getRock(origin);
        //System.out.println("origin rock " + originRockType);

        Pair<Block, IWeighted<BlockState>> rockOrePair = new MutablePair<>();

        for (Block block : blocks.keySet()) {
            if (block.defaultBlockState() == originRockType.raw().defaultBlockState()) {
                rockOrePair = Pair.of(block, blocks.get(block));
            }
        }

        if (rockOrePair.getRight() == null) {
            return false;
        }

        //this has devolved fast
        veinDataMap.put(name, new VeinData(name, rarity, (int) (size * 1.5), density, originRockType, rockOrePair.getRight()));

        return true;
    }

    private VeinData pickFromWeightedVeins(Map<String, VeinData> veinDataMap, RandomSource random) {
        Map<String, Integer> weightMap = new HashMap<>();

        veinDataMap.forEach((name, veinData) -> {
            weightMap.put(name, veinData.rarity);
        });

        return veinDataMap.get(pickFromWeighted(weightMap, random));
    }

    private String pickFromWeighted(Map<String, Integer> nameWeightMap, RandomSource random) {
        int weightMax = nameWeightMap.values().stream().mapToInt(Integer::intValue).sum();
        int randomInt = random.nextInt(1, weightMax);

        int indexSum = 0;
        for (String name : nameWeightMap.keySet()) {
            int weight = nameWeightMap.get(name);
            indexSum += weight;

            if (indexSum >= randomInt) {
                return name;
            }

        }

        return nameWeightMap.keySet().stream().toList().get(0);
    }

    private BlockState pickFromWeightedBlockstate(Map<BlockState, Double> blockWeightMap, float density, RandomSource random) {
        Map<String, Integer> nameWeightMap = new HashMap<>();
        Map<String, BlockState> translationMap = new HashMap<>();

        blockWeightMap.forEach((blockstate, weight) -> {
            nameWeightMap.put(blockstate.toString(), weight.intValue());
            translationMap.put(blockstate.toString(), blockstate);
        });

        int weightMax = nameWeightMap.values().stream().mapToInt(Integer::intValue).sum();

        float airWeight = weightMax * (1 - density);

        nameWeightMap.put(Blocks.AIR.defaultBlockState().toString(), (int) airWeight);
        translationMap.put(Blocks.AIR.defaultBlockState().toString(), Blocks.AIR.defaultBlockState());

        return translationMap.get(pickFromWeighted(nameWeightMap, random));
    }

    private BoundingBox getBoundingBox(BlockPos origin, int size, int height) {
        return new BoundingBox(-size, -height / 2, -size, size, height / 2, size).moved(origin.getX(), origin.getY(), origin.getZ());
    }

    private Metaballs2D getMetaball(RandomSource random, int size) {
        return new Metaballs2D(random, 4, 6, size * 0.4, size * 0.5, size * 0.6);
    }

    private void placeMetaball(FeaturePlaceContext<MineshaftOreConfig> context, VeinData veinData) {
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        MineshaftOreConfig config = context.config();
        PoolElementStructurePiece piece;

        int size = Math.min(veinData.size, config.horizontal_range().sample(random));
        float density = veinData.density * 0.9f;
        IWeighted<BlockState> oreBlock = veinData.wightedOres;

        BoundingBox box = getBoundingBox(origin, size, config.vertical_range().sample(random));
        System.out.println(box);
        Metaballs2D metaball = getMetaball(random, size);
        System.out.println(metaball);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Block targetBlock = veinData.rockType.raw();

        var maxY = Math.min(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()), box.maxY());

        for (int x = box.minX(); x <= box.maxX(); ++x) {
            for (int z = box.minZ(); z <= box.maxZ(); ++z) {
                if (metaball.inside(x - origin.getX(), z - origin.getZ())) {
                    for (int y = box.minY(); y <= maxY; ++y) {
                        if (random.nextFloat() < density) {
                            cursor.set(x, y, z);

                            if (level.getBlockState(cursor).is(targetBlock)) {
                                BlockState placedOre = oreBlock.get(random);
                                level.setBlock(cursor, placedOre, 3);
                            }
                        }
                    }
                }
            }
        }
    }

    public record VeinData(String name, int rarity, int size, float density, RockSettings rockType, IWeighted<BlockState> wightedOres) {
    }
}
