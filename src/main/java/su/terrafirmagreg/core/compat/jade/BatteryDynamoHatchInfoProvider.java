package su.terrafirmagreg.core.compat.jade;

import static com.gregtechceu.gtceu.utils.GTUtil.formatLongNumber;
import static com.gregtechceu.gtceu.utils.GTUtil.getStringRemainTime;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.jade.provider.MachineInfoProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.BatteryDynamoHatchPartMachine;

/**
 * Copy BatteryStorageInfoProvider
 */
public class BatteryDynamoHatchInfoProvider
        extends MachineInfoProvider<@NotNull BatteryDynamoHatchPartMachine, @NotNull CompoundTag> {

    public BatteryDynamoHatchInfoProvider() {
        super(TFGCore.id("battery_dynamo_hatch_info"), BatteryDynamoHatchPartMachine.class);
    }

    @Override
    protected CompoundTag write(BatteryDynamoHatchPartMachine machine) {
        CompoundTag tag = new CompoundTag();
        tag.put("energy", getEnergyData(machine));
        tag.put("storage", machine.getBatteryInventory().serializeNBT());
        return tag;
    }

    private CompoundTag getEnergyData(BatteryDynamoHatchPartMachine machine) {
        var info = machine.getEnergyInfo();
        CompoundTag tag = new CompoundTag();
        tag.putLong("changed", machine.getInputPerSec() - machine.getOutputPerSec());
        tag.putLong("capacity", info.capacity().longValue());
        tag.putLong("stored", info.stored().longValue());
        return tag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
            BlockEntity blockEntity, IPluginConfig config) {
        CompoundTag container = data.getCompound("energy");
        long changed = container.getLong("changed"), stored = container.getLong("stored"),
                capacity = container.getLong("capacity");
        tooltip.add(Component.translatable("gtceu.jade.changes_eu_tick",
                FormattingUtil.formatNumbers(((double) changed) / 20.0)));
        if (changed > 0L) {
            tooltip.add(Component.translatable("gtceu.jade.remaining_charge_time",
                    getStringRemainTime((capacity - stored) / changed)));
        } else if (changed < 0L) {
            tooltip.add(Component.translatable("gtceu.jade.remaining_discharge_time",
                    getStringRemainTime((stored) / -changed)));
        }
        if (GTUtil.isShiftDown()) {
            CustomItemStackHandler handler = new CustomItemStackHandler();
            handler.deserializeNBT(data.getCompound("storage"));
            IElementHelper helper = tooltip.getElementHelper();
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).getCount() != 0) {
                    ItemStack stack = handler.getStackInSlot(i);
                    tooltip.add(helper.smallItem(stack));
                    IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
                    if (item == null)
                        continue;
                    tooltip.append(Component.literal(
                            GTValues.VNF[item.getTier()] + "§r " + formatLongNumber(item.getCharge()) +
                                    " / " + formatLongNumber(item.getMaxCharge()) + " EU"));
                }
            }
        }
    }
}
