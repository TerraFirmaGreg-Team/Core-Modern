package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;

@Mixin(value = WanderingTrader.class, remap = true)
public abstract class WanderingTraderMixin extends AbstractVillager {

    // Prevents wandering traders from using milk buckets/invis potions

    public WanderingTraderMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void tfg$registerGoals(CallbackInfo ci) {
        this.goalSelector.removeAllGoals(goal -> goal instanceof UseItemGoal<?>);
    }
}
