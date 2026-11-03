package su.terrafirmagreg.core.compat.create.display_source;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.PowerUnits;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.cells.BasicCellInventory;

@SuppressWarnings("NoTranslation")
public class MEControllerDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        Level level = context.level();
        BlockPos pos = context.getSourcePos();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ControllerBlockEntity controller)) {
            return ZERO.copy();
        }

        IGrid grid = controller.getMainNode().getGrid();
        if (grid == null) {
            return ZERO.copy();
        }

        int mode = context.sourceConfig().getInt("Mode");
        return switch (mode) {
            case 0 -> {
                // Exposed Channels Used/ Max
                int usedChannels = grid.getPathingService().getUsedChannels();
                ChannelMode channelMode = grid.getPathingService().getChannelMode();
                if (channelMode == ChannelMode.INFINITE) {
                    yield Component.literal(usedChannels + " / ∞");
                }
                ControllerState controllerState = grid.getPathingService().getControllerState();
                if (controllerState == ControllerState.NO_CONTROLLER) {
                    yield Component.literal(usedChannels + " / " + channelMode.getAdHocNetworkChannels());
                }
                if (controllerState == ControllerState.CONTROLLER_CONFLICT) {
                    yield Component.literal(usedChannels + " / 0");
                }

                int exposedFaces = 0;
                var controllers = getControllers(grid);
                for (ControllerBlockEntity c : controllers) {
                    BlockPos cPos = c.getBlockPos();
                    Level cLevel = c.getLevel();
                    if (cLevel == null) {
                        continue;
                    }
                    for (Direction dir : Direction.values()) {
                        BlockPos neighborPos = cPos.relative(dir);
                        if (!(cLevel.getBlockEntity(neighborPos) instanceof ControllerBlockEntity)) {
                            exposedFaces++;
                        }
                    }
                }
                int maxChannels = exposedFaces * 32 * channelMode.getCableCapacityFactor();
                yield Component.translatable("tfg.tooltip.me_network.channels", usedChannels + " / " + maxChannels);
            }
            case 1 -> {
                // Channel Passive Energy Drain (EU/t)
                double channelPowerAE = grid.getEnergyService().getChannelPowerUsage();
                double channelPowerEU = PowerUnits.AE.convertTo(PowerUnits.EU, channelPowerAE);
                yield Component.translatable("gtceu.recipe.eu.total", String.format("%.2f", channelPowerEU));
            }
            case 2 -> {
                // Energy Usage (EU/t)
                double avgPowerAE = grid.getEnergyService().getAvgPowerUsage();
                double avgPowerEU = PowerUnits.AE.convertTo(PowerUnits.EU, avgPowerAE);
                yield Component.translatable("gtceu.recipe.eu.total", String.format("%.2f", avgPowerEU));
            }
            case 3 -> {
                // Total Storage Types Used/ Max
                StorageStats storageStats = getStorageStats(grid);
                yield Component.literal(storageStats.usedTypes() + " / " + storageStats.maxTypes() + " ").append(Component.translatable("gui.ae2.Types"));
            }
            case 4 -> {
                // Total Storage Bytes Used/ Max
                StorageStats storageStats = getStorageStats(grid);
                yield Component.translatable("gui.ae2.BytesUsed", storageStats.usedBytes() + " / " + storageStats.maxBytes());
            }
            default -> {
                // Total storage Used %
                StorageStats storageStats = getStorageStats(grid);
                double usedPercent = storageStats.maxBytes() > 0 ? ((double) storageStats.usedBytes() / (double) storageStats.maxBytes()) * 100.0 : 0.0;
                yield Component.literal(String.format("%.3f%%", usedPercent));
            }
        };
    }

    private Set<ControllerBlockEntity> getControllers(IGrid grid) {
        Set<ControllerBlockEntity> controllers = new HashSet<>();
        for (IGridNode node : grid.getNodes()) {
            if (node.getOwner() instanceof ControllerBlockEntity controller) {
                controllers.add(controller);
            }
        }
        return controllers;
    }

    private Set<IChestOrDrive> getDrivesAndChests(IGrid grid) {
        Set<IChestOrDrive> drives = new HashSet<>();
        for (IGridNode node : grid.getNodes()) {
            if (node.getOwner() instanceof IChestOrDrive drive) {
                drives.add(drive);
            }
        }
        return drives;
    }

    private record StorageStats(long usedTypes, long maxTypes, long usedBytes, long maxBytes) {
    }

    private StorageStats getStorageStats(IGrid grid) {
        long usedTypes = 0;
        long maxTypes = 0;
        long usedBytes = 0;
        long maxBytes = 0;

        for (IChestOrDrive machine : getDrivesAndChests(grid)) {
            for (int i = 0; i < machine.getCellCount(); i++) {
                StorageCell cell = machine.getOriginalCellInventory(i);
                if (cell instanceof BasicCellInventory basicCell) {
                    usedTypes += basicCell.getStoredItemTypes();
                    maxTypes += basicCell.getTotalItemTypes();
                    usedBytes += basicCell.getUsedBytes();
                    maxBytes += basicCell.getTotalBytes();
                }
            }
        }
        return new StorageStats(usedTypes, maxTypes, usedBytes, maxBytes);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "me_network";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (!isFirstLine) {
            builder.addSelectionScrollInput(0, 120, (si, l) -> si
                    .forOptions(CreateLang.translatedOptions("display_source.me_network",
                            "channels_used_max",
                            "channel_passive_drain",
                            "energy_usage",
                            "storage_types",
                            "storage_bytes",
                            "storage_used_percent"))
                    .titled(Component.translatable("tfg.display_source.me_controller.display_mode")), "Mode");
        }
    }
}
