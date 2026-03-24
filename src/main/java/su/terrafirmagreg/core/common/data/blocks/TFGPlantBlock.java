/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.TFGTags;

public abstract class TFGPlantBlock extends PlantBlock {

    protected TFGPlantBlock(ExtendedProperties properties) {
        super(properties);
    }

    public static PlantBlock createShrub(RegistryPlant plant, ExtendedProperties properties) {
        return new PlantBlock(properties) {
            static final VoxelShape SHAPE = box(0, 0, 0, 16, 16, 16);

            @Override
            public RegistryPlant getPlant() {
                return plant;
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return SHAPE;
            }
        };
    }

    public static PlantBlock createShortShrub(RegistryPlant plant, ExtendedProperties properties) {
        return new PlantBlock(properties) {
            static final VoxelShape SHAPE = box(0, 0, 0, 16, 16, 16);

            @Override
            public RegistryPlant getPlant() {
                return plant;
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return SHAPE;
            }
        };
    }

    public static PlantBlock createPerchedEpiphyte(RegistryPlant plant, ExtendedProperties properties) {
        return new PlantBlock(properties) {
            @Override
            public RegistryPlant getPlant() {
                return plant;
            }

            @Override
            public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
                return isEpiphytePlantable(level.getBlockState(pos.below()));
            }
        };
    }

    public static boolean isEpiphytePlantable(BlockState state) {
        return Helpers.isBlock(state, TFGTags.Blocks.EpiphytePlantableOn);
    }
}
