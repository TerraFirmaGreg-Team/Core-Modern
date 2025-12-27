package su.terrafirmagreg.core.mixins.common.sophisticatedbackpacks;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.data.TFGTags;

@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper", remap = false)
public class FeedingUpgradeWrapperMixin {

    // Cancel backpack feeding upgrade tick when player is in PlayerRevive bleeding state
    // This prevents food from being consumed without effect
    @Inject(method = "tick", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$preventFeedingWhenBleeding(@Nullable Entity entity, Level level, BlockPos pos, CallbackInfo ci) {
        if (entity instanceof Player player && player.getPersistentData().getBoolean("playerrevive:bleeding")) {
            ci.cancel();
        }
    }

    // Only eat food, don't eat blacklisted food
    @Overwrite(remap = false)
    private static boolean isEdible(ItemStack stack, LivingEntity player) {
        return stack.is(TFCTags.Items.FOODS) && !stack.is(TFGTags.Items.AutoEatBlacklist);
    }
}
