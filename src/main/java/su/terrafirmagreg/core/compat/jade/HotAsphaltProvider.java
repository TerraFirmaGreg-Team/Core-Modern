package su.terrafirmagreg.core.compat.jade;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import su.terrafirmagreg.core.common.block.asphalt.HotAsphaltRoadBlock;

@SuppressWarnings("removal")
public enum HotAsphaltProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof TickCounterBlockEntity counter)) {
            return;
        }
        long ticksLeft = HotAsphaltRoadBlock.TICKS_UNTIL_SET - counter.getTicksSinceUpdate();
        if (ticksLeft > 0) {
            tooltip.add(Component.translatable("tfc.jade.time_left", Calendars.get(accessor.getLevel()).getTimeDelta(ticksLeft)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TFGJadePlugin.HOT_ASPHALT_INFO;
    }
}
