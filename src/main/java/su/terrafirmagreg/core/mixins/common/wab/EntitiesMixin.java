package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.wanmine.wab.entity.Snatcher;
import net.wanmine.wab.event.Entities;

import su.terrafirmagreg.core.common.data.entities.snatcher.SnatcherData;

@Mixin(value = Entities.class, remap = false)
public class EntitiesMixin {

    // Snatchers don't have a finalizeSpawn method for some reason, so we have to set
    // our additional entity data here

    @Inject(method = "onSpawn", at = @At("TAIL"), remap = false)
    private static void tfg$onSpawn(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        if (event.getEntity() instanceof Snatcher snatcher) {
            snatcher.getEntityData().set(SnatcherData.DATA_IS_MALE, snatcher.getRandom().nextBoolean());
        }
    }
}
