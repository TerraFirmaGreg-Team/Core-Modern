package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;

import su.terrafirmagreg.core.utils.MarsEnvironmentalHelpers;

@Mixin(value = GTUtil.class, remap = false)
public abstract class GTUtilMixin {

    /**
     * @author FiNiTe
     * @reason makes GT solar machines understand TFC's rain mechanics <br>
     *         the EnvironmentHelpers thing I found from
     *         https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.20.x/src/main/java/net/dries007/tfc/mixin/LevelMixin.java
     */
    @Overwrite
    public static boolean canSeeSunClearly(Level world, BlockPos blockPos) {
        BlockPos bLockPosAbove = blockPos.above();
        if (!world.canSeeSky(bLockPosAbove)) {
            return false;
        } else {
            // world.isDay() sometimes gives false due to skyDarken not being < 4 sometimes during day
            // (maybe smth to do with vanilla or tfc rain logic idk)
            boolean isDay = Calendars.get(world).getCalendarDayTime() <= 12000;
            boolean isRaining = EnvironmentHelpers.isRainingOrSnowing(world, blockPos);

            // For tfc overworld: EnvironmentHelpers.isRainingOrSnowing(world,blockPos) instead of world.isRaining()
            // just incase I left it how it was before for other dimensions
            if (world.dimension() == Level.OVERWORLD || world.dimension() == Planet.VENUS) {
                return isDay && !isRaining;
            }
            // No solar power during mars sandstorms
            else if (world.dimension() == Planet.MARS) {
                final float windStrength = ClimateRenderCache.INSTANCE.getWind().length();
                return windStrength < MarsEnvironmentalHelpers.DUST_LOOSEN_SPEED && isDay;
            }
            // Other planets just use day/night only (orbits count as planets)
            else if (PlanetApi.API.isPlanet(world)) {
                return isDay;
            }
            // Any weird edge cases
            else {
                return false;
            }
        }
    }
}
