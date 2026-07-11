package su.terrafirmagreg.core.client;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.mixins.client.tfc.TFCColorsAccessor;

public class PalmColorProvider {

    /**
     * Gets the foliage color for palm trees based on their lifecycle and climate range.
     *
     * @param tree The palm tree type.
     * @param pos The leaf block position. Can be null for items.
     * @param tintIndex The tint index (only 0 is handled).
     * @return The calculated color, or -1 if invalid.
     */
    public static int getPalmFoliageColor(PalmTrees tree, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0) {
            return -1;
        }

        Level world = ClientHelpers.getLevel();
        float temperature;
        float rainfall;
        if (world != null && pos != null) {
            temperature = Climate.getAverageTemperature(world, pos);
            rainfall = Climate.getRainfall(world, pos);
        } else {
            temperature = 12.0F;
            rainfall = 250.0F;
        }

        int autumnIndex = tree.getFoliageColorIndex();
        ClimateRange range = tree.getClimateRange().get();

        int hydration = (int) (rainfall / 5);
        boolean inRange = range.checkBoth(hydration, temperature, false);

        if (inRange) {
            Month currentMonth = Calendars.CLIENT.getCalendarMonthOfYear();
            Lifecycle lifecycle = tree.getStages()[currentMonth.ordinal()];
            if (lifecycle == Lifecycle.DORMANT) {
                autumnIndex += 50;
                temperature += 50;
            }
        } else {
            autumnIndex += 100;
            temperature += 100;
        }

        return getPalmFoliageColorFromIndex(pos, temperature, Mth.clamp(autumnIndex, 0, 255));
    }

    /**
     * Gets the foliage color for palm trees based on the autumn index and temperature.
     * This allows coloring based on lifecycle and climate range instead of the typical seasons.
     *
     * @param pos The leaf block position. Can be null.
     * @param temperature The average temperature at the position.
     * @param autumnIndex The vertical index in the fall colormap (0-255).
     * @return The color from the fall colormap.
     */
    public static int getPalmFoliageColorFromIndex(@Nullable BlockPos pos, float temperature, int autumnIndex) {
        int[] colorCache = TFCColorsAccessor.getFoliageColorsCache();

        // Temperature mapping for foliage (x-axis of png).
        int temperatureIndex = 255 - Mth.clamp((int) ((temperature + 20.0F) * 255.0F / 50.0F), 0, 255);

        // Local hash to add randomness.
        int positionDeltaHash = 0;
        if (pos != null) {
            positionDeltaHash = (Helpers.hash(836494186029734123L, pos) & 7) - 3;
        }

        int autumnProgressIndex = Mth.clamp(temperatureIndex + positionDeltaHash, 0, 255);

        return colorCache[autumnProgressIndex | (autumnIndex & 0xFF) << 8];
    }
}
