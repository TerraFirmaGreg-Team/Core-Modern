package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import java.util.List;

import com.simibubi.create.foundation.utility.CreateLang;
import electrolyte.greate.GreateValues;
import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.negodya1.vintageimprovements.content.kinetics.vibration.VibratingTableBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = VibratingTableBlockEntity.class, remap = false)
public abstract class VibratingTableBlockEntityMixin extends KineticBlockEntity implements ITieredKineticBlockEntity {
    public VibratingTableBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);

		addStressImpactStats(tooltip, calculateStressApplied());

		Lang.builder("greate")
			.add(CreateLang.number(Mth.abs(getSpeed())).style(ChatFormatting.AQUA))
			.space()
			.add(CreateLang.text("/").style(ChatFormatting.DARK_GRAY))
			.space()
			.add(CreateLang.number(GreateValues.getMaxSpeedFromMaterial(GTMaterials.Steel)).style(ChatFormatting.AQUA))
			.space()
			.add(CreateLang.translate("create.generic.unit.rpm"))
			.space()
			.add(CreateLang.translate("greate.tooltip.current_shaft_tier").style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);

		return true;
    }
}
