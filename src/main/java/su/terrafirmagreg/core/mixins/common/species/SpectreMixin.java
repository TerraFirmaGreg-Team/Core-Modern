package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_3.Spectre;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Spectre.class, remap = false)
public abstract class SpectreMixin extends Monster {

    protected SpectreMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract void setVariant(Spectre.Type type);

    @Shadow
    public abstract Spectre.Type getVariant();

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), remap = true)
    private void tfg$changeType(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag tag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        // If easy difficulty, only spawn the basic variant
        if (levelAccessor.getDifficulty() == Difficulty.EASY)
            return;

        // Otherwise, 20% for hulking, 40% for the others
        float rand = levelAccessor.getRandom().nextFloat();
        if (rand < 0.2f)
            setVariant(Spectre.Type.HULKING_SPECTRE);
        else if (rand < 0.6f)
            setVariant(Spectre.Type.JOUSTING_SPECTRE);
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"), remap = true)
    private void tfg$changeAttributes(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag tag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        if (getVariant() == Spectre.Type.HULKING_SPECTRE) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0F);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0F);
        } else if (getVariant() == Spectre.Type.JOUSTING_SPECTRE) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0F);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(15.0F);
        }
    }
}
