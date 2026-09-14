/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This is implemented in {@link BlockEntity}s with a timer based recipe, to interact with the clocks timer mode.
 */
public interface IRecipeTimer
{
    int getRecipeDuration();

    long getRemainingTime();
}
