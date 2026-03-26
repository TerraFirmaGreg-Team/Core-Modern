/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.block;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.TFCTallGrassBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

import su.terrafirmagreg.core.common.data.TFGTags;

public abstract class PassableCactusBlock extends TFCTallGrassBlock {
    public static PassableCactusBlock create(RegistryPlant plant, ExtendedProperties properties) {
        return new PassableCactusBlock(properties) {
            @Override
            public RegistryPlant getPlant() {
                return plant;
            }
        };
    }

    protected PassableCactusBlock(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        if (state.getValue(PART) == Part.LOWER) {
            return Helpers.isBlock(belowState, TFGTags.Blocks.DryPlantPlantableOn);
        } else {
            if (state.getBlock() != this) {
                return Helpers.isBlock(belowState, TFGTags.Blocks.DryPlantPlantableOn);
            }
            return belowState.getBlock() == this && belowState.getValue(PART) == Part.LOWER;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.hurt(entity.damageSources().cactus(), 1f);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
