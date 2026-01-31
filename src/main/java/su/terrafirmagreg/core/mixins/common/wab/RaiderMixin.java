package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.wanmine.wab.entity.Raider;
import net.wanmine.wab.init.data.WabTags;

@Mixin(value = Raider.class)
public abstract class RaiderMixin extends Animal {

    @Shadow
    private int neutralTick;

    protected RaiderMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
    public void tfg$spawnChildFromBreeding(ServerLevel level, Animal mate, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void tfg$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        Raider raider = (Raider) (Object) this;

        if (itemStack.is(WabTags.Items.MEAT_FOOD) && !raider.isNeutral()) {
            this.usePlayerItem(player, hand, itemStack);
            neutralTick = 3600;
            this.setTarget(null);
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }
    }

    @Inject(method = "getLootingLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$getLootingLevel(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1);
    }
}
