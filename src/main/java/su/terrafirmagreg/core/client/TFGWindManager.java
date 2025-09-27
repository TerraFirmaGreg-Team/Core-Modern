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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;

import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.utils.MarsEnvironmentalHelpers;

public class TFGWindManager {

    private interface IWindTicker {
        static void tickWind() {
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
            final ParticleOptions darkWind = TFGParticles.DARK_MARS_WIND.get();
            final ParticleOptions mediumWind = TFGParticles.MEDIUM_MARS_WIND.get();
            final ParticleOptions lightWind = TFGParticles.LIGHT_MARS_WIND.get();

            if (biome.is(TFGTags.Biomes.HasDarkSandWind))
                return darkWind;
            if (biome.is(TFGTags.Biomes.HasMediumSandWind))
                return mediumWind;
            if (biome.is(TFGTags.Biomes.HasLightSandWind))
                return lightWind;

            return TFCParticles.WIND.get();
        }

        /**
         * Wind particle spawn behavior on Mars. Increases particle density and adds biome-specific particles.
         * @param level saves one method call
         */
        public static void tickWind(Level level) {
            final Player player = ClientHelpers.getPlayer();

            if (player != null && level != null && level.getGameTime() % 2 == 0) // spawns particles every 2 ticks
            {
                final BlockPos pos = player.blockPosition();
                // wind source depends on climate model - in this case, KubeJS Mars, OR whatever the current override is
                final Vec2 wind = MarsEnvironmentalHelpers.wind_override.lengthSquared() == 0.0
                        ? ClimateRenderCache.INSTANCE.getWind()
                        : MarsEnvironmentalHelpers.wind_override;
                // TFC comments say not to go over 1, but afaik there's no problem with doing so.
                final float windStrength = wind.length();
                int count = 0;
                if (windStrength > windThreshold) {
                    count = (int) (windStrength * particleMultiplier);
                }

                if (count == 0)
                    return;

                final Vec2 offsetVec = wind.normalized().scale(-6);

                final int particlesPerCheck = (int) Math.ceil((double) count / biomeChecks);

                // total particles per tick: biomeChecks * particlesPerCheck =~ windStrength * particleMultiplier

                for (int i = 0; i < biomeChecks; i++) {
                    final Vec2 randCheckVector = TFGClientHelpers.nextVec2InRadius(level.random, 12);

                    final double checkX = pos.getX() + offsetVec.x + randCheckVector.x;
                    final double checkZ = pos.getZ() + offsetVec.y + randCheckVector.y;

                    final BlockPos biomeCheckPos = new BlockPos((int) checkX, pos.getY(), (int) checkZ);
                    final Holder<Biome> biome = level.getBiome(biomeCheckPos);
                    final ParticleOptions particle = getParticleForBiome(biome);

                    for (int j = 0; j < particlesPerCheck; j++) {
                        final Vec2 randParticleVector = TFGClientHelpers.nextVec2InRadius(level.random, 12);
                        final double x = pos.getX() + offsetVec.x + randParticleVector.x;
                        final double y = pos.getY() + Mth.nextDouble(level.random, -1, 6);
                        final double z = pos.getZ() + offsetVec.y + randParticleVector.y;

                        if (level.canSeeSky(BlockPos.containing(x, y, z))) {
                            level.addParticle(particle, x, y, z, 0D, 0D, 0D);
                        }
                    }
                }
            }
        }
    }

}
