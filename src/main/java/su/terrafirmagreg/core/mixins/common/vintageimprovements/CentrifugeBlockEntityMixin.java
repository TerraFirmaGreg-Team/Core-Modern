package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.negodya1.vintageimprovements.content.kinetics.centrifuge.CentrifugeBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;

@Mixin(value = CentrifugeBlockEntity.class, remap = false)
public abstract class CentrifugeBlockEntityMixin extends KineticBlockEntity implements ITieredKineticBlockEntity {
    public CentrifugeBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @ModifyExpressionValue(method = "addToGoggleTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", remap = true))
    private MutableComponent tfg$useItemDisplayName(MutableComponent original, @Local ItemStack stackInSlot) {
        return stackInSlot.getHoverName().copy();
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"))
    private void tfg$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        addToGoggleTooltip(tooltip, GTMaterials.Steel, getSpeed());
    }
}
