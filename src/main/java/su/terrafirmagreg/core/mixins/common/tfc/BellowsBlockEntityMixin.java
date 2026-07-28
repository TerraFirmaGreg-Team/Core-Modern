package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.Lang;
import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.create.RotationPropagatorAccessor;
import su.terrafirmagreg.core.utils.CreateKineticsHelper;

/**
 * Mixin for {@link BellowsBlockEntity} to function with Create rotation.
 */
@Mixin(value = BellowsBlockEntity.class, remap = false)
public abstract class BellowsBlockEntityMixin extends TFCBlockEntity implements IHaveGoggleInformation {

    public BellowsBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "isConnectedToNetwork", at = @At("HEAD"), cancellable = true)
    private void tfg$onIsConnectedToNetwork(CallbackInfoReturnable<Boolean> cir) {
        if (level != null) {
            Direction back = getBlockState().getValue(BellowsBlock.FACING).getOpposite();
            if (level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
                if (RotationPropagatorAccessor.callGetAxisModifier(kbe, back.getOpposite()) != 0) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "getCrankRotation", at = @At("HEAD"), cancellable = true)
    private void tfg$onGetCrankRotation(CallbackInfoReturnable<Rotation> cir) {
        if (level != null) {
            Direction back = getBlockState().getValue(BellowsBlock.FACING).getOpposite();
            if (level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
                float faceSpeed = Math.abs(CreateKineticsHelper.getActualSpeed(kbe, back.getOpposite()));
                float impact = (float) BlockStressValues.getImpact(getBlockState().getBlock());

                if (impact * faceSpeed > (TFGConfig.SERVER.BELLOWS_STRESS_LIMIT.get() + 0.0001f) || kbe.isOverStressed()) {
                    cir.setReturnValue(new Rotation() {
                        @Override
                        public float angle(float partialTick) {
                            return 0;
                        }

                        @Override
                        public float speed() {
                            return 0;
                        }

                        @Override
                        public @NotNull Direction direction() {
                            return Direction.UP;
                        }
                    });
                    return;
                }

                float speed = Math.min(faceSpeed, TFGConfig.SERVER.BELLOWS_RPM_LIMIT.get());
                // Convert RPM to TFC rad/tick.
                float tfcSpeed = speed * (float) Math.PI / 600f;
                cir.setReturnValue(new Rotation() {
                    @Override
                    public float angle(float partialTick) {
                        assert level != null;
                        return (level.getGameTime() + partialTick) * tfcSpeed;
                    }

                    @Override
                    public float speed() {
                        return tfcSpeed;
                    }

                    @Override
                    public @NotNull Direction direction() {
                        return Direction.UP;
                    }
                });
            }
        }
    }

    @Inject(method = "getExtensionLength", at = @At("HEAD"), cancellable = true)
    private void tfg$onGetExtensionLength(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (level != null) {
            Direction back = getBlockState().getValue(BellowsBlock.FACING).getOpposite();
            if (level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
                float faceSpeed = Math.abs(CreateKineticsHelper.getActualSpeed(kbe, back.getOpposite()));
                float impact = (float) BlockStressValues.getImpact(getBlockState().getBlock());

                if (impact * faceSpeed > (TFGConfig.SERVER.BELLOWS_STRESS_LIMIT.get() + 0.0001f) || kbe.isOverStressed()) {
                    cir.setReturnValue(0.125f);
                    return;
                }

                float speed = Math.min(faceSpeed, TFGConfig.SERVER.BELLOWS_RPM_LIMIT.get());
                // Convert RPM to TFC rad/tick.
                float tfcSpeed = speed * (float) Math.PI / 600f;
                float angle = (level.getGameTime() + partialTick) * tfcSpeed;
                cir.setReturnValue(0.125f + 0.25f * (1.0f + Mth.sin(angle)));
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float stressAtBase = (float) BlockStressValues.getImpact(getBlockState().getBlock());
        if (stressAtBase == 0) {
            return false;
        }

        Direction back = getBlockState().getValue(BellowsBlock.FACING).getOpposite();
        float faceSpeed = 0;
        boolean overstressed = false;
        if (level != null && level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
            faceSpeed = Math.abs(CreateKineticsHelper.getActualSpeed(kbe, back.getOpposite()));
            overstressed = kbe.isOverStressed() || (stressAtBase * faceSpeed > (TFGConfig.SERVER.BELLOWS_STRESS_LIMIT.get() + 0.0001f));
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
                .add(CreateLang.number(TFGConfig.SERVER.BELLOWS_STRESS_LIMIT.get())
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
                .add(CreateLang.number(TFGConfig.SERVER.BELLOWS_RPM_LIMIT.get())
                        .style(ChatFormatting.RED))
                .add(CreateLang.translate("generic.unit.rpm")
                        .style(ChatFormatting.RED))
                .forGoggles(tooltip);

        return true;
    }
}
