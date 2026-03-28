package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.mixin.accessor.ChunkAccessAccessor;
import net.dries007.tfc.world.*;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaFactory;
import net.dries007.tfc.world.noise.ChunkNoiseSamplingSettings;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.NoiseSampler;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraftforge.common.Tags;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidDefinition;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;
import su.terrafirmagreg.core.world.new_ow_wg.chunk.TFGChunkHeightFiller;
import su.terrafirmagreg.core.world.new_ow_wg.chunk.TFGChunkNoiseFiller;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoiseSampler;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverNoiseSampler;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreNoiseSampler;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.TFGSurfaceManager;

// Points the TFC chunk generator to use the new biome layers if the config option is enabled

@Mixin(value = TFCChunkGenerator.class, remap = true)
public abstract class TFCChunkGeneratorMixin implements ChunkGeneratorExtension {

    @Shadow(remap = false)
    protected abstract BiomeExtension sampleBiomeNoRiver(int blockX, int blockZ);

    @Shadow(remap = false)
    protected abstract ChunkNoiseSamplingSettings createNoiseSamplingSettingsForChunk(ChunkAccess chunk);

    @Shadow(remap = false)
    protected abstract ChunkBaseBlockSource createBaseBlockSourceForChunk(ChunkAccess chunk);

    @Shadow(remap = false)
    @Final
    private BiomeSourceExtension customBiomeSource;
    @Shadow(remap = false)
    private ChunkDataProvider chunkDataProvider;
    @Shadow(remap = false)
    @Final
    private FastConcurrentCache<TFCAquifer> aquiferCache;

    @Unique
    private Noise2D tfg$tideHeightNoise;
    @Unique
    private NoiseSampler tfg$noiseSampler;
    @Unique
    private TFGSurfaceManager tfg$surfaceManager;

    @Inject(method = "initRandomState", at = @At("HEAD"), remap = false)
    private void tfg$captureWorldSeed(ChunkMap chunkMap, ServerLevel level, CallbackInfo ci) {
        Seed.worldSeed = level.getSeed();
    }

