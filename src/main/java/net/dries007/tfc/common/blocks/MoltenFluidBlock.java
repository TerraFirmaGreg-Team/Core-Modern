/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class MoltenFluidBlock extends LiquidBlock
{
    @SuppressWarnings("deprecation")
    public MoltenFluidBlock(Supplier<? extends FlowingFluid> supplier, Properties properties)
    {
        super(supplier.get(), properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        if (entity.getY() < (pos.getY() + state.getFluidState().getHeight(level, pos)))
        {
            entity.lavaHurt();
        }
    }
}
