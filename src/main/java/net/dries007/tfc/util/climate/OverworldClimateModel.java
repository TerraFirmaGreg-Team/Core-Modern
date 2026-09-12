/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.climate;

import java.util.Random;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;

/**
 * The climate model for TerraFirmaCraft's overworld. Provides a number of mechanics including:
 * - seasonal, monthly, and daily temperature variance.
 * - altitude based temperature, including both above and below ground effects.
 * - time varying precipitation types.
 */
public class OverworldClimateModel implements WorldGenClimateModel
{
    public static final float SNOW_FREEZE_TEMPERATURE = -2f;
    public static final float SNOW_MELT_TEMPERATURE = 2f;

    public static final float ICE_FREEZE_TEMPERATURE = -4f;
    public static final float ICE_MELT_TEMPERATURE = 2f;

    public static final float ICICLE_MIN_FREEZE_TEMPERATURE = -10f;
    public static final float ICICLE_MAX_FREEZE_TEMPERATURE = -2f;
    public static final float ICICLE_DRIP_TEMPERATURE = 0f;
    public static final float ICICLE_MELT_TEMPERATURE = 4f;

    public static final float LAVA_LEVEL_TEMPERATURE = 15f;

    public static final float SEA_LEVEL = TFCChunkGenerator.SEA_LEVEL_Y;
    public static final float DEPTH_LEVEL = -64;

    public static final int FOGGY_DAY_RARITY = 10;
    public static final float FOGGY_RAINFALL_MINIMUM = 150f;
    public static final float FOGGY_RAINFALL_PEAK = 300f;

    public static float getAdjustedAverageTempByElevation(BlockPos pos, ChunkData chunkData)
    {
        return getAdjustedAverageTempByElevation(pos.getY(), chunkData.getAverageTemp(pos));
    }

    public static float getAdjustedAverageTempByElevation(int y, float averageTemperature)
    {
        if (y > SEA_LEVEL)
        {
            // -1.6 C / 10 blocks above sea level
            float elevationTemperature = Mth.clamp((y - SEA_LEVEL) * 0.16225f, 0, 17.822f);
            return averageTemperature - elevationTemperature;
        }
        else
        {
            // Not a lot of trees should generate below sea level
            return averageTemperature;
        }
    }

    @Override
    public ClimateModelType type()
    {
        return ClimateModels.OVERWORLD.get();
    }

    /**
     * Obtain the climate model for the current dimension, assuming it is an {@link OverworldClimateModel}
     * This is intended for use in select world generation, which is fine with only functioning in an overworld climate model like scenario
     */
    @Nullable
    public static OverworldClimateModel getIfPresent(Object maybeLevel)
    {
        final Level unsafeLevel = Helpers.getUnsafeLevel(maybeLevel);
        if (unsafeLevel != null)
        {
            final ClimateModel model = Climate.model(unsafeLevel);
            if (model instanceof OverworldClimateModel overworldClimateModel)
            {
                return overworldClimateModel;
            }
        }
        return null;
    }

    private long climateSeed = 0;
	@Getter
    private float temperatureScale = 20_000f;

	/**
	 * Calculates the average monthly temperature for a location and given calendar month.
	 * @param ignoreHemispheres will scale the temperature by the given month factor without inverting it if it is in a Southern Hemisphere.
	 *                          For instance, with this true, passing in the factor for June will always return the factor for Early Summer, never for Early Winter
	 */
	public float getAverageMonthlyTemperature(int z, int y, float averageTemperature, float monthFactor, boolean ignoreHemispheres)
	{
		if (ignoreHemispheres && !getInNorthernHemisphere(z, temperatureScale))
		{
			monthFactor = -monthFactor;
		}
		final float monthlyTemperature = calculateMonthlyTemperature(z, monthFactor);
		return adjustTemperatureByElevation(y, averageTemperature, monthlyTemperature, 0);
	}

	/**
	 * Return true if the position is in a Northern Hemisphere, false if Southern
	 */
	public static boolean getInNorthernHemisphere(int z, float hemisphereScale)
	{
		if (hemisphereScale == 0)
		{
			return true;
		}
		final int adjustedZ = z - (int) (hemisphereScale / 2);
		final int poleToPoleDistance = (int) (hemisphereScale * 2);
		final int normalizedZ = Mth.positiveModulo(adjustedZ, (poleToPoleDistance * 2));
		return normalizedZ > poleToPoleDistance;
	}

