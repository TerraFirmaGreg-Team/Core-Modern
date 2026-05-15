package su.terrafirmagreg.core.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@SuppressWarnings("removal")
public enum TierLockedProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (!accessor.getPlayer().getMainHandItem().isCorrectToolForDrops(state)) {
            tooltip.add(Component.translatable("tfg.tooltip.tier_locked_block").withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("tfg.tooltip.tier_locked_block_explosives"));
    }

    @Override
    public ResourceLocation getUid() {
        return TFGJadePlugin.TLB_Info;
    }
}
