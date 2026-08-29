package su.terrafirmagreg.core.common.blockentity;

import com.teammoeg.steampowered.SPConfig;
import com.teammoeg.steampowered.content.engine.SteamEngineTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Create;

public class TitaniumSteamEngineTileEntity extends SteamEngineTileEntity {
    public TitaniumSteamEngineTileEntity(BlockEntityType<? extends SteamEngineTileEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Block getFlywheel() {
        return TFGBlocks_Create.TITANIUM_FLYWHEEL.get();
    }

    // Can't be bothered making a bunch of new configs for these when they're nothing special

    @Override
    public float getGeneratingCapacity() {
        return SPConfig.COMMON.steelFlywheelCapacity.get() * 4;
    }

    @Override
    public float getGeneratingSpeed() {
        return SPConfig.COMMON.steelFlywheelSpeed.get();
    }

    @Override
    public int getSteamConsumptionPerTick() {
        return SPConfig.COMMON.steelFlywheelSteamConsumptionPerTick.get() * 4;
    }

    @Override
    public int getSteamStorage() {
        return SPConfig.COMMON.steelFlywheelSteamStorage.get() + 32000;
    }

    @Override
    public double getSuckEfficiency() {
        return SPConfig.COMMON.steelFlywheelSuckEfficiency.get();
    }
}
