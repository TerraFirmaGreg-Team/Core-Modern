package su.terrafirmagreg.core.common.block.create;

import java.util.List;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.teammoeg.steampowered.SPConfig;
import com.teammoeg.steampowered.client.ClientUtils;
import com.teammoeg.steampowered.content.engine.SteamEngineBlock;

import net.createmod.catnip.lang.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;

import su.terrafirmagreg.core.common.blockentity.TitaniumSteamEngineTileEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class TitaniumSteamEngineBlock extends SteamEngineBlock implements IBE<TitaniumSteamEngineTileEntity> {

    public TitaniumSteamEngineBlock(Properties builder) {
        super(builder);
    }

    @Override
    public Class<TitaniumSteamEngineTileEntity> getBlockEntityClass() {
        return TitaniumSteamEngineTileEntity.class;
    }

    @Override
    public BlockEntityType<? extends TitaniumSteamEngineTileEntity> getBlockEntityType() {
        return TFGBlockEntities.TITANIUM_STEAM_ENGINE.get();
    }

    @Override
    public void appendHoverText(ItemStack i, BlockGetter w, List<Component> t, TooltipFlag f) {
        if (Screen.hasShiftDown()) {
            t.add(Component.translatable("tooltip.steampowered.engine.brief").withStyle(ChatFormatting.GOLD));
            if (ClientUtils.hasGoggles())
                t.add(Component.translatable("tooltip.steampowered.engine.steamconsume", SPConfig.COMMON.steelFlywheelSteamConsumptionPerTick.get() * 4).withStyle(ChatFormatting.GOLD));
        } else {
            t.add(TooltipHelper.holdShift(FontHelper.Palette.GRAY, false));
        }
        super.appendHoverText(i, w, t, f);
    }
}
