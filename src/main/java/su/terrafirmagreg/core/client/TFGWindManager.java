/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */

package su.terrafirmagreg.core.client;


import lombok.Setter;
import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;
import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGWindManager {

    private interface IWindTicker {
        static void tickWind() {}
    }

    public static class Overworld implements IWindTicker {
        /**
         * original TFC implementation of tickWind()
         * @param level as level is already declared in the mixin, this just saves an extra call
         */
        public static void tickWind(Level level) {
            if (!TFCConfig.CLIENT.enableWindParticles.get())
                return;
            final Player player = ClientHelpers.getPlayer();
            if (player != null && level != null && level.getGameTime() % 2 == 0)
            {
                final BlockPos pos = player.blockPosition();
                final Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
                final float windStrength = wind.length();
                int count = 0;
                if (windStrength > 0.3f)
                {
                    count = (int) (windStrength * 8);
                }
                else if (player.getVehicle() instanceof Boat)
                {
                    count = 2; // always show if in a boat
                }
                if (count == 0)
                    return;
                final double xBias = wind.x > 0 ? 6 : -6;
                final double zBias = wind.y > 0 ? 6 : -6;
                final ParticleOptions particle = ClimateRenderCache.INSTANCE.getTemperature() < 0f && level.getRainLevel(0) > 0 ? TFCParticles.SNOWFLAKE.get() : TFCParticles.WIND.get();
                for (int i = 0; i < count; i++)
                {
                    final double x = pos.getX() + Mth.nextDouble(level.random, -12 - xBias, 12 - xBias);
                    final double y = pos.getY() + Mth.nextDouble(level.random, -1, 6);
                    final double z = pos.getZ() + Mth.nextDouble(level.random, -12 - zBias, 12 - zBias);
                    if (level.canSeeSky(BlockPos.containing(x, y, z)))
                    {
                        level.addParticle(particle, x, y, z, 0D, 0D, 0D);
                    }
                }
            }
        }
    }

    public static class Mars implements IWindTicker {
        @Setter
        private static int particleMultiplier = 16;

        @Setter
        private static float windThreshold = 0.2f;

        @Setter
        private static int biomeChecks = 4;

        private static ParticleOptions getParticleForBiome(Holder<Biome> biome) {
            if (biome.is(TFGTags.Biomes.HasDarkSandWind)) return TFGParticles.DARK_MARS_WIND.get();
            if (biome.is(TFGTags.Biomes.HasMediumSandWind)) return TFGParticles.MEDIUM_MARS_WIND.get();
            if (biome.is(TFGTags.Biomes.HasLightSandWind)) return TFGParticles.LIGHT_MARS_WIND.get();
            return TFCParticles.WIND.get();
        }

        /**
         * Wind particle spawn behavior on Mars. Increases particle density and adds biome-specific particles.
         * @param level saves one method call
         */
        public static void tickWind(Level level) {
            final Player player = ClientHelpers.getPlayer();

            if (player != null && level != null && level.getGameTime() % 2 == 0)
            {
                final BlockPos pos = player.blockPosition();
                // wind source depends on climate model - in this case, KubeJS Mars
                final Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
                // TFC comments say not to go over 1, but afaik there's no problem with doing so. Don't fix what ain't broke
                final float windStrength = wind.length();
                int count = 0;
                if (windStrength > windThreshold)
                {
                    count = (int) (windStrength * particleMultiplier);
                }

                if (count == 0)
                    return;

                final double xBias = wind.x > 0 ? 6 : -6; // if wind blows east, add 6, else minus 6
                final double zBias = wind.y > 0 ? 6 : -6; // if wind blows north, add 6, else minus 6

                final int particlesPerCheck = (int) Math.ceil((double) count / biomeChecks);

                // total particles per tick: biomeChecks * particlesPerCheck =~ windStrength * particleMultiplier

                for (int i = 0; i < biomeChecks; i++)
                {
                    // if wind blows east, bias spawn towards west
                    final double checkX = pos.getX() + Mth.nextDouble(level.random, -12 - xBias, 12 - xBias);
                    // if wind blows north, bias spawn towards south
                    final double checkZ = pos.getZ() + Mth.nextDouble(level.random, -12 - zBias, 12 - zBias);

                    final BlockPos biomeCheckPos = new BlockPos((int) checkX, pos.getY(), (int) checkZ);
                    final Holder<Biome> biome = level.getBiome(biomeCheckPos);
                    final ParticleOptions particle = getParticleForBiome(biome);

                    for (int j = 0; j < particlesPerCheck; j++) {
                        final double x = pos.getX() + Mth.nextDouble(level.random, -12 - xBias, 12 - xBias);
                        final double y = pos.getY() + Mth.nextDouble(level.random, -1, 6);
                        final double z = pos.getZ() + Mth.nextDouble(level.random, -12 - zBias, 12 - zBias);

                        if (level.canSeeSky(BlockPos.containing(x, y, z)))
                        {
                            level.addParticle(particle, x, y, z, 0D, 0D, 0D);
                        }
                    }
                }
            }
        }
    }

}
