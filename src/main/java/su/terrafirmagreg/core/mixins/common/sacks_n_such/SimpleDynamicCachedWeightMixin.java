package su.terrafirmagreg.core.mixins.common.sacks_n_such;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.dries007.tfc.common.capabilities.size.Weight;
import net.minecraft.world.item.ItemStack;

import mod.traister101.sns.common.capability.SimpleDynamicCachedWeight;
import mod.traister101.sns.common.items.SNSItems;

@Mixin(value = SimpleDynamicCachedWeight.class, remap = false)
public abstract class SimpleDynamicCachedWeightMixin {
    @Final
    @Shadow(remap = false)
    private ItemStack owner;

    /** Changes the dynamic weight returning VERY_HEAVY when nearly full to HEAVY for HUGE containers so they don't trigger the exhaust debuff on players
     * @author Ujhik
     * @reason To avoid exhaust debuff given by FRAME_PACK or QUIVER when full or nearly full because they are HUGE and the dynamic weight of the mod sns returns VERY_HEAVY
     */
    @ModifyReturnValue(method = "getWeight", at = @At("RETURN"), remap = false)
    private Weight tfg$changeVeryHeavyToHeavy(Weight originalReturn) {
        boolean isFramePack = owner.is(SNSItems.FRAME_PACK.get());
        boolean isQuiver = owner.is(SNSItems.QUIVER.get());
        if (originalReturn == Weight.VERY_HEAVY && (isFramePack || isQuiver)) {
            return Weight.HEAVY;
        }

        return originalReturn;
    }
}
