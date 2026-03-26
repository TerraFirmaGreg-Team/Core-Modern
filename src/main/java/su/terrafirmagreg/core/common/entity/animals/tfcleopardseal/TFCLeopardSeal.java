/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the License at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.entity.animals.tfcleopardseal;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Dynamic;

import net.dries007.tfc.common.entities.aquatic.AmphibiousAnimal;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.ForgeMod;

import su.terrafirmagreg.core.common.data.TFGSounds;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.entity.ai.amphibian.PinnipedAI;

public class TFCLeopardSeal extends AmphibiousAnimal {

    public static @NotNull AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0D).add(Attributes.MOVEMENT_SPEED, 1.0D).add(Attributes.ATTACK_DAMAGE, 6.0D).add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.0);
    }

    public TFCLeopardSeal(EntityType<? extends AmphibiousAnimal> type, Level level) {
        super(type, level, TFGSounds.SEAL);
    }

    // TODO: Would like leopard seals to defend themselves rather than play dead
    @Override
    public boolean isPlayingDeadEffective() {
        return false;
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return Helpers.isItem(stack, TFGTags.Items.SEAL_FOOD);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ambient.get();
    }

    public void playAmbientSound() {
        if (!this.isInWaterOrBubble()) {
            super.playAmbientSound();
        }
    }

    @Override
    protected @NotNull Brain.Provider<? extends AmphibiousAnimal> brainProvider() {
        return Brain.provider(PinnipedAI.MEMORY_TYPES, PinnipedAI.SENSOR_TYPES);
    }

    @Override
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        return PinnipedAI.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep() {
        getBrain().tick((ServerLevel) level(), this);
        PinnipedAI.updateActivity(this);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    public static boolean spawnRules(EntityType<? extends TFCLeopardSeal> type, LevelAccessor level, MobSpawnType spawn,
            BlockPos pos, RandomSource rand) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
