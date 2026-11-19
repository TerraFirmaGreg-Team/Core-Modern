package su.terrafirmagreg.core.compat.jade;

import com.gregtechceu.gtceu.integration.jade.provider.MachineTraitProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.tfgt.machine.trait.GasWellTrait;

public class GasWellProvider extends MachineTraitProvider<GasWellTrait, CompoundTag> {

    public static final GasWellProvider INSTANCE = new GasWellProvider();

    private GasWellProvider() {
        super(TFGCore.id("gas_well_info"), GasWellTrait.class);
    }

    @Override
    protected CompoundTag write(GasWellTrait trait) {
        var data = new CompoundTag();
        if (!trait.getGasWell().isFormed())
            return data;

        data.putBoolean("formed", true);
        data.putBoolean("active", trait.isActive());
        data.putInt("remaining", GasWellTrait.EXPLOSIVE_CONSUMPTION_INTERVAL * 20 - trait.getTimer());
        if (trait.getVeinFluid() != null)
            data.putInt("veinPercent", trait.getVeinPercent());
        return data;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
            BlockEntity blockEntity, IPluginConfig config) {
        if (!data.getBoolean("formed"))
            return;

        if (data.getBoolean("active")) {
            tooltip.add(Component.translatable("tfg.machine.gas_well.next_explosive",
                    Component.literal(data.getInt("remaining") / 20 + "s").withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable("tfg.machine.gas_well.waiting_explosive")
                    .withStyle(ChatFormatting.RED));
        }

        if (data.contains("veinPercent", Tag.TAG_INT)) {
            int percent = data.getInt("veinPercent");
            tooltip.add(Component.translatable("tfg.machine.gas_well.vein_remaining",
                    Component.literal(percent + "%").withStyle(percent > 50 ? ChatFormatting.GREEN
                            : percent > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED)));
        }
    }
}
