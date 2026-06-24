/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoiseSampler;

public class CinderConeSurfaceBuilder implements SurfaceBuilder {
    public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
        return seed -> new CinderConeSurfaceBuilder(parent.apply(seed), Seed.of(seed));
    }

    private final SurfaceBuilder parent;
    private final Seed seed;

    private final Noise2D heightNoise;

    public CinderConeSurfaceBuilder(SurfaceBuilder parent, Seed seed) {
        this.seed = seed;
        this.parent = parent;
        this.heightNoise = new OpenSimplex2D(seed.next()).octaves(2).spread(0.1f).scaled(-4, 4);
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        ISurfaceBuilderContext ctx = (ISurfaceBuilderContext) context;
        BiomeExtension cinderConeBiome = ctx.tfg$getCinderConeBiome();
        IBiomeExtension cbb = (IBiomeExtension) cinderConeBiome;
        if (cbb != null && cbb.tfg$hasCinderCones()) {
            final CenteredFeatureNoiseSampler sampler = CenteredFeatureNoise.cinder(seed);
            final float easing = sampler.calculateEasing(context.pos(), cinderConeBiome);
            if (easing > 0.6f && startY > cbb.tfg$getCenteredFeatureRockHeight() + heightNoise.noise(context.pos().getX(), context.pos().getZ())) {
                buildVolcanicSurface(context, startY, endY, easing);
                return;
            }
        }
        parent.buildSurface(context, startY, endY);
    }

    private void buildVolcanicSurface(SurfaceBuilderContext context, int startY, int endY, float easing) {
        final BlockState basalt = TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.RAW).get().defaultBlockState();

        int surfaceDepth = -1;
        for (int y = startY; y >= endY; --y) {
            BlockState stateAt = context.getBlockState(y);
            if (stateAt.isAir()) {
                // Reached air, reset surface depth
                surfaceDepth = -1;
            } else if (context.isDefaultBlock(stateAt)) {
                if (surfaceDepth == -1) {
                    // Reached surface. Place top state and switch to subsurface layers
                    surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(y, 5, 4);
                    surfaceDepth = Mth.clamp((int) (surfaceDepth * (easing - 0.6f) / 0.4f), 2, 11);
                    context.setBlockState(y, basalt);
                } else if (surfaceDepth > 0) {
                    // Subsurface layers
                    surfaceDepth--;
                    context.setBlockState(y, basalt);
                }
            }
        }
    }
}
