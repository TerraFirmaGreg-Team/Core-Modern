package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
            if (speed > 0) {
                // Speed limit.
                speed = Math.min(speed, 32f);
                // Convert RPM to TFC rad/tick.
                cir.setReturnValue(speed * (float) Math.PI / 600f);
            }
        }
    }

    @Inject(method = "isConnectedToNetwork", at = @At("HEAD"), cancellable = true)
    private void onIsConnectedToNetwork(CallbackInfoReturnable<Boolean> cir) {
        QuernBlockEntity quern = (QuernBlockEntity) (Object) this;
        if (quern.getRotationSpeed() > 0 && quern.hasHandstone()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getRotationAngle", at = @At("HEAD"), cancellable = true)
    private void onGetRotationAngle(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (getRotationSpeed() > 0 && level != null) {
            float speed = getRotationSpeed();
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

        CreateLang.translate("gui.goggles.kinetic_stats")
                .forGoggles(tooltip);

        CreateLang.translate("tooltip.stressImpact")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float speed = 0;
        if (level != null && level.getBlockEntity(worldPosition.above()) instanceof KineticBlockEntity kbe) {
            speed = Math.abs(kbe.getSpeed());
        }

        float stressTotal = stressAtBase * speed;

        CreateLang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CreateLang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        CreateLang.translate("create.tooltip.speedRequirement")
                .style(ChatFormatting.GRAY)
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
