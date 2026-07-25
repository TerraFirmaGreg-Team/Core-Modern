package su.terrafirmagreg.core.mixins.common.steampowered;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.teammoeg.steampowered.content.flywheel.SteamFlywheelTileEntity;

import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.gtceu.material.GreatePropertyKeys;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = SteamFlywheelTileEntity.class, remap = false)
public class SteamFlywheelTileEntityMixin extends GeneratingKineticBlockEntity implements ITieredKineticBlockEntity {

    private int tier;

    public SteamFlywheelTileEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tier = ((ITieredBlock) state.getBlock()).getTier();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!tooltip.isEmpty()) {
            CreateLang.builder().space();
        } else {
            CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
        }

        float capacity = ((ITieredBlock) getBlockState().getBlock()).getMaterial().getProperty(GreatePropertyKeys.KINETIC).getMaxCapacity();

        Lang.builder("greate").translate("tooltip.capacity").style(ChatFormatting.GRAY).forGoggles(tooltip);
        Lang.builder("greate").add(CreateLang.number(capacity).style(ChatFormatting.AQUA).add(CreateLang.text("su")).space()
                .add(CreateLang.text("at current shaft tier").style(ChatFormatting.DARK_GRAY))).forGoggles(tooltip, 1);

        Lang.builder("greate").translate("tooltip.networkStatistics").style(ChatFormatting.GRAY).forGoggles(tooltip);
        CreateLang.number(stress).style(ChatFormatting.AQUA).add(CreateLang.text("su")).space().add(CreateLang.text("consumed").style(ChatFormatting.DARK_GRAY)).space()
                .add(CreateLang.text("/").style(ChatFormatting.AQUA)).space().add(CreateLang.number(capacity).style(ChatFormatting.AQUA))
                .add(CreateLang.text("su").space().add(CreateLang.text("generated").style(ChatFormatting.DARK_GRAY))).forGoggles(tooltip, 1);

        return true;
    }

    public int getTier() {
        return tier;
    }
}
