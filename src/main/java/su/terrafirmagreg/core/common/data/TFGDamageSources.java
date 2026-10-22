package su.terrafirmagreg.core.common.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.TFGCore;

/**
 * Same structure as TFCDamageSources
*/
public final class TFGDamageSources {

    public static final ResourceKey<DamageType> HYPOXIA = ResourceKey.create(Registries.DAMAGE_TYPE, TFGCore.id("hypoxia"));

    private TFGDamageSources() {
    }

    public static void hypoxia(Entity entity, float amount) {
        entity.hurt(new DamageSource(fetch(HYPOXIA, entity.level())), amount);
    }

    private static Holder<DamageType> fetch(ResourceKey<DamageType> type, Level level) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
    }
}
