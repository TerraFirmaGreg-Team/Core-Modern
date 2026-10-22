package su.terrafirmagreg.core.mixins.common.primitive_creatures;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import earth.terrarium.adastra.api.planets.Planet;
import primitive_creatures_.entity.Golem2Entity;

@Mixin(value = Golem2Entity.class, remap = false)
public class Golem2Mixin extends Monster {

    protected Golem2Mixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        if (level().dimension() == Planet.VENUS) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(16.0);
        }
        return super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
