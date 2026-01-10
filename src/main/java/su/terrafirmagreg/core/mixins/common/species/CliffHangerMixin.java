package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;

import com.ninni.species.server.entity.mob.update_3.CliffHanger;
import com.ninni.species.server.entity.mob.update_3.Hanger;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Mixin(value = CliffHanger.class, remap = false)
public abstract class CliffHangerMixin extends Hanger {
    protected CliffHangerMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
