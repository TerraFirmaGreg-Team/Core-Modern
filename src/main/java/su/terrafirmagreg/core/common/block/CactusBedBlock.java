/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.block;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.TFGTags;

public abstract class CactusBedBlock extends PlantBlock {
    public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;

    public static CactusBedBlock createBarrel(RegistryPlant plant, ExtendedProperties properties) {

        return new CactusBedBlock(properties) {
            static final VoxelShape SHAPE = box(4, 0, 4, 12, 8, 12);

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

    protected CactusBedBlock(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        mutable.setWithOffset(pos, 0, -1, 0);
        BlockState belowState = level.getBlockState(mutable);
        return Helpers.isBlock(belowState, TFGTags.Blocks.DryPlantPlantableOn);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.getType().is(TFGTags.Entities.IgnoresCacti)) {
            entity.hurt(entity.damageSources().cactus(), 1f);
        }
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    // These two methods allow placing extra per block
    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return !useContext.isSecondaryUseActive() && useContext.getItemInHand().is(this.asItem()) && state.getValue(AGE) < 3 || super.canBeReplaced(state, useContext);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (Helpers.isBlock(state, this)) {
            return state.setValue(AGE, Math.min(3, state.getValue(AGE) + 1));
        }
        return super.getStateForPlacement(context);
    }
}