    @Override
    public float getTemperature(@Nullable LevelReader level, BlockPos pos, ChunkData data, long calendarTicks, int daysInMonth)
    {
        // Month temperature
        final Month currentMonth = ICalendar.getMonthOfYear(calendarTicks, daysInMonth);
        final float delta = ICalendar.getFractionOfMonth(calendarTicks, daysInMonth);
        final float monthFactor = Mth.lerp(delta, currentMonth.getTemperatureModifier(), currentMonth.next().getTemperatureModifier());

        final float monthTemperature = calculateMonthlyTemperature(pos.getZ(), monthFactor);
        final float dailyTemperature = calculateDailyTemperature(calendarTicks);

        return adjustTemperatureByElevation(pos.getY(), data.getAverageTemp(pos), monthTemperature, dailyTemperature);
    }

    @Override
    public float getAverageTemperature(LevelReader level, BlockPos pos)
    {
        return ChunkData.get(level, pos).getAverageTemp(pos);
    }

    @Override
    public float getRainfall(LevelReader level, BlockPos pos)
    {
        final ChunkData data = ChunkData.get(level, pos);
        return data.getRainfall(pos);
    }

    @Override
    public float getFogginess(LevelReader level, BlockPos pos, long calendarTime)
    {
        // seed as if we're 2 hours in the future, in order to start the cycle at 4am (2 hours before sunrise)
        final long day = ICalendar.getTotalDays(calendarTime + (2 * ICalendar.TICKS_IN_HOUR));
        final Random random = seededRandom(day, 129341623413L);
        if (random.nextInt(FOGGY_DAY_RARITY) != 0)
        {
            return 0;
        }

        final float fogModifier = random.nextFloat(); // untransformed value of the fog

        final long dayTime = Calendars.get(level).getCalendarDayTime();
        float scaledTime; // a value between 0 and 1
        if (dayTime > 22000) // 4am to 6am
        {
            scaledTime = Mth.map(dayTime, 22000, 24000, 0, 1);
        }
        else if (dayTime >= 0 && dayTime < 4000) // 6am to 10am
        {
            scaledTime = 1;
        }
        else if (dayTime >= 4000 && dayTime < 6000) // 10am to 12pm
        {
            scaledTime = 1 - Mth.map(dayTime, 4000, 6000, 0, 1);
        }
        else // 12pm to 4am
        {
            scaledTime = 0;
        }

        final float rainfall = getRainfall(level, pos);
        final float rainfallModifier = Mth.clampedMap(rainfall, FOGGY_RAINFALL_MINIMUM, FOGGY_RAINFALL_PEAK, 0, 1);
        final float skylightModifier = Mth.clampedMap(level.getBrightness(LightLayer.SKY, pos), 0f, 10f, 0f, 1f);

        return Helpers.easeInOutCubic(scaledTime) * fogModifier * rainfallModifier * skylightModifier;
    }

    @Override
    public float getWaterFogginess(LevelReader level, BlockPos pos, long calendarTime)
    {
        if (Helpers.isFluid(level.getFluidState(pos), Fluids.WATER))
        {
            return Mth.clampedMap(level.getRawBrightness(pos, 0), 0f, 15f, 0.6f, 1.0f);
        }
        return 1f;
    }

    @Override
    public Vec2 getWindVector(Level level, BlockPos pos, long calendarTime)
    {
        final int y = pos.getY();
        if (y < SEA_LEVEL - 6)
            return Vec2.ZERO;
        final Random random = seededRandom(ICalendar.getTotalDays(calendarTime), 129341623413L);

        final Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(TFCTags.Biomes.HAS_PREDICTABLE_WINDS))
        {
            final boolean isDay = level.getDayTime() % 24000 < 12000;
            final int windScale = TFCConfig.SERVER.oceanWindScale.get();
            final boolean oddBand = pos.getZ() < 0 ?
                pos.getZ() % (windScale * 2) < windScale :
                pos.getZ() % (windScale * 2) > windScale;
            final float intensity = random.nextFloat() * 0.3f + 0.3f + (0.4f * level.getRainLevel(0f));
            float angle;
            if (isDay && oddBand)
                angle = Mth.PI / 4;
            else if (isDay)
                angle = 7 * Mth.PI / 4;
            else if (oddBand)
                angle = 5 * Mth.PI / 4;
            else
                angle = 3 * Mth.PI / 4;
            angle += random.nextFloat() * 0.2f - 0.1f;
            return new Vec2(Mth.cos(angle), Mth.sin(angle)).scale(intensity);
        }

