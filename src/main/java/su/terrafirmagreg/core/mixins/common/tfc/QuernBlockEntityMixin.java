package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.Lang;
import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = QuernBlockEntity.class, remap = false)
public abstract class QuernBlockEntityMixin extends TFCBlockEntity implements IHaveGoggleInformation {

    public QuernBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract float getRotationSpeed();

    @Inject(method = "getRotationSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetRotationSpeed(CallbackInfoReturnable<Float> cir) {
        if (level != null && level.getBlockEntity(worldPosition.above()) instanceof KineticBlockEntity kbe) {
            float speed = Math.abs(kbe.getSpeed());
            float theoreticalSpeed = Math.abs(kbe.getTheoreticalSpeed());
            float impact = (float) BlockStressValues.getImpact(getBlockState().getBlock());

            if (impact * theoreticalSpeed > 8.0001f || kbe.isOverStressed()) {
                cir.setReturnValue(0f);
                return;
            }

            // Speed limit.
            speed = Math.min(speed, 32f);
            // Convert RPM to TFC rad/tick.
            cir.setReturnValue(speed * (float) Math.PI / 600f);
        }
    }

    @Inject(method = "isConnectedToNetwork", at = @At("HEAD"), cancellable = true)
    private void onIsConnectedToNetwork(CallbackInfoReturnable<Boolean> cir) {
        QuernBlockEntity quern = (QuernBlockEntity) (Object) this;
        if (level != null && level.getBlockEntity(worldPosition.above()) instanceof KineticBlockEntity && quern.hasHandstone()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getRotationAngle", at = @At("HEAD"), cancellable = true)
    private void onGetRotationAngle(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (level != null && level.getBlockEntity(worldPosition.above()) instanceof KineticBlockEntity) {
            float speed = getRotationSpeed();
            if (speed <= 0) {
                cir.setReturnValue(0f);
                return;
            }
            float degreesPerTick = speed * (180f / (float) Math.PI);
            float angle = (level.getGameTime() + partialTick) * degreesPerTick;
            cir.setReturnValue(angle % 360);
        }
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void onServerTick(Level level, BlockPos pos, BlockState state, QuernBlockEntity quern, CallbackInfo ci) {
        if (quern.getRotationSpeed() > 0 && !quern.isGrinding() && level.getGameTime() % 10 == 0) {
            quern.startGrinding();
        }
    }

    @Inject(method = "updateHandstone", at = @At("TAIL"))
    private void onUpdateHandstone(CallbackInfo ci) {
        tfg$notifyCreateNeighbors();
    }

    @Unique
    private void tfg$notifyCreateNeighbors() {
        if (level != null && !level.isClientSide) {
            for (Direction direction : Direction.values()) {
                if (level.getBlockEntity(worldPosition.relative(direction)) instanceof KineticBlockEntity kbe) {
                    kbe.updateSpeed = true;
                    kbe.networkDirty = true;
                    kbe.setChanged();
                }
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        QuernBlockEntity quern = (QuernBlockEntity) (Object) this;
        if (!quern.hasHandstone()) {
            return false;
        }

        float stressAtBase = (float) BlockStressValues.getImpact(getBlockState().getBlock());
        if (stressAtBase == 0) {
            return false;
        }

        float theoreticalSpeed = 0;
        boolean overstressed = false;
        if (level != null && level.getBlockEntity(worldPosition.above()) instanceof KineticBlockEntity kbe) {
            theoreticalSpeed = Math.abs(kbe.getTheoreticalSpeed());
            overstressed = kbe.isOverStressed() || (stressAtBase * theoreticalSpeed > 8.0001f);
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

        float stressTotal = stressAtBase * theoreticalSpeed;

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
                .add(CreateLang.text("§48.0 §7(§8ULS§7)"))
                .forGoggles(tooltip);

        CreateLang.translate("schedule.instruction.throttle")
                .style(ChatFormatting.GRAY)
                .text(":")
                .space()
                .add(CreateLang.text("<")
                        .style(ChatFormatting.RED))
                .space()
                .add(CreateLang.number(32f)
                        .style(ChatFormatting.RED))
                .add(CreateLang.translate("generic.unit.rpm")
                        .style(ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        return true;
    }
}
