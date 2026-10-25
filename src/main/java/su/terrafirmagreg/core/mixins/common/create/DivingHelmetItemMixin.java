package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.equipment.armor.DivingHelmetItem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;

import su.terrafirmagreg.core.common.environment.HypoxiaHandler;

@Mixin(value = DivingHelmetItem.class, remap = false)
public abstract class DivingHelmetItemMixin {

    /**
     * Made by TomPlop so the Diving Gear can't be use when exposed to Vacuum - check HypoxiaHandler
     */
    @Inject(method = "breatheUnderwater", at = @At("HEAD"), cancellable = true)
    private static void tfg$disableDivingGear(LivingBreatheEvent event, CallbackInfo ci) {
        LivingEntity entity = event.getEntity();
        if (!DivingHelmetItem.isWornBy(entity) || HypoxiaHandler.isDivingGearDisabled(entity)) {
            return;
        }
        if (entity.level().isClientSide()) {
            entity.getPersistentData().remove("VisualBacktankAir");
        }
        ci.cancel();
    }
}