        final float preventFrequentWindyDays = random.nextFloat() < 0.1f ? 1f : random.nextFloat();
        final float intensity = Math.min(0.5f * random.nextFloat() * preventFrequentWindyDays
            + 0.4f * Mth.clampedMap(y, SEA_LEVEL, SEA_LEVEL + 65, 0f, 1f)
            + 0.6f * level.getRainLevel(0f), 1f);
        final float angle = random.nextFloat() * Mth.TWO_PI;
        return new Vec2(Mth.cos(angle), Mth.sin(angle)).scale(intensity);
    }

    @Override
    public void onWorldLoad(ServerLevel level)
    {
        final ChunkGeneratorExtension extension = (ChunkGeneratorExtension) level.getChunkSource().getGenerator();

        temperatureScale = extension.settings().temperatureScale();
        climateSeed = LinearCongruentialGenerator.next(level.getSeed(), 719283741234L);
    }

    @Override
    public void onSyncToClient(FriendlyByteBuf buffer)
    {
        buffer.writeFloat(temperatureScale);
        buffer.writeLong(climateSeed);
    }

    @Override
    public void onReceiveOnClient(FriendlyByteBuf buffer)
    {
        temperatureScale = buffer.readFloat();
        climateSeed = buffer.readLong();
    }

    /**
     * Adjusts a series of temperature factors by elevation. Returns the sum temperature after adjustment.
     */
    protected float adjustTemperatureByElevation(int y, float averageTemperature, float monthTemperature, float dailyTemperature)
    {
        // Adjust temperature based on elevation
        // Above sea level, temperature lowers linearly with y.
        // Below sea level, temperature tends towards the average temperature for the area (having less influence from daily and monthly temperature)
        // Towards the bottom of the world, temperature tends towards a constant as per the existence of "lava level"
        if (y > SEA_LEVEL)
        {
			// -1.6 C / 10 blocks above sea level
			final float averageElevationTemperature = Helpers.adjustAverageTemperatureByElevation(y, averageTemperature, SEA_LEVEL);
			return averageElevationTemperature + monthTemperature + dailyTemperature;
        }
        else if (y > 0)
        {
            // The influence of daily and monthly temperature is reduced as depth increases
            float monthInfluence = Helpers.inverseLerp(y, 0, SEA_LEVEL);
            float dailyInfluence = Mth.clamp(monthInfluence * 3f - 2f, 0, 1); // Range 0 - 1, decays faster than month influence
            return averageTemperature + Mth.lerp(monthInfluence, (float) 0, monthTemperature) + Mth.lerp(dailyInfluence, (float) 0, dailyTemperature);
        }
        else
        {
            // At y = 0, there will be no influence from either month or daily temperature
            // Between this and the bottom of the world, linearly scale average temperature towards depth temperature
            float depthInfluence = Helpers.inverseLerp(y, DEPTH_LEVEL, 0);
            return Mth.lerp(depthInfluence, LAVA_LEVEL_TEMPERATURE, averageTemperature);
        }
    }

    /**
     * Calculates the monthly temperature for a given latitude and month modifier
     */
    protected float calculateMonthlyTemperature(int z, float monthTemperatureModifier)
    {
		return monthTemperatureModifier * (temperatureScale == 0 ? 0 : Helpers.triangle(-18f, 0f, 1f / (4f * temperatureScale), z - temperatureScale / 2));
    }

    /**
     * Calculates the daily variation temperature at a given time.
     * Influenced by both random variation day by day, and the time of day.
     * Range: -3.9 - 3.9
     */
    protected float calculateDailyTemperature(long calendarTime)
    {
        // Hottest part of the day at 12, coldest at 0
        int hourOfDay = ICalendar.getHourOfDay(calendarTime);
        if (hourOfDay > 12)
        {
            // Range: 0 - 12
            hourOfDay = 24 - hourOfDay;
        }
        // Range: -1 - 1
        float hourModifier = (hourOfDay / 6f) - 1f;

        // Note: this does not use world seed, as that is not synced from server - client, resulting in the seed being different
        long day = ICalendar.getTotalDays(calendarTime);
        final Random random = seededRandom(day, 1986239412341L);
        return ((random.nextFloat() - random.nextFloat()) + 0.3f * hourModifier) * 3f;
    }

    protected Random seededRandom(long day, long salt)
    {
        long seed = LinearCongruentialGenerator.next(climateSeed, day);
        seed = LinearCongruentialGenerator.next(seed, salt);
        return new Random(seed);
    }
}
