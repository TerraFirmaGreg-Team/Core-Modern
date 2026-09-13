/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;

import static net.dries007.tfc.world.layer.TFCLayers.*;

public enum RegionEdgeBiomeLayer implements AdjacentTransformLayer
{
    INSTANCE;

	@Override
	public int apply(AreaContext context, int north, int east, int south, int west, int center) {
		final Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);

		// >= 2 Adjacent border conditions
		if (TFCLayers.isLow(center)) {
			if (matcher.test(TFCLayers::isOcean) && matcher.test(TFCLayers::isMountains)) {
				return TFCLayers.OCEANIC_MOUNTAINS;
			} else if (matcher.test(TFCLayers::isOcean) && matcher.test(i -> i == TFCLayers.LOWLANDS)) {
				return TFCLayers.SALT_MARSH;
			}
		}

		// No mud/salt flats near oceans
		if (TFCLayers.isFlats(center)) {
			if (matcher.test(TFCLayers::isOcean) && matcher.test(TFCLayers::isFlats)) {
				return TFCLayers.CANYONS;
			}
		}

		if (center == TFCLayers.PLATEAU || center == TFCLayers.BADLANDS) {
			if (matcher.test(i -> i == TFCLayers.LOW_CANYONS || i == TFCLayers.LOWLANDS)) {
				return TFCLayers.HILLS;
			} else if (matcher.test(i -> i == TFCLayers.PLAINS || i == TFCLayers.HILLS)) {
				return TFCLayers.ROLLING_HILLS;
			}
		} else if (TFCLayers.isMountains(center)) {
			if (matcher.test(TFCLayers::isLow)) {
				return TFCLayers.ROLLING_HILLS;
			}
		}
		// Inverses of above conditions
		else if (center == TFCLayers.LOWLANDS || center == TFCLayers.LOW_CANYONS) {
			if (matcher.test(i -> i == TFCLayers.PLATEAU || i == TFCLayers.BADLANDS)) {
				return TFCLayers.HILLS;
			} else if (matcher.test(TFCLayers::isMountains)) {
				return TFCLayers.ROLLING_HILLS;
			}
		} else if (center == TFCLayers.PLAINS || center == TFCLayers.HILLS) {
			if (matcher.test(i -> i == TFCLayers.PLATEAU || i == TFCLayers.BADLANDS)) {
				return TFCLayers.HILLS;
			} else if (matcher.test(TFCLayers::isMountains)) {
				return TFCLayers.ROLLING_HILLS;
			}
		} else if (center == TFCLayers.DEEP_OCEAN_TRENCH) {
			if (matcher.test(i -> !TFCLayers.isOcean(i))) {
				return TFCLayers.OCEAN;
			}
		}
		return center;
	}
}