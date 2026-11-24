/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.soil;

import java.util.List;

import com.eerussianguy.firmalife.client.FLClientHelpers;
import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;
import com.eerussianguy.firmalife.common.util.Plantable;
import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface HoeOverlayBlock
{
    void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug);
    default void addHoeOverlayFertilizer(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
        addHoeOverlayInfo(level, pos, state, text, isDebug);
        BlockEntity block_entity = level.getBlockEntity(pos);
        if (block_entity instanceof LargePlanterBlockEntity planter)
        {
            assert FLClientHelpers.getTargetedLocation() != null;
            final Vec3 trace = FLClientHelpers.getTargetedLocation().getLocation().add(-pos.getX(), -pos.getY(), -pos.getZ());
            int slot = (int) (2 * Math.round(trace.x) + 3 * Math.round(trace.z) - 4 * Math.round(trace.x) * Math.round(trace.z)); // Calculate planter's selected slot
            Plantable plantable = planter.getPlantable(slot);
            if (plantable != null) {
                text.add(Component.translatable("tfg.tooltip.crop.fertilizer").append(Component.translatable(
                        switch (plantable.getPrimaryNutrient()) {
                            case NITROGEN -> "tfg.tooltip.fertilizer.nitrogen";
                            case POTASSIUM -> "tfg.tooltip.fertilizer.potassium";
                            case PHOSPHOROUS -> "tfg.tooltip.fertilizer.phosphorus";
                        })));
            }
        }
        if (block_entity instanceof CropBlockEntity) {
            text.add(Component.translatable("tfg.tooltip.crop.fertilizer").append(Component.translatable(
                    switch (((CropBlock)state.getBlock()).getPrimaryNutrient()) {
                        case NITROGEN -> "tfg.tooltip.fertilizer.nitrogen";
                        case POTASSIUM -> "tfg.tooltip.fertilizer.potassium";
                        case PHOSPHOROUS -> "tfg.tooltip.fertilizer.phosphorus";
                    }
            )));
        }
    }
}