    // overwriting the AreaFactory
    @Redirect(method = "initRandomState", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/layer/TFCLayers;createRegionBiomeLayer(Lnet/dries007/tfc/world/region/RegionGenerator;J)Lnet/dries007/tfc/world/layer/framework/AreaFactory;"), remap = false)
    private AreaFactory tfg$modifyCreateRegionBiomeLayer(RegionGenerator generator, long layerSeed, @Local(argsOnly = true) ServerLevel level) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            tfg$tideHeightNoise = TFGBiomeNoise.shoreTideLevelNoise(Seed.of(Seed.worldSeed));
            tfg$noiseSampler = new NoiseSampler(Seed.worldSeed, level.registryAccess().lookupOrThrow(Registries.NOISE), level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION));
            return TFGLayers.createRegionBiomeLayer(generator, layerSeed);
        } else {
            return TFCLayers.createRegionBiomeLayer(generator, layerSeed);
        }
    }

    // overwriting the getFromLayerId
    @ModifyArg(method = "initRandomState", index = 1, at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/layer/framework/ConcurrentArea;<init>(Lnet/dries007/tfc/world/layer/framework/AreaFactory;Ljava/util/function/IntFunction;)V"), remap = false)
    private IntFunction<BiomeExtension> tfg$modifyGetFromLayerId(IntFunction<BiomeExtension> mappingFunction) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            return TFGLayers::getFromLayerId;
        } else {
            return TFCLayers::getFromLayerId;
        }
    }

    @Inject(method = "initRandomState", at = @At("TAIL"), remap = false)
    private void tfg$initSurfaceManager(ChunkMap chunkMap, ServerLevel level, CallbackInfo ci) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            tfg$surfaceManager = new TFGSurfaceManager(Seed.worldSeed);
        }
    }

    @Inject(method = "getBaseHeight", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState state, CallbackInfoReturnable<Integer> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            final ChunkPos pos = new ChunkPos(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            cir.setReturnValue((int) tfg$createHeightFillerForChunk(pos).sampleHeight(x, z));
        }
    }

    @Inject(method = "fillFromNoise", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$fillFromNoise(Executor mainExecutor, Blender oldTerrainBlender, RandomState rawState, StructureManager structureFeatureManager, ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            // Initialization
            final ChunkNoiseSamplingSettings settings = createNoiseSamplingSettingsForChunk(chunk);
            final LevelAccessor actualLevel = (LevelAccessor) ((ChunkAccessAccessor) chunk).accessor$getLevelHeightAccessor();
            final ChunkPos chunkPos = chunk.getPos();
            final RandomSource random = new XoroshiroRandomSource(chunkPos.x * 1842639486192314L, chunkPos.z * 579238196380231L);
            final ChunkData chunkData = chunkDataProvider.get(chunk);

            // Lock sections
            final Set<LevelChunkSection> sections = new HashSet<>();
            for (LevelChunkSection section : chunk.getSections()) {
                section.acquire();
                sections.add(section);
            }

            final Object2DoubleMap<BiomeExtension>[] biomeWeights = ChunkBiomeSampler.sampleBiomes(chunkPos, this::sampleBiomeNoRiver, BiomeExtension::biomeBlendType);
            final ChunkBaseBlockSource baseBlockSource = createBaseBlockSourceForChunk(chunk);
            final TFGChunkNoiseFiller filler = new TFGChunkNoiseFiller((ProtoChunk) chunk, biomeWeights, customBiomeSource, tfg$createBiomeSamplersForChunk(chunk),
                    tfg$createRiverSamplersForChunk(), tfg$createShoreSamplersForChunk(), tfg$createVolcanoSamplersForChunk(), tfg$noiseSampler, baseBlockSource,
                    settings, SEA_LEVEL_Y, tfg$tideHeightNoise, Beardifier.forStructuresInChunk(structureFeatureManager, chunkPos));

            final Seed wSeed = Seed.of(Seed.worldSeed);
            final BiomeExtension cinderConeBiome = CenteredFeatureNoise.cinder(wSeed).getCenterBiome(chunkPos.getBlockX(8), chunkPos.getBlockZ(8), customBiomeSource);
            final BiomeExtension tuffRingBiome = CenteredFeatureNoise.tuffRing(wSeed).getCenterBiome(chunkPos.getBlockX(8), chunkPos.getBlockZ(8), customBiomeSource);
            final BiomeExtension tuyaBiome = CenteredFeatureNoise.tuya(wSeed).getCenterBiome(chunkPos.getBlockX(8), chunkPos.getBlockZ(8), customBiomeSource);

            cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
                filler.sampleAquiferSurfaceHeight(this::sampleBiomeNoRiver);
                chunkData.generateFull(filler.surfaceHeight(), filler.aquifer().surfaceHeights());
                chunkData.getRockData().useCache(chunkPos);
                filler.fillFromNoise();

                aquiferCache.set(chunkPos.x, chunkPos.z, filler.aquifer());

                return chunk;
            }, Util.backgroundExecutor()).whenCompleteAsync((ret, error) -> {
                sections.forEach(LevelChunkSection::release);

                tfg$surfaceManager.buildSurface(actualLevel, chunk, rockLayerSettings(), chunkData, filler.localBiomes(),
                        filler.localBiomesNoRivers(), filler.localBiomeWeights(), filler.createSlopeMap(), random, SEA_LEVEL_Y, settings.minY(),
                        cinderConeBiome, tuffRingBiome, tuyaBiome);
            }));
        }
    }

    @Inject(method = "buildSurface", at = @At("HEAD"), remap = true)
    private void tfg$generateHornfels(WorldGenRegion level, StructureManager structureFeatureManager, RandomState state, ChunkAccess chunk, CallbackInfo ci) {
        final ChunkPos chunkPos = chunk.getPos();
        final RandomSource random = new XoroshiroRandomSource(chunkPos.x * 2369412341L, chunkPos.z * 8192836412341L);
        final LevelChunkSection bottomSection = chunk.getSection(0);
        final RockData rockData = chunkDataProvider.get(chunk).getRockData();
        rockData.useCache(chunkPos);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 0-9 only replace stone
                for (int y = 0; y < 10; y++) {
                    if (bottomSection.getBlockState(x, y, z).is(Tags.Blocks.STONE)) {
                        bottomSection.setBlockState(x, y, z, tfg$getHornfels(rockData.getRock(chunkPos.getBlockX(x), y - 64, chunkPos.getBlockZ(z))), false);
                    }
                }
                // 10-13 only replace stone but with some noise
                for (int y = 10; y < 14; y++) {
                    if (random.nextInt(4) < 14 - y && bottomSection.getBlockState(x, y, z).is(Tags.Blocks.STONE)) {
                        bottomSection.setBlockState(x, y, z, tfg$getHornfels(rockData.getRock(chunkPos.getBlockX(x), y - 64, chunkPos.getBlockZ(z))), false);
                    }
                }
            }
        }
    }

    @Unique
    private final Block tfg$marble = TFCBlocks.ROCK_BLOCKS.get(Rock.MARBLE).get(Rock.BlockType.RAW).get();
    @Unique
    private final Block tfg$gabbro = TFCBlocks.ROCK_BLOCKS.get(Rock.GABBRO).get(Rock.BlockType.RAW).get();
    @Unique
    private final Block tfg$diorite = TFCBlocks.ROCK_BLOCKS.get(Rock.DIORITE).get(Rock.BlockType.RAW).get();

    @Unique
    private BlockState tfg$getHornfels(RockSettings rockSettings) {
        var raw = rockSettings.raw();
        if (raw == tfg$marble) {
            // marble is the only sedimentary carbonate that can spawn this deep
            return TFGBlocks_Earth.CARBONATE_HORNFELS.getDefaultState();
        } else if (raw == tfg$gabbro || raw == tfg$diorite) {
            // diorite is intermediate so it's an honorary mafic
            return TFGBlocks_Earth.MAFIC_HORNFELS.getDefaultState();
        } else {
            // this is for metamorphics, but felsics can get it too
            return TFGBlocks_Earth.PELITIC_HORNFELS.getDefaultState();
        }
    }

    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"), remap = true)
    private void tfg$outputBiomes(WorldGenLevel level, ChunkAccess chunk, StructureManager structureFeatureManager, CallbackInfo ci) {
        TFGBedrockFluidDefinition.safelyGetFluidVein(chunk, BedrockFluidVeinSavedData.getOrCreate(level.getLevel()));
    }

    @Unique
    private TFGChunkHeightFiller tfg$createHeightFillerForChunk(ChunkPos pos) {
        final Object2DoubleMap<BiomeExtension>[] biomeWeights = ChunkBiomeSampler.sampleBiomes(pos, this::sampleBiomeNoRiver, BiomeExtension::biomeBlendType);
        return new TFGChunkHeightFiller(biomeWeights, customBiomeSource, tfg$createBiomeSamplersForChunk(null), tfg$createRiverSamplersForChunk(),
                tfg$createShoreSamplersForChunk(), tfg$createVolcanoSamplersForChunk(), SEA_LEVEL_Y, tfg$tideHeightNoise);
    }

    @Unique
    private Map<TFGRiverBlendType, TFGRiverNoiseSampler> tfg$createRiverSamplersForChunk() {
        final Seed seed = Seed.of(Seed.worldSeed);
        final EnumMap<TFGRiverBlendType, TFGRiverNoiseSampler> builder = new EnumMap<>(TFGRiverBlendType.class);
        for (TFGRiverBlendType blendType : TFGRiverBlendType.ALL) {
            builder.put(blendType, blendType.createNoiseSampler(seed));
        }
        return builder;
    }

    @Unique
    private Map<ShoreBlendType, ShoreNoiseSampler> tfg$createShoreSamplersForChunk() {
        final Seed seed = Seed.of(Seed.worldSeed);
        final EnumMap<ShoreBlendType, ShoreNoiseSampler> builder = new EnumMap<>(ShoreBlendType.class);
        for (ShoreBlendType blendType : ShoreBlendType.ALL) {
            builder.put(blendType, blendType.createNoiseSampler(seed));
        }
        return builder;
    }

    @Unique
    private Map<CenteredFeatureBlendType, CenteredFeatureNoiseSampler> tfg$createVolcanoSamplersForChunk() {
        final Seed seed = Seed.of(Seed.worldSeed);
        final EnumMap<CenteredFeatureBlendType, CenteredFeatureNoiseSampler> builder = new EnumMap<>(CenteredFeatureBlendType.class);
        for (CenteredFeatureBlendType blendType : CenteredFeatureBlendType.ALL) {
            builder.put(blendType, blendType.createNoiseSampler(seed));
        }
        return builder;
    }

    @Unique
    private Map<BiomeExtension, BiomeNoiseSampler> tfg$createBiomeSamplersForChunk(@Nullable ChunkAccess chunk) {
        final ImmutableMap.Builder<BiomeExtension, BiomeNoiseSampler> builder = ImmutableMap.builder();
        for (BiomeExtension extension : TFGBiomes.getExtensions()) {
            final BiomeNoiseSampler sampler = extension.createNoiseSampler(Seed.worldSeed);
            if (sampler != null) {
                sampler.prepare(this, chunk);
                builder.put(extension, sampler);
            }
        }
        return builder.build();
    }
}
