package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TuyaNoise;

public class TuyasSurfaceBuilder implements SurfaceBuilder {
    public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
        return seed -> new TuyasSurfaceBuilder(parent.apply(seed), Seed.of(seed));
    }

    private final SurfaceBuilder parent;

    private final TuyaNoise tuyaNoise;

    public TuyasSurfaceBuilder(SurfaceBuilder parent, Seed seed) {
        this.parent = parent;
        this.tuyaNoise = new TuyaNoise(seed);
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        var biome = (IBiomeExtension) context.biome();
        if (biome.tfg$hasTuyas()) {
            final float easing = tuyaNoise.calculateEasing(context.pos().getX(), context.pos().getZ(), biome.tfg$getTuyaRarity());
            if (1 - easing < 0.16f) {
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
                    surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(y, 4, 5);
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
