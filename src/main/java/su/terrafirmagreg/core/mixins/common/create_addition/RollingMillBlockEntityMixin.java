package su.terrafirmagreg.core.mixins.common.create_addition;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlockEntity;
import com.mrh0.createaddition.config.Config;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.GreateValues;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = RollingMillBlockEntity.class, remap = false)
public abstract class RollingMillBlockEntityMixin extends KineticBlockEntity implements ITieredKineticBlockEntity {
    @Shadow
    private RollingRecipe lastRecipe;

    public RollingMillBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lcom/mrh0/createaddition/blocks/rolling_mill/RollingMillBlockEntity;getProcessingDuration()I"))
    private int tfg$tick() {
        if (lastRecipe == null)
            return Config.ROLLING_MILL_PROCESSING_DURATION.get();

        return lastRecipe.getProcessingDuration();
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
