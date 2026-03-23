/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.ITallPlant;
import net.dries007.tfc.common.blocks.plant.TFCTallGrassBlock;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TallShrubBlock extends TFCTallGrassBlock implements ITallPlant {
    protected static final EnumProperty<Part> PART = TFCBlockStateProperties.TALL_PLANT_PART;
    protected static final VoxelShape PLANT_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public static TallShrubBlock create(RegistryPlant plant, ExtendedProperties properties) {
        return new TallShrubBlock(properties) {
            @Override
            public RegistryPlant getPlant() {
                return plant;
            }
        };
    }

    protected TallShrubBlock(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLANT_SHAPE;
    }
}
