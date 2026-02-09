package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.mixin.accessor.ChunkAccessAccessor;
import net.dries007.tfc.world.*;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaFactory;
import net.dries007.tfc.world.noise.ChunkNoiseSamplingSettings;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.NoiseSampler;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.surface.SurfaceManager;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;
import su.terrafirmagreg.core.world.new_ow_wg.chunk.TFGChunkHeightFiller;
import su.terrafirmagreg.core.world.new_ow_wg.chunk.TFGChunkNoiseFiller;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverNoiseSampler;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreNoiseSampler;

// Points the TFC chunk generator to use the new biome layers if the config option is enabled

@Mixin(value = TFCChunkGenerator.class, remap = false)
public abstract class TFCChunkGeneratorMixin implements ChunkGeneratorExtension {

    @Shadow
    protected abstract BiomeExtension sampleBiomeNoRiver(int blockX, int blockZ);

    @Shadow
    protected abstract Map<BiomeExtension, BiomeNoiseSampler> createBiomeSamplersForChunk(@Nullable ChunkAccess chunk);

    @Shadow
    public abstract int getSeaLevel();

    @Shadow
    protected abstract ChunkNoiseSamplingSettings createNoiseSamplingSettingsForChunk(ChunkAccess chunk);

    @Shadow
    protected abstract ChunkBaseBlockSource createBaseBlockSourceForChunk(ChunkAccess chunk);

    @Shadow
    @Final
    private BiomeSourceExtension customBiomeSource;
    @Shadow
    private long noiseSamplerSeed;
    @Shadow
    private ChunkDataProvider chunkDataProvider;
    @Shadow
    @Final
    private FastConcurrentCache<TFCAquifer> aquiferCache;
    @Shadow
    private SurfaceManager surfaceManager;

    @Unique
    private Noise2D tfg$tideHeightNoise;
    @Unique
    private NoiseSampler tfg$noiseSampler;

    @Redirect(method = "initRandomState", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/layer/TFCLayers;createRegionBiomeLayer(Lnet/dries007/tfc/world/region/RegionGenerator;J)Lnet/dries007/tfc/world/layer/framework/AreaFactory;"), remap = false)
    private AreaFactory tfg$initRandomState(RegionGenerator generator, long worldSeed, @Local(argsOnly = true) ServerLevel level) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            Seed seed = Seed.of(worldSeed);
            tfg$tideHeightNoise = TFGBiomeNoise.shoreTideLevelNoise(seed);
            tfg$noiseSampler = new NoiseSampler(seed.next(), level.registryAccess().lookupOrThrow(Registries.NOISE), level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION));
            return TFGLayers.createRegionBiomeLayer(generator, worldSeed);
        } else {
            return TFCLayers.createRegionBiomeLayer(generator, worldSeed);
        }
    }

    @Inject(method = "getBaseHeight", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState state, CallbackInfoReturnable<Integer> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            final ChunkPos pos = new ChunkPos(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            cir.setReturnValue((int) tfg$createHeightFillerForChunk(pos).sampleHeight(x, z));
        }
    }

    @Inject(method = "fillFromNoise", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$fillFromNoise(Executor mainExecutor, Blender oldTerrainBlender, RandomState rawState, StructureManager structureFeatureManager, ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
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
            final TFGChunkNoiseFiller filler = new TFGChunkNoiseFiller((ProtoChunk) chunk, biomeWeights, customBiomeSource, createBiomeSamplersForChunk(chunk), tfg$createRiverSamplersForChunk(),
                    tfg$createShoreSamplersForChunk(), tfg$noiseSampler, baseBlockSource, settings, getSeaLevel(), tfg$tideHeightNoise,
                    Beardifier.forStructuresInChunk(structureFeatureManager, chunkPos));

            cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
                filler.sampleAquiferSurfaceHeight(this::sampleBiomeNoRiver);
                chunkData.generateFull(filler.surfaceHeight(), filler.aquifer().surfaceHeights());
                chunkData.getRockData().useCache(chunkPos);
                filler.fillFromNoise();

                aquiferCache.set(chunkPos.x, chunkPos.z, filler.aquifer());

                sections.forEach(LevelChunkSection::release);

                return chunk;
            }, Util.backgroundExecutor()).whenCompleteAsync((ret, error) -> {
                sections.forEach(LevelChunkSection::release);
                surfaceManager.buildSurface(actualLevel, chunk, rockLayerSettings(), chunkData, filler.localBiomes(), filler.localBiomesNoRivers(), filler.localBiomeWeights(), filler.createSlopeMap(),
                        random, getSeaLevel(), settings.minY());
            }));
        }
    }

    @Unique
    private TFGChunkHeightFiller tfg$createHeightFillerForChunk(ChunkPos pos) {
        final Object2DoubleMap<BiomeExtension>[] biomeWeights = ChunkBiomeSampler.sampleBiomes(pos, this::sampleBiomeNoRiver, BiomeExtension::biomeBlendType);
        return new TFGChunkHeightFiller(biomeWeights, customBiomeSource, createBiomeSamplersForChunk(null), tfg$createRiverSamplersForChunk(), tfg$createShoreSamplersForChunk(), getSeaLevel(),
                tfg$tideHeightNoise);
    }

    @Unique
    private Map<TFGRiverBlendType, TFGRiverNoiseSampler> tfg$createRiverSamplersForChunk() {
        final Seed seed = Seed.of(noiseSamplerSeed).forkStable();
        final EnumMap<TFGRiverBlendType, TFGRiverNoiseSampler> builder = new EnumMap<>(TFGRiverBlendType.class);
        for (TFGRiverBlendType blendType : TFGRiverBlendType.ALL) {
            builder.put(blendType, blendType.createNoiseSampler(seed));
        }
        return builder;
    }

    @Unique
    private Map<ShoreBlendType, ShoreNoiseSampler> tfg$createShoreSamplersForChunk() {
        final Seed seed = Seed.of(noiseSamplerSeed).forkStable();
        final EnumMap<ShoreBlendType, ShoreNoiseSampler> builder = new EnumMap<>(ShoreBlendType.class);
        for (ShoreBlendType blendType : ShoreBlendType.ALL) {
            builder.put(blendType, blendType.createNoiseSampler(seed));
        }
        return builder;
    }
}
