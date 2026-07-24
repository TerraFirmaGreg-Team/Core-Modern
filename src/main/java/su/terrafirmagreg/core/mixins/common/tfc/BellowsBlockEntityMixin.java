package su.terrafirmagreg.core.mixins.common.tfc;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = BellowsBlockEntity.class, remap = false)
public abstract class BellowsBlockEntityMixin extends TFCBlockEntity {

    public BellowsBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "getCrankRotation", at = @At("HEAD"), cancellable = true)
    private void onGetCrankRotation(CallbackInfoReturnable<Rotation> cir) {
        if (level != null) {
            Direction back = getBlockState().getValue(BellowsBlock.FACING).getOpposite();
            if (level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
                float speed = Math.abs(kbe.getSpeed());
                if (speed > 0) {
                    // Speed limit.
                    speed = Math.min(speed, 16f);
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
    }

    @Inject(method = "getExtensionLength", at = @At("HEAD"), cancellable = true)
    private void onGetExtensionLength(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (level != null) {
            Direction back = getBlockState().getValue(BellowsBlock.FACING).getOpposite();
            if (level.getBlockEntity(worldPosition.relative(back)) instanceof KineticBlockEntity kbe) {
                float speed = Math.abs(kbe.getSpeed());
                if (speed > 0) {
                    // Speed limit.
                    speed = Math.min(speed, 16f);
                    // Convert RPM to TFC rad/tick.
                    float tfcSpeed = speed * (float) Math.PI / 600f;
                    float angle = (level.getGameTime() + partialTick) * tfcSpeed;
                    cir.setReturnValue(0.125f + 0.25f * (1.0f + Mth.sin(angle)));
                }
            }
        }
    }
}
