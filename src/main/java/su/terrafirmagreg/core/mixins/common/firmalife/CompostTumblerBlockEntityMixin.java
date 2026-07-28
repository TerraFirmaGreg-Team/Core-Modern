package su.terrafirmagreg.core.mixins.common.firmalife;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.CompostTumblerBlockEntity;
import com.eerussianguy.firmalife.common.blocks.CompostTumblerBlock;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.Lang;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.utils.CreateKineticsHelper;

/**
 * Mixin into {@link CompostTumblerBlockEntity} to add render handling for Create rotation support.
 */
@Mixin(value = CompostTumblerBlockEntity.class, remap = false)
public abstract class CompostTumblerBlockEntityMixin extends TFCBlockEntity implements IHaveGoggleInformation {

    public CompostTumblerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "isRotating", at = @At("HEAD"), cancellable = true)
    private void onIsRotating(CallbackInfoReturnable<Boolean> cir) {
        if (level != null) {
            Direction back = getBlockState().getValue(CompostTumblerBlock.FACING).getOpposite();
            if (level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
                float stressAtBase = (float) BlockStressValues.getImpact(getBlockState().getBlock());
                float faceSpeed = Math.abs(CreateKineticsHelper.getActualSpeed(kbe, back.getOpposite()));
                boolean overstressed = kbe.isOverStressed() || (stressAtBase * faceSpeed > (TFGConfig.SERVER.COMPOSTER_STRESS_LIMIT.get() + .0001f));
                if (faceSpeed > 0 && !overstressed) {
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float stressAtBase = (float) BlockStressValues.getImpact(getBlockState().getBlock());
        if (stressAtBase == 0) {
            return false;
        }

        Direction back = getBlockState().getValue(CompostTumblerBlock.FACING).getOpposite();
        float faceSpeed = 0;
        boolean overstressed = false;
        if (level != null && level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
            faceSpeed = Math.abs(CreateKineticsHelper.getActualSpeed(kbe, back.getOpposite()));
            overstressed = kbe.isOverStressed() || (stressAtBase * faceSpeed > (TFGConfig.SERVER.COMPOSTER_STRESS_LIMIT.get() + .0001f));
        }

        if (overstressed) {
            CreateLang.translate("gui.stressometer.overstressed")
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip);
        }

        CreateLang.translate("gui.goggles.kinetic_stats")
                .forGoggles(tooltip);

        CreateLang.translate("tooltip.stressImpact")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float stressTotal = stressAtBase * faceSpeed;

        CreateLang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CreateLang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        Lang.builder("greate").translate("tooltip.max_capacity")
                .style(ChatFormatting.GRAY)
                .space()
                .add(CreateLang.number(TFGConfig.SERVER.COMPOSTER_STRESS_LIMIT.get())
                        .style(ChatFormatting.RED))
                .add(CreateLang.text("SU")
                        .style(ChatFormatting.RED))
                .forGoggles(tooltip);

        CreateLang.translate("schedule.instruction.throttle")
                .style(ChatFormatting.GRAY)
                .text(":")
                .space()
                .add(CreateLang.text("<")
                        .style(ChatFormatting.RED))
                .space()
                .add(CreateLang.number(TFGConfig.SERVER.COMPOSTER_RPM_LIMIT.get())
                        .style(ChatFormatting.RED))
                .add(CreateLang.translate("generic.unit.rpm")
                        .style(ChatFormatting.RED))
                .forGoggles(tooltip);

        return true;
    }
}
