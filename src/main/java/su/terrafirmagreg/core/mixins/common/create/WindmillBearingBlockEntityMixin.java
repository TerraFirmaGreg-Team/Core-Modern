package su.terrafirmagreg.core.mixins.common.create;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;
import electrolyte.greate.infrastructure.config.GConfigUtility;

@Mixin(value = WindmillBearingBlockEntity.class, remap = false)
public class WindmillBearingBlockEntityMixin extends GeneratingKineticBlockEntity implements ITieredKineticBlockEntity {

    public WindmillBearingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        if (!tooltip.isEmpty()) {
            CreateLang.builder().space();
        } else {
            CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
        }

        Lang.builder("greate").translate("tooltip.capacity").style(ChatFormatting.GRAY).forGoggles(tooltip);
        Lang.builder("greate").add(CreateLang.number(GConfigUtility.getMaxCapacityFromTier(1)).style(ChatFormatting.AQUA).add(CreateLang.text("su")).space()
                .add(CreateLang.text("at current shaft tier").style(ChatFormatting.DARK_GRAY))).forGoggles(tooltip, 1);
        Lang.builder("greate").translate("tooltip.networkStatistics").style(ChatFormatting.GRAY).forGoggles(tooltip);
        CreateLang.number(stress).style(ChatFormatting.AQUA).add(CreateLang.text("su")).space().add(CreateLang.text("consumed").style(ChatFormatting.DARK_GRAY)).space()
                .add(CreateLang.text("/").style(ChatFormatting.AQUA)).space().add(CreateLang.number(capacity).style(ChatFormatting.AQUA))
                .add(CreateLang.text("su").space().add(CreateLang.text("generated").style(ChatFormatting.DARK_GRAY))).forGoggles(tooltip, 1);
        return true;
    }
}
